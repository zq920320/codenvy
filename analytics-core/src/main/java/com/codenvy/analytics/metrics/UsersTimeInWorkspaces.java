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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.storage.MongoDataLoader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersTimeInWorkspaces extends ReadBasedMetric {

    public static final String SESSIONS = "sessions";
    public static final String TIME     = "time";

    public UsersTimeInWorkspaces() {
        super(MetricType.USERS_TIME_IN_WORKSPACES);
    }

    @Override
    public String getStorageTable() {
        return MetricType.PRODUCT_USAGE_SESSIONS.name().toLowerCase() + MongoDataLoader.EXT_COLLECTION_NAME_SUFFIX;
    }

    @Override
    public boolean isSingleTable() {
        return true;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put("_id", "$ws");
        group.put("time", new BasicDBObject("$sum", "$value"));
        group.put("sessions", new BasicDBObject("$sum", 1));
        BasicDBObject opCount = new BasicDBObject("$group", group);

        return new DBObject[]{opCount};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public String getDescription() {
        return "How much time every user has spent in workspaces";
    }
}
