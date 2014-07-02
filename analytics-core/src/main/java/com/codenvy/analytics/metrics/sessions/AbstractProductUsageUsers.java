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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractProductUsageUsers extends ReadBasedMetric implements ReadBasedExpandable {

    private final long    min;
    private final long    max;
    private final boolean includeMin;
    private final boolean includeMax;

    protected AbstractProductUsageUsers(String metricName,
                                        long min,
                                        long max,
                                        boolean includeMin,
                                        boolean includeMax) {
        super(metricName);
        this.min = min;
        this.max = max;
        this.includeMin = includeMin;
        this.includeMax = includeMax;
    }

    protected AbstractProductUsageUsers(MetricType metricType,
                                        long min,
                                        long max,
                                        boolean includeMin,
                                        boolean includeMax) {
        this(metricType.name(), min, max, includeMin, includeMax);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + USER);
        group.put("total", new BasicDBObject("$sum", "$" + TIME));

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);

        BasicDBObject count = new BasicDBObject();
        count.put("_id", null);
        count.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$match", new BasicDBObject("total", range)),
                              new BasicDBObject("$group", count)};
    }

    @Override
    public Context applySpecificFilter(Context clauses) {
        if (!clauses.exists(MetricFilter.USER)) {
            Context.Builder builder = new Context.Builder(clauses);
            builder.put(MetricFilter.USER, Parameters.USER_TYPES.REGISTERED.name());
            return builder.build();
        }

        return clauses;
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());
        group.put("total", new BasicDBObject("$sum", "$" + TIME));

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$match", new BasicDBObject("total", range)),
                              new BasicDBObject("$project", projection)};
    }

    @Override
    public String getExpandedField() {
        return USER;
    }
}
