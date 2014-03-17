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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractActiveEntities extends ReadBasedMetric {

    private final ReadBasedMetric basedMetric;
    private final String          valueField;

    public AbstractActiveEntities(String metricName, String basedMetricName, String valueField) {
        super(metricName);
        this.basedMetric = (ReadBasedMetric)MetricFactory.getMetric(basedMetricName);
        this.valueField = valueField;
    }

    public AbstractActiveEntities(MetricType metricType, MetricType basedMetric, String valueField) {
        this(metricType.name(), basedMetric.name(), valueField);
    }

    public AbstractActiveEntities(String metricName, Metric basedMetric, String valueField) {
        super(metricName);
        this.basedMetric = (ReadBasedMetric)basedMetric;
        this.valueField = valueField;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{valueField};
    }

    @Override
    public DBObject getFilter(Context clauses) throws IOException, ParseException {
        return basedMetric.getFilter(clauses);
    }

    @Override
    public String getStorageCollectionName() {
        return basedMetric.getStorageCollectionName();
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + valueField);

        DBObject count = new BasicDBObject();
        count.put(ID, null);
        count.put(valueField, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$group", count)};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
