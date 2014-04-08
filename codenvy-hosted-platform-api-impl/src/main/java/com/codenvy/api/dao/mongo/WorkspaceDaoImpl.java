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

import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.server.exception.WorkspaceException;
import com.codenvy.api.workspace.server.exception.WorkspaceNotFoundException;
import com.codenvy.api.workspace.shared.dto.Attribute;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.api.dao.exception.ItemNamingException;
import com.codenvy.api.dao.util.NamingValidator;
import com.mongodb.*;
import com.mongodb.util.JSON;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Workspace DAO implementation based on MongoDB storage.
 */
@Singleton
public class WorkspaceDaoImpl implements WorkspaceDao {

    protected static final String DB_COLLECTION = "organization.storage.db.workspace.collection";

    DBCollection collection;

    UserDao userDao;

    @Inject
    public WorkspaceDaoImpl(UserDao userDao, DB db, @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        collection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        this.userDao = userDao;
    }

    @Override
    public void create(Workspace workspace) throws WorkspaceException {
        try {
            NamingValidator.validate(workspace.getName());
            validateWorkspaceNameAvailable(workspace);
            collection.save(toDBObject(workspace));
        } catch (ItemNamingException e) {
            throw new WorkspaceException(e.getMessage());
        } catch (MongoException me) {
            throw new WorkspaceException(me.getMessage(), me);
        }
    }

    @Override
    public void update(Workspace workspace) throws WorkspaceException {
        DBObject query = new BasicDBObject("id", workspace.getId());
        if (collection.findOne(query) == null) {
            throw new WorkspaceNotFoundException(workspace.getId());
        }
        try {
            NamingValidator.validate(workspace.getName());
            collection.update(query, toDBObject(workspace));
        } catch (ItemNamingException e) {
            throw new WorkspaceException(e.getMessage());
        } catch (MongoException me) {
            throw new WorkspaceException(me.getMessage(), me);
        }
    }

    @Override
    public void remove(String id) throws WorkspaceException {
        try {
            collection.remove(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new WorkspaceException(me.getMessage(), me);
        }
    }

    @Override
    public Workspace getById(String id) throws WorkspaceException {
        DBObject res;
        try {
            res = collection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new WorkspaceException(me.getMessage(), me);
        }
        return res != null ? DtoFactory.getInstance().createDtoFromJson(res.toString(), Workspace.class) : null;
    }

    @Override
    public Workspace getByName(String name) throws WorkspaceException {
        DBObject res;
        try {
            res = collection.findOne(new BasicDBObject("name", name));
        } catch (MongoException me) {
            throw new WorkspaceException(me.getMessage(), me);
        }
        return res != null ? DtoFactory.getInstance().createDtoFromJson(res.toString(), Workspace.class) : null;
    }

    @Override
    public List<Workspace> getByOrganization(String organizationId) throws WorkspaceException {
        List<Workspace> result = new ArrayList<>();
        try {
            DBCursor cursor = collection.find(new BasicDBObject("organizationId", organizationId));
            for (DBObject one : cursor) {
                result.add(DtoFactory.getInstance().createDtoFromJson(one.toString(), Workspace.class));
            }
        } catch (MongoException me) {
            throw new WorkspaceException(me.getMessage(), me);
        }
        return result;
    }

    /**
     * Convert workspace to Database ready-to-use object,
     * @param obj Workspace to convert
     * @return DBObject
     */
    private  DBObject toDBObject(Workspace obj) {
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
                                        .withOrganizationId(obj.getOrganizationId())
                                        .withTemporary(obj.isTemporary())
                                        .withAttributes(attributes);

        return (DBObject)JSON.parse(workspace.toString());
    }


    /**
     * Ensure that user given workspace name not already occupied.
     * @param workspace  workspace to check
     * @throws WorkspaceException
     */
    private void validateWorkspaceNameAvailable(Workspace workspace) throws WorkspaceException{
        DBObject res = collection.findOne(new BasicDBObject("name", workspace.getName()));
        if (res != null) {
            throw new WorkspaceException(
                    String.format("Unable to create workspace: name '%s' already exists.", workspace.getName()));
        }

    }
}
