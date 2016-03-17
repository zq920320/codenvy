/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
public abstract class AbstractActiveEntities extends ReadBasedMetric implements ReadBasedExpandable {

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
    public Context applySpecificFilter(Context clauses) throws IOException {
        return basedMetric.applySpecificFilter(clauses);
    }

    @Override
    public String getStorageCollectionName() {
        return basedMetric.getStorageCollectionName();
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject();
        match.put(valueField, new BasicDBObject("$nin", new Object[]{"", null}));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + valueField);

        DBObject count = new BasicDBObject();
        count.put(ID, null);
        count.put(valueField, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$group", count)};
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject match = new BasicDBObject();
        match.put(getExpandedField(), new BasicDBObject("$nin", new Object[]{"", null}));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getExpandedField() {
        return valueField;
    }
}
