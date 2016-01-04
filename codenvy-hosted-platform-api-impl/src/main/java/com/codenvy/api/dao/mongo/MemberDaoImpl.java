/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Implementation of {@link MemberDao} based on MongoDB storage.
 * <pre>
 * Workspace members collection document scheme:
 *
 * {
 *      "_id": ObjectId("..."),
 *      "userId" : "user123",
 *      "workspaceId" : "workspace123",
 *      "roles" : [ "workspace/admin", "workspace/developer" ]
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
        collection.createIndex(new BasicDBObject("userId", 1).append("workspaceId", 1), new BasicDBObject("unique", true));
    }

    @Override
    public void create(Member newMember) throws ConflictException, ServerException, NotFoundException {
        // check if user and workspace exist
        final User user = userDao.getById(newMember.getUserId());
        final Workspace workspace = workspaceDao.getById(newMember.getWorkspaceId());
        try {
            collection.insert(toDBObject(newMember));
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("User '%s' is already has membership in workspace '%s'",
                                               newMember.getUserId(),
                                               newMember.getWorkspaceId()));
        } catch (MongoException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
        LOG.info("EVENT#user-added-to-ws# USER#{}# WS#{}# FROM#website#", user.getEmail(), workspace.getName());
    }

    @Override
    public void update(Member update) throws ConflictException, NotFoundException, ServerException {
        try {
            if (collection.update(query(update.getUserId(), update.getWorkspaceId()), toDBObject(update)).getN() == 0) {
                throw new NotFoundException(format("Membership between '%s' and '%s' not found",
                                                   update.getUserId(),
                                                   update.getWorkspaceId()));
            }
        } catch (MongoException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public List<Member> getWorkspaceMembers(String workspaceId) throws ServerException {
        return doGetMembers(new BasicDBObject("workspaceId", workspaceId));
    }

    @Override
    public List<Member> getUserRelationships(String userId) throws ServerException {
        return doGetMembers(new BasicDBObject("userId", userId));
    }

    @Override
    public Member getWorkspaceMember(String wsId, String userId) throws ServerException, NotFoundException {
        final DBObject memberDoc = collection.findOne(query(userId, wsId));
        if (memberDoc == null) {
            throw new NotFoundException(format("Membership between '%s' and '%s' not found", userId, wsId));
        }
        return fromDBObject(memberDoc);
    }

    @Override
    public void remove(Member member) throws ServerException, NotFoundException, ConflictException {
        try {
            if (collection.remove(query(member.getUserId(), member.getWorkspaceId())).getN() == 0) {
                throw new NotFoundException(format("Membership between '%s' and '%s' not found",
                                                   member.getUserId(),
                                                   member.getWorkspaceId()));
            }
        } catch (MongoException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @VisibleForTesting
    Member fromDBObject(DBObject memberObj) {
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

    @VisibleForTesting
    BasicDBObject query(String userId, String workspaceId) {
        return new BasicDBObject("userId", userId).append("workspaceId", workspaceId);
    }

    private List<Member> doGetMembers(BasicDBObject query) throws ServerException {
        final List<Member> members;
        try (DBCursor cursor = collection.find(query)) {
            members = new ArrayList<>(cursor.count());
            for (DBObject memberDoc : cursor) {
                members.add(fromDBObject(memberDoc));
            }
        }
        return members;
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
}
