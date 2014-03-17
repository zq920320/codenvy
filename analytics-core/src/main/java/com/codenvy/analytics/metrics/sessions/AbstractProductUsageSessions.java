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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractProductUsageSessions extends ReadBasedMetric {

    final private long    min;
    final private long    max;
    final private boolean includeMin;
    final private boolean includeMax;

    protected AbstractProductUsageSessions(String metricName,
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

    protected AbstractProductUsageSessions(MetricType metricType,
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
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();

        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$group", group)};
    }

    @Override
    public DBObject getFilter(Context clauses) throws ParseException, IOException {
        DBObject dbObject = super.getFilter(clauses);
        DBObject match = (DBObject)dbObject.get("$match");

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);
        match.put(TIME, range);

        return dbObject;
    }
}
