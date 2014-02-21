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

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractActiveEntities extends ReadBasedMetric {

    private final String valueField;
    private final String basedMetricName;

    public AbstractActiveEntities(String metricName, String basedMetricName, String valueField) {
        super(metricName);
        this.basedMetricName = basedMetricName;
        this.valueField = valueField;
    }

    protected AbstractActiveEntities(MetricType metricType, MetricType basedMetric, String valueField) {
        this(metricType.name(), basedMetric.name(), valueField);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{valueField};
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(basedMetricName);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put("_id", "$" + valueField);
        BasicDBObject opGroupBy = new BasicDBObject("$group", group);

        group = new BasicDBObject();
        group.put("_id", null);
        group.put(valueField, new BasicDBObject("$sum", 1));
        BasicDBObject opCount = new BasicDBObject("$group", group);

        return new DBObject[]{opGroupBy, opCount};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
