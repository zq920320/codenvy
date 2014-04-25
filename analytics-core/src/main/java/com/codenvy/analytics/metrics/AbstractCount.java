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
public abstract class AbstractCount extends ReadBasedMetric implements Expandable {

    private final ReadBasedMetric basedMetric;

    private String expandingField;

    public AbstractCount(String metricName, String basedMetricName) {
        super(metricName);
        this.basedMetric = (ReadBasedMetric)MetricFactory.getMetric(basedMetricName);
    }

    public AbstractCount(MetricType metricType, MetricType basedMetric) {
        this(metricType.name(), basedMetric.name());
    }

    public AbstractCount(MetricType metricType, MetricType basedMetric, String expandindField) {
        this(metricType.name(), basedMetric.name());
        this.expandingField = expandindField;
    }

    public AbstractCount(String metricName, Metric basedMetric) {
        super(metricName);
        this.basedMetric = (ReadBasedMetric)basedMetric;
    }

    @Override
    public String getStorageCollectionName() {
        return basedMetric.getStorageCollectionName();
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public DBObject getFilter(Context clauses) throws IOException, ParseException {
        return basedMetric.getFilter(clauses);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$group", group)};
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + expandingField);

        DBObject projection = new BasicDBObject(expandingField, "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getExpandedValueField() {
        return expandingField;
    }
}
