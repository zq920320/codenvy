/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.sessions.ProductUsageSessionsList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersTimeInWorkspacesList extends AbstractListValueResulted {

    public static final String WS       = "_id";
    public static final String SESSIONS = "sessions";
    public static final String TIME     = "time";

    public UsersTimeInWorkspacesList() {
        super(MetricType.USERS_TIME_IN_WORKSPACES_LIST);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{TIME, SESSIONS, WS};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put("_id", "$" + ProductUsageSessionsList.WS);
        group.put(TIME, new BasicDBObject("$sum", "$" + ProductUsageSessionsList.TIME));
        group.put(SESSIONS, new BasicDBObject("$sum", 1));
        BasicDBObject opCount = new BasicDBObject("$group", group);

        return new DBObject[]{opCount};
    }

    @Override
    public String getDescription() {
        return "How much time every user has spent in workspaces";
    }
}
