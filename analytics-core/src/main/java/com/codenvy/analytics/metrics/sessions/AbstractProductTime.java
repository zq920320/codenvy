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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Map;

/** @author Anatoliy Bazko */
public abstract class AbstractProductTime extends ReadBasedMetric {

    public static final String SESSIONS = "sessions";

    public AbstractProductTime(MetricType metricType) {
        super(metricType);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put("_id", "$" + getTrackedFields()[0]);
        group.put(getTrackedFields()[1], new BasicDBObject("$sum", "$" + getTrackedFields()[1]));
        group.put(getTrackedFields()[2], new BasicDBObject("$sum", 1));

        DBObject project = new BasicDBObject();
        project.put(getTrackedFields()[0], "$_id");
        project.put(getTrackedFields()[1], "$" + getTrackedFields()[1]);
        project.put(getTrackedFields()[2], "$" + getTrackedFields()[2]);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
