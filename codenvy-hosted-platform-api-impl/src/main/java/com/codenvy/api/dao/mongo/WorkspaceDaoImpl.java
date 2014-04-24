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
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.server.exception.WorkspaceException;
import com.codenvy.api.workspace.shared.dto.Attribute;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Workspace DAO implementation based on MongoDB storage.
 */
@Singleton
public class WorkspaceDaoImpl implements WorkspaceDao {
    /* should contain [3, 20] characters, first and last character is letter or digit, available characters {A-Za-z0-9.-_}*/
    private static final Pattern WS_NAME = Pattern.compile("[\\w][\\w\\.\\-]{1,18}[\\w]");

    protected static final String DB_COLLECTION = "organization.storage.db.workspace.collection";

    DBCollection collection;
    UserDao      userDao;
    MemberDao    memberDao;

    @Inject
    public WorkspaceDaoImpl(UserDao userDao, MemberDao memberDao, DB db, @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        collection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        this.memberDao = memberDao;
        this.userDao = userDao;
    }

    @Override
    public void create(Workspace workspace) throws ConflictException, ServerException {
        try {
            validateWorkspaceName(workspace.getName());
            ensureWorkspaceNameDoesNotExist(workspace.getName());
            collection.save(toDBObject(workspace));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void update(Workspace workspace) throws ConflictException, NotFoundException, ServerException {
        DBObject query = new BasicDBObject("id", workspace.getId());
        if (collection.findOne(query) == null) {
            throw new NotFoundException("Workspace not found " + workspace.getId());
        }
        try {
            validateWorkspaceName(workspace.getName());
            collection.update(query, toDBObject(workspace));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void remove(String id) throws ServerException, NotFoundException, ConflictException {
        if (memberDao.getWorkspaceMembers(id).size() > 0) {
            throw new ConflictException("It is not possible to remove workspace with existing members");
        }
        try {
            collection.remove(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public Workspace getById(String id) throws NotFoundException, ServerException {
        DBObject res;
        try {
            res = collection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null)
            throw new NotFoundException("Workspace not found " + id);
        return DtoFactory.getInstance().createDtoFromJson(res.toString(), Workspace.class);
    }

    @Override
    public Workspace getByName(String name) throws NotFoundException, ServerException {
        DBObject res;
        try {
            res = collection.findOne(new BasicDBObject("name", name));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null)
            throw new NotFoundException("Workspace not found " + name);
        return DtoFactory.getInstance().createDtoFromJson(res.toString(), Workspace.class);
    }

    @Override
    public List<Workspace> getByAccount(String accountId) throws ServerException {
        List<Workspace> result = new ArrayList<>();
        try {
            DBCursor cursor = collection.find(new BasicDBObject("accountId", accountId));
            for (DBObject one : cursor) {
                result.add(DtoFactory.getInstance().createDtoFromJson(one.toString(), Workspace.class));
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    /**
     * Convert workspace to Database ready-to-use object,
     *
     * @param obj
     *         Workspace to convert
     * @return DBObject
     */
    private DBObject toDBObject(Workspace obj) {
        List<Attribute> attributes = new ArrayList<>();
        if (obj.getAttributes() != null) {
            for (Attribute one : obj.getAttributes()) {
                attributes.add(DtoFactory.getInstance().createDto(Attribute.class)
                                         .withName(one.getName())
                                         .withValue(one.getValue())
                                         .withDescription(one.getDescription()));
            }
        }
        Workspace workspace = DtoFactory.getInstance().createDto(Workspace.class)
                                        .withId(obj.getId())
                                        .withName(obj.getName())
                                        .withAccountId(obj.getAccountId())
                                        .withTemporary(obj.isTemporary())
                                        .withAttributes(attributes);

        return (DBObject)JSON.parse(workspace.toString());
    }

    private void validateWorkspaceName(String workspaceName) throws ConflictException {
        if (workspaceName == null) {
            throw new ConflictException("Workspace name required");
        }
        if (!WS_NAME.matcher(workspaceName).matches()) {
            throw new ConflictException("Incorrect workspace name");
        }
    }

    /**
     * Ensure that user given workspace name not already occupied and it is valid.
     *
     * @param workspaceName
     *         workspace name to check
     * @throws WorkspaceException
     */
    private void ensureWorkspaceNameDoesNotExist(String workspaceName) throws ConflictException {
        DBObject res = collection.findOne(new BasicDBObject("name", workspaceName));
        if (res != null) {
            throw new ConflictException(
                    String.format("Unable to create workspace: name '%s' already exists.", workspaceName));
        }
    }
}
