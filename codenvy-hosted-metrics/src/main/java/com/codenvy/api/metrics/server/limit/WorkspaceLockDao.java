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
package com.codenvy.api.metrics.server.limit;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceLockDao {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceLockDao.class);

    private static final String WORKSPACE_COLLECTION = "organization.storage.db.workspace.collection";

    private final DBCollection workspaceCollection;

    @Inject
    public WorkspaceLockDao(DB db,
                            @Named(WORKSPACE_COLLECTION) String workspaceCollectionName) {
        workspaceCollection = db.getCollection(workspaceCollectionName);
    }

    /**
     * Get all workspaces which are locked after RAM runner resources was exceeded.
     *
     * @return all locked workspaces
     */
    public List<Workspace> getWorkspacesWithLockedResources() throws ServerException {
        DBObject query = QueryBuilder.start("attributes").elemMatch(new BasicDBObject("name", Constants.RESOURCES_LOCKED_PROPERTY)).get();

        try (DBCursor accounts = workspaceCollection.find(query)) {
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
     * Converts database list to Map
     */
    public static Map<String, String> asMap(Object src) {
        if (!(src instanceof BasicDBList)) {
            throw new IllegalArgumentException("BasicDBList was expected");
        }
        final BasicDBList list = (BasicDBList)src;
        final Map<String, String> map = new HashMap<>();
        for (Object obj : list) {
            final BasicDBObject attribute = (BasicDBObject)obj;
            map.put(attribute.getString("name"), attribute.getString("value"));
        }
        return map;
    }
}
