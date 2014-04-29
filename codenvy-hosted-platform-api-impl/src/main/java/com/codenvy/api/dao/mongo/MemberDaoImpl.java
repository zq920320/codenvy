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

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.MemberDao;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.shared.dto.Member;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of MemberDAO based on MongoDB storage.
 * <p/>
 * Uses the following DB scheme:
 * <p/>
 * ------------------------------------------------------------------
 * |  _ID (UserId)  |              List of members                    |
 * -------------------------------------------------------------------|
 * |   user1234     |  ["ws1", List<Roles>], ["ws2", List<Roles>]     |
 * ------------------------------------------------------------------
 */
@Singleton
public class MemberDaoImpl implements MemberDao {
    private static final Logger LOG = LoggerFactory.getLogger(MemberDaoImpl.class);

    protected static final String DB_COLLECTION = "organization.storage.db.ws.member.collection";

    DBCollection collection;
    UserDao      userDao;
    WorkspaceDao workspaceDao;

    @Inject
    public MemberDaoImpl(UserDao userDao, WorkspaceDao workspaceDao, DB db,
                         @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        this.userDao = userDao;
        this.workspaceDao = workspaceDao;
    }

    @Override
    public void create(Member member) throws ConflictException, ServerException, NotFoundException {
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
                    throw new ConflictException(
                            String.format(
                                    "Membership of user %s in workspace %s already exists. Use update method instead.",
                                    member.getUserId(), member.getWorkspaceId())
                    );
                }
            }
            // Adding new member
            members.add(toDBObject(member));
            old.put("members", members);

            //Save
            collection.save(old);

            logUserAddedToWsEvent(member);
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    private void logUserAddedToWsEvent(Member member) {
        try {
            User user = userDao.getById(member.getUserId());
            Workspace workspace = workspaceDao.getById(member.getWorkspaceId());

            LOG.info("EVENT#user-added-to-ws# USER#{}# WS#{}# FROM#website#", user.getEmail(), workspace.getName());
        } catch (NotFoundException | ServerException e) {
            LOG.error("Can't log Analytics event", e);
        }
    }

    @Override
    public void update(Member member) throws ConflictException, NotFoundException, ServerException {
        validateSubjectsExists(member.getUserId(), member.getWorkspaceId());
        try {
            // Retrieving his membership list
            DBObject query = new BasicDBObject("_id", member.getUserId());
            DBObject old = collection.findOne(query);
            if (old == null)
                throw new NotFoundException(
                        String.format("Unable to update membership: user %s does not exist.", member.getUserId()));

            BasicDBList members = (BasicDBList)old.get("members");
            if (members == null)
                throw new NotFoundException(
                        String.format("Unable to update membership: user %s doesn't have any memberships.", member.getUserId()));

            // Find membership in given WS and removing it
            boolean found = false;
            Iterator it = members.iterator();
            while (it.hasNext()) {
                Member one = DtoFactory.getInstance().createDtoFromJson(it.next().toString(), Member.class);
                if (one.getWorkspaceId().equals(member.getWorkspaceId())) {
                    it.remove();
                    found = true;
                }
            }
            if (!found)
                throw new NotFoundException(
                        String.format("Unable to update membership: user %s doesn't have no memberships in the WS %s.",
                                      member.getUserId(), member.getWorkspaceId())
                );

            // Save new
            members.add(toDBObject(member));
            old.put("members", members);
            collection.update(query, old);
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public List<Member> getWorkspaceMembers(String wsId) throws ServerException {
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
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public List<Member> getUserRelationships(String userId) throws ServerException {
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
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void remove(Member member) throws ServerException, NotFoundException, ConflictException {
        validateSubjectsExists(member.getUserId(), member.getWorkspaceId());
        DBObject query = new BasicDBObject("_id", member.getUserId());
        try {
            DBObject old = collection.findOne(query);
            if (old == null) {
                throw new NotFoundException(String.format("Workspace member with id %s doesn't exist", member.getUserId()));
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
            throw new ServerException(me.getMessage(), me);
        }
    }


    /**
     * Convert Member object to Database ready-to-use object,
     *
     * @param obj
     *         object to convert
     * @return DBObject
     */
    private DBObject toDBObject(Member obj) {
        Member member = DtoFactory.getInstance().createDto(Member.class)
                                  .withUserId(obj.getUserId())
                                  .withWorkspaceId(obj.getWorkspaceId())
                                  .withRoles(obj.getRoles());
        return (DBObject)JSON.parse(member.toString());
    }

    void validateSubjectsExists(String userId, String workspaceId) throws ConflictException, ServerException,
                                                                          NotFoundException {
        try {
            userDao.getById(userId);
            workspaceDao.getById(workspaceId);
            if (userDao.getById(userId) == null)
                throw new ConflictException(
                        String.format("Unable to update membership: user %s does not exist.", userId));
            if (workspaceDao.getById(workspaceId) == null)
                throw new ConflictException(
                        String.format("Unable to update membership: workspace %s does not exist.", workspaceId));

        } catch (NotFoundException ex) {
            throw new ConflictException("Unable to update membership: " + ex.getMessage());
        }
    }
}
