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
public abstract class AbstractProjectType extends ReadBasedMetric {

    private final String[] types;

    protected AbstractProjectType(String metricName, String[] types) {
        super(metricName);
        this.types = types;
    }

    protected AbstractProjectType(MetricType metricType, String[] types) {
        this(metricType.name(), types);
    }

    @Override
    public String getStorageTable() {
        return MetricType.PROJECT_TYPES.name().toLowerCase();
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();

        group.put("_id", null);
        for (String type : types) {
            String field = type.toLowerCase();
            group.put(field, new BasicDBObject("$sum", "$" + field));
        }

        return new DBObject[]{new BasicDBObject("$group", group)};
    }
}
