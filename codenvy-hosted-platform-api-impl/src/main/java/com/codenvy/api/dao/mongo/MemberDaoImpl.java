/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.workspace.server.dao.Member;
import com.codenvy.api.workspace.server.dao.MemberDao;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

/**
 * Implementation of {@link MemberDao} based on MongoDB storage.
 * <pre>
 * Workspace members collection document scheme:
 *
 * {
 *     "_id" : "userId...",
 *     "members" : [
 *          ...
 *          {
 *              "userId" : "userId...",
 *              "workspaceId" : "workspaceId...",
 *              "roles" : [
 *                  "role1...",
 *                  "role2..."
 *              ]
 *          }
 *          ...
 *     ]
 * }
 *
 * </pre>
 *
 * @author Eugene Voevodin
 */
@Singleton
public class MemberDaoImpl implements MemberDao {
    private static final Logger LOG           = LoggerFactory.getLogger(MemberDaoImpl.class);
    private static final String DB_COLLECTION = "organization.storage.db.ws.member.collection";

    private final DBCollection collection;
    private final UserDao      userDao;
    private final WorkspaceDao workspaceDao;

    @Inject
    public MemberDaoImpl(UserDao userDao,
                         WorkspaceDao workspaceDao,
                         DB db,
                         @Named(DB_COLLECTION) String collectionName) {
        this.userDao = userDao;
        this.workspaceDao = workspaceDao;
        collection = db.getCollection(collectionName);
        collection.ensureIndex("members.workspaceId");
    }

    @Override
    public void create(Member newMember) throws ConflictException, ServerException, NotFoundException {
        checkWorkspaceAndUserExist(newMember);
        try {
            final DBObject membersDocument = documentFor(newMember);
            final BasicDBList members = (BasicDBList)membersDocument.get("members");
            checkMemberIsAbsent(newMember, members);
            //member doesn't exist so we can create and save it
            members.add(toDBObject(newMember));
            collection.save(membersDocument);
            logUserAddedToWsEvent(newMember);
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to persist member");
        }
    }

