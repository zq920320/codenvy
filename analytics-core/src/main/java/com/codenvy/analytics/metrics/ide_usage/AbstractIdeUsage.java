/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.ide_usage;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author Alexander Reshetnyak */
public abstract class AbstractIdeUsage extends ReadBasedMetric {

    private final String[] types;

    protected AbstractIdeUsage(String metricName, String[] types) {
        super(metricName);

        for (int i = 0; i < types.length; i++) {
            types[i] = types[i].toLowerCase();
        }
        this.types = types;
    }

    protected AbstractIdeUsage(MetricType metricType, String[] types) {
        this(metricType.name(), types);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.IDE_USAGES);
    }

    @Override
    public String[] getTrackedFields() {
        return types;
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();

        group.put(ID, null);
        for (String type : types) {
            group.put(type, new BasicDBObject("$sum", "$" + type));
        }

        return new DBObject[]{new BasicDBObject("$group", group)};
    }
}