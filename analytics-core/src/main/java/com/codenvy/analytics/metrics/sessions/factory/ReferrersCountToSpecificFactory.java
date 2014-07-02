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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.List;

/** @author Alexander Reshetnyak */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class ReferrersCountToSpecificFactory extends ReadBasedMetric {

    public static final String UNIQUE_REFERRERS_COUNT = "unique_referrers_count";

    public ReferrersCountToSpecificFactory() {
        super(MetricType.REFERRERS_COUNT_TO_SPECIFIC_FACTORY);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{FACTORY, UNIQUE_REFERRERS_COUNT};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + FACTORY);
        group.put("referrers", new BasicDBObject("$addToSet", "$" + REFERRER));
        dbOperations.add(new BasicDBObject("$group", group));

        dbOperations.add(new BasicDBObject("$unwind", "$referrers"));

        group = new BasicDBObject();
        group.put(ID, "$_id");
        group.put(UNIQUE_REFERRERS_COUNT, new BasicDBObject("$sum", 1));
        dbOperations.add(new BasicDBObject("$group", group));

        DBObject project = new BasicDBObject();
        project.put(FACTORY, "$_id");
        project.put(UNIQUE_REFERRERS_COUNT, 1);
        dbOperations.add(new BasicDBObject("$project", project));

        return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public String getDescription() {
        return "The referrers count to a specific factory";
    }
}
