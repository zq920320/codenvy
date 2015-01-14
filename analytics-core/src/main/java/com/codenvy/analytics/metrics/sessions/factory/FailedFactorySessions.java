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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.List;

/** @author Alexander Reshetnyak */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS_ID, MetricFilter.PERSISTENT_WS})
public class FailedFactorySessions extends ReadBasedMetric implements ReadBasedExpandable {

    public FailedFactorySessions() {
        super(MetricType.FAILED_FACTORY_SESSIONS);
    }

    protected FailedFactorySessions(MetricType type) {
        super(type);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        DBObject match = new BasicDBObject();
        match.put(TIME, new BasicDBObject("$lte", 0));
        dbOperations.add(new BasicDBObject("$match", match));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + WS);
        dbOperations.add(new BasicDBObject("$group", group));

        group = new BasicDBObject();
        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));
        dbOperations.add(new BasicDBObject("$group", group));

        return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
    }

    @Override
    public String getDescription() {
        return "Failed factory sessions";
    }

    @Override
    public String getExpandedField() {
        return WS;
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context context) {
        List<DBObject> dbOperations = new ArrayList<>();

        DBObject match = new BasicDBObject();
        match.put(TIME, new BasicDBObject("$lte", 0));
        dbOperations.add(new BasicDBObject("$match", match));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + WS);
        dbOperations.add(new BasicDBObject("$group", group));

        DBObject projection = new BasicDBObject(getExpandedField(), "$" + ID);
        dbOperations.add(new BasicDBObject("$project", projection));

        return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }
}