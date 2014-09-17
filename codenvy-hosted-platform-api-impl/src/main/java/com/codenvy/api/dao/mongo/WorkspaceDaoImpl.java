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
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.workspace.event.CreateWorkspaceEvent;
import com.codenvy.workspace.event.DeleteWorkspaceEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Implementation of {@link WorkspaceDao} based on MongoDB storage.
 * <pre>
 * Workspace collection document scheme:
 *
 * {
 *      "id" : "workspaceId...",
 *      "accountId" : "accountId...",
 *      "name" : "name...",
 *      "temporary" : false | true,
 *      "attributes": [
 *          ...
 *          {
 *              "name" : "key...",
 *              "value" : "value..."
 *          }
 *          ...
 *      ]
 * }
 * </pre>
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
    private final EventService eventService;

    @Inject
    public WorkspaceDaoImpl(DB db,
                            EventService eventService,
                            @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        collection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        collection.ensureIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true));
        collection.ensureIndex(new BasicDBObject("accountId", 1));
        this.eventService = eventService;
    }

    @Override
    public void create(Workspace workspace) throws ConflictException, ServerException {
        validateName(workspace.getName());
        checkNameAvailable(workspace.getName());
        checkIdAvailable(workspace.getId());
        try {
            collection.save(toDBObject(workspace));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to create workspace");
        }
        eventService.publish(new CreateWorkspaceEvent(workspace.getId(), workspace.isTemporary()));
    }

    @Override
    public void update(Workspace update) throws ConflictException, NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", update.getId());
        final DBObject workspaceDocument = collection.findOne(query);
        if (workspaceDocument == null) {
            throw new NotFoundException("Workspace not found " + update.getId());
        }
        if (!update.getName().equals(workspaceDocument.get("name"))) {
            validateName(update.getName());
            checkNameAvailable(update.getName());
        }
        try {
            collection.update(query, toDBObject(update));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to update workspace");
        }
    }

    @Override
    public Workspace getById(String id) throws NotFoundException, ServerException {
        final DBObject workspaceDocument;
        try {
            workspaceDocument = collection.findOne(new BasicDBObject("id", id));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to retrieve workspace");
        }
        if (workspaceDocument == null) {
            throw new NotFoundException(format("Workspace with id %s not found", id));
        }
        return toWorkspace(workspaceDocument);
    }

    @Override
    public Workspace getByName(String name) throws NotFoundException, ServerException {
        final DBObject workspaceDocument;
        try {
            workspaceDocument = collection.findOne(new BasicDBObject("name", name));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to retrieve workspace");
        }
        if (workspaceDocument == null) {
            throw new NotFoundException("Workspace not found " + name);
        }
        return toWorkspace(workspaceDocument);
    }

    @Override
    public List<Workspace> getByAccount(String accountId) throws ServerException {
        final List<Workspace> workspaces;
        try (DBCursor workspacesCursor = collection.find(new BasicDBObject("accountId", accountId))) {
            workspaces = new ArrayList<>(workspacesCursor.size());
            for (DBObject dbWorkspace : workspacesCursor) {
                workspaces.add(toWorkspace(dbWorkspace));
            }
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to retrieve workspaces");
        }
        return workspaces;
    }

    @Override
    public void remove(String id) throws ServerException {
        try {
            final DBObject workspaceDocument = collection.findAndRemove(new BasicDBObject("id", id));
            final Workspace removedWorkspace = toWorkspace(workspaceDocument);
            LOG.info("EVENT#workspace-destroyed# WS#{}# WS-ID#{}#", removedWorkspace.getName(), removedWorkspace.getId());
            eventService.publish(new DeleteWorkspaceEvent(id, removedWorkspace.isTemporary(), removedWorkspace.getName()));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to remove workspace");
        }
    }

    /**
     * Converts database object to workspace ready-to-use object
     */
    /*used in tests*/Workspace toWorkspace(DBObject wsObj) {
        final BasicDBObject basicWsObj = (BasicDBObject)wsObj;
        return new Workspace().withId(basicWsObj.getString("id"))
                              .withName(basicWsObj.getString("name"))
                              .withAccountId(basicWsObj.getString("accountId"))
                              .withTemporary(basicWsObj.getBoolean("temporary"))
                              .withAttributes(toAttributes((BasicDBList)basicWsObj.get("attributes")));
    }

    /**
     * Converts workspace to database ready-to-use object.
     * Each workspace attribute may have name with "dot", but mongo doesn't support
     * keys with "dot" so we need to save attributes not as "one object", but as list of Objects with
     * structure:
     * <pre>
     *     {
     *        "name" : "attributeName",
     *        "value" : "attributeValue"
     *     }
     * </pre>
     *
     * @param workspace
     *         Workspace to convert
     * @return DBObject
     */
    /*used in tests*/DBObject toDBObject(Workspace workspace) {
        return new BasicDBObject().append("id", workspace.getId())
                                  .append("name", workspace.getName())
                                  .append("accountId", workspace.getAccountId())
                                  .append("temporary", workspace.isTemporary())
                                  .append("attributes", toDBList(workspace.getAttributes()));
    }

    private Map<String, String> toAttributes(BasicDBList list) {
        final Map<String, String> attributes = new HashMap<>();
        if (list != null) {
            for (Object obj : list) {
                final BasicDBObject attribute = (BasicDBObject)obj;
                attributes.put(attribute.getString("name"), attribute.getString("value"));
            }
        }
        return attributes;
    }

    private BasicDBList toDBList(Map<String, String> attributes) {
        final BasicDBList list = new BasicDBList();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            list.add(new BasicDBObject().append("name", entry.getKey())
                                        .append("value", entry.getValue()));
        }
        return list;
    }

    private void validateName(String workspaceName) throws ConflictException {
        if (workspaceName == null) {
            throw new ConflictException("Workspace name required");
        }
        if (!WS_NAME.matcher(workspaceName).matches()) {
            throw new ConflictException("Incorrect workspace name");
        }
    }

    private void checkIdAvailable(String id) throws ConflictException {
        if (collection.findOne(new BasicDBObject("id", id)) != null) {
            throw new ConflictException(format("Workspace with id '%s' already exists", id));
        }
    }

    private void checkNameAvailable(String name) throws ConflictException {
        if (collection.findOne(new BasicDBObject("name", name)) != null) {
            throw new ConflictException(format("Workspace with name '%s' already exists", name));
        }
    }
}
