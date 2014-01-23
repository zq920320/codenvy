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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author Alexander Reshetnyak */
public class ReferrersCountToSpecificFactory extends ReadBasedMetric {

    public static final String UNIQUE_REFERRERS_COUNT = "unique_referrers_count";

    public ReferrersCountToSpecificFactory() {
        super(MetricType.REFERRERS_COUNT_TO_SPECIFIC_FACTORY);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{ProductUsageFactorySessionsList.FACTORY,
                            UNIQUE_REFERRERS_COUNT,
        };
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        DBObject group = new BasicDBObject();
        group.put("_id", "$" + ProductUsageFactorySessionsList.FACTORY);
        group.put("referrers", new BasicDBObject("$addToSet", "$" + ProductUsageFactorySessionsList.REFERRER));
        dbOperations.add(new BasicDBObject("$group", group));

        dbOperations.add(new BasicDBObject("$unwind", "$referrers"));

        group = new BasicDBObject();
        group.put("_id", "$_id");
        group.put(UNIQUE_REFERRERS_COUNT, new BasicDBObject("$sum", 1));
        dbOperations.add(new BasicDBObject("$group", group));

        DBObject project = new BasicDBObject();
        project.put(ProductUsageFactorySessionsList.FACTORY, "$_id");
        project.put(UNIQUE_REFERRERS_COUNT, 1);
        dbOperations.add(new BasicDBObject("$project", project));

        return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public String getDescription() {
        return "The unique referrers count by factory";
    }
}
