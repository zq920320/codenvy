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

import com.codenvy.analytics.metrics.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.util.regex.Pattern;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractUsersProfile extends ReadBasedMetric {

    public AbstractUsersProfile(MetricType metricType) {
        super(metricType);
    }

    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.putIfNotNull(Parameters.PER_PAGE, clauses.getAsString(Parameters.PER_PAGE));
        builder.putIfNotNull(Parameters.PAGE, clauses.getAsString(Parameters.PAGE));
        builder.putIfNotNull(Parameters.SORT, clauses.getAsString(Parameters.SORT));

        for (MetricFilter filter : clauses.getFilters()) {
            Object value = clauses.get(filter);

            if (filter == MetricFilter.USER) {
                builder.put(MetricFilter._ID, processValue(value, filter.isNumericType()));

            } else if (filter == MetricFilter.USER_COMPANY
                       || filter == MetricFilter.USER_FIRST_NAME
                       || filter == MetricFilter.USER_LAST_NAME) {

                builder.put(filter, convertToPattern(value));
            }
        }

        return builder.build();
    }

    private Object convertToPattern(Object value) throws IOException {
        if (value instanceof Pattern) {
            return value;

        } else if (value instanceof Pattern[]) {
            return new BasicDBObject("$in", value);

        } else if (value instanceof String) {
            return processStringValue((String)value, false);

        } else if (value instanceof String[]) {
            return new BasicDBObject("$in", getPatterns((String[])value));

        } else {
            throw new IllegalArgumentException("Unsupported type " + value.getClass());
        }
    }

    @Override
    protected Object processStringValue(String value, boolean isNumericType) {
        boolean processExclusiveValues = value.startsWith(EXCLUDE_SIGN);
        if (processExclusiveValues) {
            value = value.substring(EXCLUDE_SIGN.length());
        }

        Pattern[] patterns = getPatterns(value.split(SEPARATOR));
        return new BasicDBObject(processExclusiveValues ? "$nin" : "$in", patterns);
    }

    private Pattern[] getPatterns(String[] values) {
        Pattern[] patterns = new Pattern[values.length];
        for (int i = 0; i < values.length; i++) {
            patterns[i] = Pattern.compile(Pattern.quote(values[i]), Pattern.CASE_INSENSITIVE);
        }
        return patterns;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        return new DBObject[0];
    }
}