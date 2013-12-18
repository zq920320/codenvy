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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.text.ParseException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractUsersProfile extends ReadBasedMetric {

    public AbstractUsersProfile(MetricType metricType) {
        super(metricType);
    }

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values = filter.get(clauses).split(",");
            String key = filter == MetricFilter.USER ? "_id" : filter.name().toLowerCase();

            match.put(key, new BasicDBObject("$in", values));
        }

        return new BasicDBObject("$match", match);
    }

    @Override
    public boolean isSingleTable() {
        return true;
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        return new DBObject[0];
    }
}
