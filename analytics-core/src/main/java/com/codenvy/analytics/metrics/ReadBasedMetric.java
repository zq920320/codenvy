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

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.storage.DataLoader;
import com.codenvy.analytics.storage.DataStorageContainer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    private static final long DAY_IN_MILLISECONDS = 86400000L;

    protected final DataLoader dataLoader;

    public ReadBasedMetric(String metricName) {
        super(metricName);
        this.dataLoader = DataStorageContainer.createDataLoader();
    }

    public ReadBasedMetric(MetricType metricType) {
        this(metricType.toString());
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return dataLoader.loadValue(this, context);
    }

    @Override
    public Set<Parameters> getParams() {
        return new HashSet<>(Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
    }

    // --------------------------------------------- storage related methods -------------

    /** Indicates if query result have to be aggregated before computation {@link ValueData} */
    public abstract boolean isAggregationSupport();

    /**
     * Returns aggregation rule. See mongoDB documentation for more information.
     *
     * @param clauses
     *         execution context
     * @return {@link DBObject}
     */
    public abstract DBObject getAggregator(Map<String, String> clauses);

    public String getStorage() {
        return getName().toLowerCase();
    }

    /**
     * Returns matching rule. See mongoDB documentation for more information.
     *
     * @param clauses
     *         execution context
     * @return {@link DBObject}
     */
    public DBObject getMatcher(Map<String, String> clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        DBObject range = new BasicDBObject();
        range.put("$gte", Utils.getFromDate(clauses).getTimeInMillis());
        range.put("$lt", Utils.getToDate(clauses).getTimeInMillis() + DAY_IN_MILLISECONDS);
        match.put("_id", range);

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values = filter.get(clauses).split(",");
            match.put(filter.name().toLowerCase(), new BasicDBObject("$in", values));
        }

        return new BasicDBObject("$match", match);
    }
}

