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

import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractSetValueResulted extends ReadBasedMetric {

    private final String valueField;

    protected AbstractSetValueResulted(String metricName, String valueField) {
        super(metricName);
        this.valueField = valueField;
    }

    public AbstractSetValueResulted(MetricType metricType, String valueField) {
        this(metricType.toString(), valueField);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{valueField};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return SetValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(valueField, new BasicDBObject("$addToSet", "$" + valueField));

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$unwind", "$" + valueField)};
    }
}