    @Override
    public void update(Member update) throws ConflictException, NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("_id", update.getUserId());
        try {
            final DBObject membersDocument = collection.findOne(query);
            if (membersDocument == null) {
                throw new NotFoundException(format("User %s doesn't have memberships", update.getUserId()));
            }
            final BasicDBList dbMembers = (BasicDBList)membersDocument.get("members");
            if (!remove(update.getWorkspaceId(), dbMembers)) {
                throw new NotFoundException(format("Membership between %s and %s not found", update.getUserId(), update.getWorkspaceId()));
            }
            dbMembers.add(toDBObject(update));
            collection.update(query, membersDocument);
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to update member");
        }
    }

    @Override
    public List<Member> getWorkspaceMembers(String workspaceId) throws ServerException {
        final List<Member> workspaceMembers;
        try (DBCursor membersCursor = collection.find(new BasicDBObject("members.workspaceId", workspaceId))) {
            workspaceMembers = new ArrayList<>(membersCursor.size());
            for (DBObject membersDocument : membersCursor) {
                final BasicDBList members = (BasicDBList)membersDocument.get("members");
                workspaceMembers.add(retrieveMember(workspaceId, members));
            }
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to retrieve workspace members");
        }
        return workspaceMembers;
    }

    @Override
    public List<Member> getUserRelationships(String userId) throws ServerException {
        final List<Member> userRelationships = new LinkedList<>();
        try {
            final DBObject membersDocument = collection.findOne(userId);
            if (membersDocument != null) {
                final BasicDBList members = (BasicDBList)membersDocument.get("members");
                for (Object memberObj : members) {
                    userRelationships.add(fromDBObject((DBObject)memberObj));
                }
            }
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to get user relationships");
        }
        return userRelationships;
    }

    @Override
    public Member getWorkspaceMember(String wsId, String userId) throws ServerException, NotFoundException {
        try {
            final DBObject membersDocument = collection.findOne(new BasicDBObject("_id", userId).append("members.workspaceId", wsId));
            if (membersDocument == null) {
                throw new NotFoundException(format("Membership between %s and %s not found", userId, wsId));
            }
            final BasicDBList members = (BasicDBList)membersDocument.get("members");
            return retrieveMember(wsId, members);
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException(ex.getMessage(), ex);
        }
    }

    @Override
    public void remove(Member member) throws ServerException, NotFoundException, ConflictException {
        final DBObject query = new BasicDBObject("_id", member.getUserId());
        try {
            final DBObject membersDocument = collection.findOne(query);
            if (membersDocument == null) {
                throw new NotFoundException(format("User %s doesn't have workspace memberships", member.getUserId()));
            }
            final BasicDBList members = (BasicDBList)membersDocument.get("members");
            //remove member from members list
            if (!remove(member.getWorkspaceId(), members)) {
                throw new NotFoundException(format("Membership between %s and %s not found", member.getUserId(), member.getWorkspaceId()));
            }
            //if user doesn't have memberships then remove document
            if (!members.isEmpty()) {
                collection.update(query, membersDocument);
            } else {
                collection.remove(query);
            }
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException(ex.getMessage(), ex);
        }
    }

    /*used in tests*/Member fromDBObject(DBObject memberObj) {
        final BasicDBObject basicMemberObj = (BasicDBObject)memberObj;
        final BasicDBList basicRoles = (BasicDBList)basicMemberObj.get("roles");
        final List<String> roles = new ArrayList<>(basicRoles.size());
        for (Object role : basicRoles) {
            roles.add(role.toString());
        }
        return new Member().withWorkspaceId(basicMemberObj.getString("workspaceId"))
                           .withUserId(basicMemberObj.getString("userId"))
                           .withRoles(roles);
    }

    private boolean remove(String workspaceId, BasicDBList src) {
        boolean found = false;
        final Iterator it = src.iterator();
        while (!found && it.hasNext()) {
            final Member member = fromDBObject((DBObject)it.next());
            if (member.getWorkspaceId().equals(workspaceId)) {
                found = true;
                it.remove();
            }
        }
        return found;
    }

    private Member retrieveMember(String workspaceId, BasicDBList src) {
        for (Object dbMember : src) {
            final Member member = fromDBObject((DBObject)dbMember);
            if (workspaceId.equals(member.getWorkspaceId())) {
                return member;
            }
        }
        return null;
    }

    /**
     * Convert Member object to Database ready-to-use object,
     *
     * @param member
     *         object to convert
     * @return DBObject
     */
    private DBObject toDBObject(Member member) {
        final BasicDBList dbRoles = new BasicDBList();
        dbRoles.addAll(member.getRoles());
        return new BasicDBObject().append("userId", member.getUserId())
                                  .append("workspaceId", member.getWorkspaceId())
                                  .append("roles", dbRoles);
    }

    private void checkWorkspaceAndUserExist(Member newMember) throws NotFoundException, ServerException {
        userDao.getById(newMember.getUserId());
        workspaceDao.getById(newMember.getWorkspaceId());
    }

    private DBObject documentFor(Member member) {
        DBObject membersDocument = collection.findOne(new BasicDBObject("_id", member.getUserId()));
        if (membersDocument == null) {
            membersDocument = new BasicDBObject("_id", member.getUserId());
            membersDocument.put("members", new BasicDBList());
        }
        return membersDocument;
    }

    private void checkMemberIsAbsent(Member target, BasicDBList members) throws ConflictException {
        for (Object dbMember : members) {
            final Member member = fromDBObject((DBObject)dbMember);
            if (target.getWorkspaceId().equals(member.getWorkspaceId())) {
                throw new ConflictException(format("Workspace %s already contains member %s", target.getWorkspaceId(), target.getUserId()));
            }
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
}
