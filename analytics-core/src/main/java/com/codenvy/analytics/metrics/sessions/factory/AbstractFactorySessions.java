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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractFactorySessions extends ReadBasedMetric implements ReadBasedExpandable {

    final private long    min;
    final private long    max;
    final private boolean includeMin;
    final private boolean includeMax;

    protected AbstractFactorySessions(String metricName,
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

    protected AbstractFactorySessions(MetricType metricType,
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
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();

        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$group", group)};
    }

    @Override
    public Context applySpecificFilter(Context clauses) {
        Context.Builder builder = new Context.Builder(clauses);

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);

        builder.put(MetricFilter.TIME, range);
        return builder.build();
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    @Override
    public String getExpandedField() {
        return SESSION_ID;
    }
}
