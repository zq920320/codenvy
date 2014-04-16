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
import com.codenvy.analytics.metrics.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author Anatoliy Bazko */
public abstract class AbstractProductTime extends ReadBasedMetric {

    public AbstractProductTime(MetricType metricType) {
        super(metricType);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject();
        match.put(getTrackedFields()[0], new BasicDBObject("$ne", ""));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getTrackedFields()[0]);
        group.put(getTrackedFields()[1], new BasicDBObject("$sum", "$" + getTrackedFields()[1]));
        group.put(getTrackedFields()[2], new BasicDBObject("$sum", 1));

        DBObject project = new BasicDBObject();
        project.put(getTrackedFields()[0], "$_id");
        project.put(getTrackedFields()[1], "$" + getTrackedFields()[1]);
        project.put(getTrackedFields()[2], "$" + getTrackedFields()[2]);

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public Context applySpecificFilter(Context clauses) {
        if (!clauses.exists(MetricFilter.USER)) {
            Context.Builder builder = new Context.Builder(clauses);
            builder.put(MetricFilter.USER, Parameters.USER_TYPES.REGISTERED.name());
            return builder.build();
        }

        return clauses;
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
