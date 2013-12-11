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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.regex.Pattern;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractProductUsageUsers extends ReadBasedMetric {

    private static final Pattern NON_ANONYMOUS_USER = Pattern.compile("^(?!ANONYMOUSUSER_).*", Pattern.CASE_INSENSITIVE);

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
    public String getStorageTable() {
        return "product_usage_sessions";
    }

    @Override
    public DBObject[] getDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put("_id", "$user");
        group.put("total", new BasicDBObject("$sum", "$value"));
        BasicDBObject opGroupBy = new BasicDBObject("$group", group);

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);
        BasicDBObject opHaving = new BasicDBObject("$match", new BasicDBObject("total", range));

        group = new BasicDBObject();
        group.put("_id", null);
        group.put("value", new BasicDBObject("$sum", 1));
        BasicDBObject opCount = new BasicDBObject("$group", group);

        return new DBObject[]{opGroupBy, opHaving, opCount};
    }

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException, IOException {
        DBObject filter = super.getFilter(clauses);

        DBObject match = (DBObject)filter.get("$match");
        if (match.get("user") == null) {
            match.put("user", NON_ANONYMOUS_USER);
        }

        return filter;
    }
}
