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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AggregatedResultMetric extends ReadBasedMetric {

    protected AggregatedResultMetric(String metricName) {
        super(metricName);
    }

    public AggregatedResultMetric(MetricType metricType) {
        super(metricType);
    }

    @Override
    public boolean isAggregationSupport() {
        return true;
    }

    @Override
    public DBObject getAggregator(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();

        group.put("_id", null);
        group.put("value", new BasicDBObject("$sum", "$value"));

        return new BasicDBObject("$group", group);
    }
}
