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

import com.codenvy.workspace.event.CreateWorkspaceEvent;
import com.codenvy.workspace.event.DeleteWorkspaceEvent;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.asMap;
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
    private final MemberDao    memberDao;

    @Inject
    public WorkspaceDaoImpl(@Named("mongo.db.organization") DB db,
                            MemberDao memberDao,
                            EventService eventService,
                            @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        collection.createIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        collection.createIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true));
        collection.createIndex(new BasicDBObject("accountId", 1));
        this.eventService = eventService;
        this.memberDao = memberDao;
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
        eventService.publish(new CreateWorkspaceEvent(workspace));
    }

    @Override
    public void update(Workspace update) throws ConflictException, NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", update.getId());
        final DBObject workspaceDocument = collection.findOne(query);
        if (workspaceDocument == null) {
            throw new NotFoundException(format("Workspace with id %s was not found ", update.getId()));
        }
        if (!update.getName().equals(workspaceDocument.get("name"))) {
            validateName(update.getName());
            checkNameAvailable(update.getName());
        }
        try {
            collection.update(query, toDBObject(update));
        } catch (MongoException ex) {
            throw new ServerException("It is not possible to update workspace", ex);
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
            throw new NotFoundException(format("Workspace with name %s was not found ", name));

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

    /**
     * Get all workspaces which are locked after RAM runner resources was exceeded.
     *
     * @return all locked workspaces
     */
    public List<Workspace> getWorkspacesWithLockedResources() throws ServerException {
        DBObject query = QueryBuilder.start("attributes").elemMatch(new BasicDBObject("name", Constants.RESOURCES_LOCKED_PROPERTY)).get();

        try (DBCursor accounts = collection.find(query)) {
            final ArrayList<Workspace> result = new ArrayList<>();
            for (DBObject accountObj : accounts) {
                result.add(toWorkspace(accountObj));
            }
            return result;
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve workspaces");
        }
    }

    @Override
    public void remove(String id) throws NotFoundException, ServerException, ConflictException {
        try {
            final DBObject query = new BasicDBObject("id", id);
            final DBObject workspaceDocument = collection.findOne(query);
            if (workspaceDocument == null) {
                throw new NotFoundException(format("Workspace with id %s was not found ", id));
            }
            //removing all workspace members
            for (Member member : memberDao.getWorkspaceMembers(id)) {
                memberDao.remove(member);
            }
            //removing workspace itself
            collection.remove(query);
            final Workspace removedWorkspace = toWorkspace(workspaceDocument);
            LOG.info("EVENT#workspace-destroyed# WS#{}# WS-ID#{}#", removedWorkspace.getName(), removedWorkspace.getId());
            eventService.publish(new DeleteWorkspaceEvent(removedWorkspace));
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
                              .withAttributes(asMap(basicWsObj.get("attributes")));
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
                                  .append("attributes", asDBList(workspace.getAttributes()));
    }

    private void validateName(String workspaceName) throws ConflictException {
        if (workspaceName == null) {
            throw new ConflictException("Workspace name required");
        }
        if (!WS_NAME.matcher(workspaceName).matches()) {
            throw new ConflictException("Incorrect workspace name, it should be between 3 to 20 characters and may contain digits, " +
                                        "latin letters, underscores, dots, dashes and should start and end only with digits, " +
                                        "latin letters or underscores");
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
