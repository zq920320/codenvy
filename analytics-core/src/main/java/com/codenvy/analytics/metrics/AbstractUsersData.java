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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.text.ParseException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractUsersData extends ReadBasedMetric {

    public AbstractUsersData(MetricType metricType) {
        super(metricType);
    }

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        if (MetricFilter.USER.exists(clauses)) {
            String[] values = MetricFilter.USER.get(clauses).split(",");
            match.put("_id", new BasicDBObject("$in", values));
        }

        return new BasicDBObject("$match", match);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
