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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.text.ParseException;
import java.util.regex.Pattern;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractUsersProfile extends ReadBasedMetric {

    public AbstractUsersProfile(MetricType metricType) {
        super(metricType);
    }

    @Override
    public DBObject getFilter(Context clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        for (MetricFilter filter : clauses.getFilters()) {
            String[] values = clauses.getAsString(filter).split(",");

            if (filter == MetricFilter.USER) {
                match.put(ID, new BasicDBObject("$in", values));

            } else if (filter == MetricFilter.USER_COMPANY
                       || filter == MetricFilter.USER_FIRST_NAME
                       || filter == MetricFilter.USER_LAST_NAME) {
                StringBuilder builder = new StringBuilder();

                for (String value : clauses.getAsString(filter).split(",")) {
                    if (builder.length() > 0) {
                        builder.append("|");
                    }

                    builder.append(Pattern.quote(value));
                }

                Pattern pattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
                match.put(filter.toString().toLowerCase(), pattern);

            } else if (filter != MetricFilter.IDE) {
                match.put(filter.toString().toLowerCase(), new BasicDBObject("$in", values));
            }
        }

        return new BasicDBObject("$match", match);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        return new DBObject[0];
    }
}
