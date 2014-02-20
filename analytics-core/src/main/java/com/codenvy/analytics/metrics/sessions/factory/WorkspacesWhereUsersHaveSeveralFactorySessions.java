/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Alexander Reshetnyak */
@RolesAllowed({"system/admin", "system/manager"})
public class WorkspacesWhereUsersHaveSeveralFactorySessions extends ReadBasedMetric {

    public static final String COUNT = "count";

    public WorkspacesWhereUsersHaveSeveralFactorySessions() {
        super(MetricType.WORKSPACES_WHERE_USERS_HAVE_SEVERAL_FACTORY_SESSIONS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{COUNT};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        DBObject group = new BasicDBObject();
        Map<String, String> m = new HashMap<>();
        m.put("user", "$" + ProductUsageFactorySessionsList.USER);
        m.put("ws", "$" + ProductUsageFactorySessionsList.WS);
        group.put("_id", m);
        group.put("sum", new BasicDBObject("$sum", 1));
        dbOperations.add(new BasicDBObject("$group", group));

        DBObject match = new BasicDBObject();
        match.put("sum", new BasicDBObject( "$gte", 2));
        dbOperations.add(new BasicDBObject("$match", match));
        
        group = new BasicDBObject();
        group.put("_id", "$_id." + ProductUsageFactorySessionsList.WS);
        dbOperations.add(new BasicDBObject("$group", group));
        
        group = new BasicDBObject();
        group.put("_id", null);
        group.put(COUNT, new BasicDBObject("$sum", 1));
        dbOperations.add(new BasicDBObject("$group", group));

        return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }
    
    @Override
    public Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
    
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }

    @Override
    public String getDescription() {
        return "The workspaces count where users have several factory sessions";
    }
}
