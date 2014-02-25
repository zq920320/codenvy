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
public abstract class AbstractSum extends ReadBasedMetric {

    public static final String VALUE = "value";

    private final String basedMetricName;
    private final String field;

    protected AbstractSum(String metricName, String basedMetricName, String field) {
        super(metricName);
        this.basedMetricName = basedMetricName;
        this.field = field;
    }

    public AbstractSum(MetricType metricType, MetricType basedMetric, String field) {
        this(metricType.name(), basedMetric.name(), field);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(basedMetricName);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{field};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(field, new BasicDBObject("$sum", "$" + field));

        return new DBObject[]{new BasicDBObject("$group", group)};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
