/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.dao.mongo;

import com.codenvy.api.user.server.dao.MemberDao;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.exception.MembershipException;
import com.codenvy.api.user.server.exception.UserException;
import com.codenvy.api.user.shared.dto.Member;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.server.exception.WorkspaceException;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.*;
import com.mongodb.util.JSON;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of MemberDAO based on MongoDB storage.
 *
 * Uses the following DB scheme:
 *
 *  ------------------------------------------------------------------
 * |  _ID (UserId)  |              List of members                    |
 * -------------------------------------------------------------------|
 * |   user1234     |  ["ws1", List<Roles>], ["ws2", List<Roles>]     |
 *  ------------------------------------------------------------------
 */
public class MemberDaoImpl implements MemberDao {

    protected static final String DB_COLLECTION = "organization.storage.db.ws.member.collection";

    DBCollection collection;

    UserDao userDao;

    WorkspaceDao workspaceDao;

    @Inject
    public MemberDaoImpl(UserDao userDao, WorkspaceDao workspaceDao, DB db,
                         @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        this.userDao = userDao;
        this.workspaceDao = workspaceDao;
    }

    @Override
    public void create(Member member) throws MembershipException {
        validateSubjectsExists(member.getUserId(), member.getWorkspaceId());
        try {
            // Retrieving his membership list, or creating new one
            DBObject old = collection.findOne(new BasicDBObject("_id", member.getUserId()));
            if (old == null) {
                old = new BasicDBObject("_id", member.getUserId());
            }
            BasicDBList members = (BasicDBList)old.get("members");
            if (members == null)
                members = new BasicDBList();

            // Ensure such member not exists yet
            for (Object member1 : members) {
                Member one = DtoFactory.getInstance().createDtoFromJson(member1.toString(), Member.class);
                if (one.getWorkspaceId().equals(member.getWorkspaceId()) &&
                    one.getUserId().equals(member.getUserId())) {
                    throw new MembershipException(
                            String.format(
                                    "Membership of user %s in workspace %s already exists. Use update method instead.",
                                    member.getUserId(), member.getWorkspaceId()));
                }
            }
            // Adding new member
            members.add(toDBObject(member));
            old.put("members", members);

            //Save
            collection.save(old);
        } catch (MongoException me) {
            throw new MembershipException(me.getMessage(), me);
        }
    }

    @Override
    public void update(Member member) throws MembershipException {
        validateSubjectsExists(member.getUserId(), member.getWorkspaceId());
        try {
            // Retrieving his membership list
            DBObject query = new BasicDBObject("_id", member.getUserId());
            DBObject old = collection.findOne(query);
            if (old == null)
                throw new MembershipException(
                        String.format("Unable to update membership: user %s does not exist.", member.getUserId()));

            BasicDBList members = (BasicDBList)old.get("members");
            if (members == null)
                throw new MembershipException(String.format("Unable to update membership: user %s doesn't have any memberships.", member.getUserId()));

            // Find membership in given WS and removing it
            boolean found = false;
            Iterator it = members.iterator();
            while (it.hasNext()) {
                Member one = DtoFactory.getInstance().createDtoFromJson(it.next().toString(), Member.class);
                if (one.getWorkspaceId().equals(member.getWorkspaceId())){
                    it.remove();
                    found = true;
                }
            }
            if (!found)
                throw new MembershipException(
                        String.format("Unable to update membership: user %s doesn't have no memberships in the WS %s.",
                                      member.getUserId(), member.getWorkspaceId()));

            // Save new
            members.add(toDBObject(member));
            old.put("members", members);
            collection.update(query, old);
        } catch (MongoException me) {
            throw new MembershipException(me.getMessage(), me);
        }
    }

    @Override
    public List<Member> getWorkspaceMembers(String wsId) throws MembershipException {
        List<Member> result = new ArrayList<>();
        try (DBCursor cursor = collection.find()) {
            for (DBObject one : cursor) {
                BasicDBList members = (BasicDBList)one.get("members");
                for (Object membershipObject : members) {
                    Member member = DtoFactory.getInstance().createDtoFromJson(membershipObject.toString(), Member.class);
                    if (wsId.equals(member.getWorkspaceId())) {
                        result.add(member);
                    }
                }
            }
        } catch (MongoException me) {
            throw new MembershipException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public List<Member> getUserRelationships(String userId) throws MembershipException {
        List<Member> result = new ArrayList<>();
        try {
            DBObject one = collection.findOne(userId);
            if (one == null) {
                return Collections.emptyList();
            }
            BasicDBList members = (BasicDBList)one.get("members");
            for (Object memberObj : members) {
                Member member = DtoFactory.getInstance().createDtoFromJson(memberObj.toString(), Member.class);
                result.add(member);
            }
        } catch (MongoException me) {
            throw new MembershipException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void remove(Member member) throws MembershipException {
        DBObject query = new BasicDBObject("_id", member.getUserId());
        try {
            DBObject old = collection.findOne(query);
            if (old == null) {
                return;
            }
            BasicDBList members = (BasicDBList)old.get("members");
            Iterator it = members.iterator();
            while (it.hasNext()) {
                Member one = DtoFactory.getInstance().createDtoFromJson(it.next().toString(), Member.class);
                if (member.getWorkspaceId().equals(one.getWorkspaceId()))
                    it.remove();
            }
            if (members.size() > 0) {
                old.put("members", members);
                collection.update(query, old);
            } else {
                collection.remove(query); // Removing user from table if no memberships anymore.
            }
        } catch (MongoException me) {
            throw new MembershipException(me.getMessage(), me);
        }
    }


    /**
     * Convert Member object to Database ready-to-use object,
     * @param obj object to convert
     * @return DBObject
     */
    private  DBObject toDBObject(Member obj) {
        Member member = DtoFactory.getInstance().createDto(Member.class)
                                  .withUserId(obj.getUserId())
                                  .withWorkspaceId(obj.getWorkspaceId())
                                  .withRoles(obj.getRoles());
        return (DBObject)JSON.parse(member.toString());
    }


    void validateSubjectsExists(String userId, String workspaceId) throws MembershipException {
        try {
            if (userDao.getById(userId) == null)
                throw new MembershipException(
                        String.format("Unable to update membership: user %s does not exist.", userId));
            if (workspaceDao.getById(workspaceId) == null)
                throw new MembershipException(
                        String.format("Unable to update membership: workspace %s does not exist.", workspaceId));
        } catch (UserException | WorkspaceException e) {
            throw new MembershipException(e.getMessage());
        }
    }
}
