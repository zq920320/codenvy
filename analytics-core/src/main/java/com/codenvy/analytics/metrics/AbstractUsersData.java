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

import com.codenvy.analytics.Utils;
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

    // TODO

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values = filter.get(clauses).split(",");
            String key = getFilterKey(filter);

            match.put(key, new BasicDBObject("$in", values));
        }

        return new BasicDBObject("$match", match);
    }

    private String getFilterKey(MetricFilter filter) {
        switch (filter) {
            case USER:
                return "_id";
            case COMPANY:
                return "user_company";
            default:
                return filter.name().toLowerCase();
        }
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
