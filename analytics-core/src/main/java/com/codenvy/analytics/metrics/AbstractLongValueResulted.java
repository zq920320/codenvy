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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractLongValueResulted extends ReadBasedMetric {

    private String expandingField;

    protected AbstractLongValueResulted(String metricName) {
        super(metricName);
    }

    public AbstractLongValueResulted(MetricType metricType) {
        super(metricType);
    }

    public AbstractLongValueResulted(MetricType metricType, String expandindField) {
        super(metricType);
        this.expandingField = expandindField;
    }
    
    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();

        String countingField = getTrackedFields()[0];

        group.put(ID, null);
        group.put(countingField, new BasicDBObject("$sum", "$" + countingField));

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
    public String getExpandedValueField() {
        return expandingField;
    }

}