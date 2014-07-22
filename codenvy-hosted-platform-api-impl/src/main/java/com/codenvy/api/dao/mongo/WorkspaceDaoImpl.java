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
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.event.workspace.CreateWorkspaceEvent;
import com.codenvy.api.event.workspace.DeleteWorkspaceEvent;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.google.gson.Gson;
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
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link com.codenvy.api.dao.mongo.WorkspaceDaoImpl} based on MongoDB storage.
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
@Singleton
public class WorkspaceDaoImpl implements WorkspaceDao {
    private static final Logger  LOG           = LoggerFactory.getLogger(WorkspaceDaoImpl.class);
    /* should contain [3, 20] characters, first and last character is letter or digit, available characters {A-Za-z0-9.-_}*/
    private static final Pattern WS_NAME       = Pattern.compile("[\\w][\\w\\.\\-]{1,18}[\\w]");
    private static final String  DB_COLLECTION = "organization.storage.db.workspace.collection";

    private final DBCollection collection;
    private final Gson         gson;
    private final EventService eventService;

    @Inject
    public WorkspaceDaoImpl(Gson gson,
                            DB db,
                            EventService eventService,
                            @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        collection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        collection.ensureIndex(new BasicDBObject("accountId", 1));
        collection.ensureIndex(new BasicDBObject("name", 1));
        this.gson = gson;
        this.eventService = eventService;
    }

    @Override
    public void create(Workspace workspace) throws ConflictException, ServerException {
        try {
            validateWorkspaceName(workspace.getName());
            ensureWorkspaceNameDoesNotExist(workspace.getName());
            collection.save(toDBObject(workspace));
            eventService.publish(new CreateWorkspaceEvent(workspace.getId()));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void update(Workspace workspace) throws ConflictException, NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", workspace.getId());
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
        try {
            final DBObject workspace = collection.findAndRemove(new BasicDBObject("id", id));
            final Workspace removedWorkspace = fromDBObject(workspace);
            LOG.info("EVENT#workspace-destroyed# WS#{}# WS-ID#{}#", removedWorkspace.getName(), removedWorkspace.getId());
            eventService.publish(new DeleteWorkspaceEvent(id, removedWorkspace.isTemporary(), removedWorkspace.getName()));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public Workspace getById(String id) throws NotFoundException, ServerException {
        final DBObject res;
        try {
            res = collection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null) {
            throw new NotFoundException("Workspace not found " + id);
        }
        return fromDBObject(res);
    }

    @Override
    public Workspace getByName(String name) throws NotFoundException, ServerException {
        final DBObject res;
        try {
            res = collection.findOne(new BasicDBObject("name", name));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null) {
            throw new NotFoundException("Workspace not found " + name);
        }
        return fromDBObject(res);
    }

    @Override
    public List<Workspace> getByAccount(String accountId) throws ServerException {
        final List<Workspace> result;
        try (DBCursor cursor = collection.find(new BasicDBObject("accountId", accountId))) {
            result = new ArrayList<>(cursor.size());
            for (DBObject wsObj : cursor) {
                result.add(fromDBObject(wsObj));
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    //should be package-private (used in tests)
    Workspace fromDBObject(DBObject wsObj) {
        return gson.fromJson(wsObj.toString(), Workspace.class);
    }

    /**
     * Convert workspace to Database ready-to-use object,
     *
     * @param workspace
     *         Workspace to convert
     * @return DBObject
     */
    private DBObject toDBObject(Workspace workspace) {
        return (DBObject)JSON.parse(gson.toJson(workspace));
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
     * @throws com.codenvy.api.core.ConflictException
     */
    private void ensureWorkspaceNameDoesNotExist(String workspaceName) throws ConflictException {
        final DBObject res = collection.findOne(new BasicDBObject("name", workspaceName));
        if (res != null) {
            throw new ConflictException(String.format("Unable to create workspace: name '%s' already exists.", workspaceName));
        }
    }
}
