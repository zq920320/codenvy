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

/**
 * Implementation of {@link WorkspaceDao} based on MongoDB storage.
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
        collection.ensureIndex(new BasicDBObject("accountId", 1));
        collection.ensureIndex(new BasicDBObject("name", 1));
        this.eventService = eventService;
    }

    @Override
    public void create(Workspace workspace) throws ConflictException, ServerException {
        try {
            validateWorkspaceName(workspace.getName());
            ensureWorkspaceNameDoesNotExist(workspace.getName());
            collection.save(toDBObject(workspace));
            eventService.publish(new CreateWorkspaceEvent(workspace.getId(), workspace.isTemporary()));
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
    public void remove(String id) throws ServerException, NotFoundException {
        try {
            final DBObject workspace = collection.findAndRemove(new BasicDBObject("id", id));
            final Workspace removedWorkspace = toWorkspace(workspace);
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
        return toWorkspace(res);
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
        return toWorkspace(res);
    }

    @Override
    public List<Workspace> getByAccount(String accountId) throws ServerException {
        final List<Workspace> result;
        try (DBCursor cursor = collection.find(new BasicDBObject("accountId", accountId))) {
            result = new ArrayList<>(cursor.size());
            for (DBObject wsObj : cursor) {
                result.add(toWorkspace(wsObj));
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    /**
     * Converts database object to workspace ready-to-use object
     */
    Workspace toWorkspace(DBObject wsObj) {
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
    private DBObject toDBObject(Workspace workspace) {
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
