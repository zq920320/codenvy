/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractSum extends ReadBasedMetric {

    private final MetricType basedMetric;
    private final String     field;

    public AbstractSum(MetricType metricType, MetricType basedMetric, String field) {
        super(metricType);
        this.basedMetric = basedMetric;
        this.field = field;
    }

    @Override
    public String getStorageCollectionName() {
        return ((ReadBasedMetric)MetricFactory.getMetric(basedMetric)).getStorageCollectionName();
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{field};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
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
