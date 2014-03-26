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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.Utils;

import java.text.ParseException;
import java.util.*;

/**
 * Unmodifiable execution context.
 *
 * @author Anatoliy Bazko
 */
public class Context {
    private Map<String, String> params;

    private Context(Map<String, String> params) {
        this.params = params;
    }

    public static Context valueOf(Map<String, String> params) {
        Builder builder = new Builder(params);
        return builder.build();
    }

    /** Returns all filters existed in context. */
    public Set<MetricFilter> getFilters() {
        Set<MetricFilter> result = new HashSet<>();

        for (MetricFilter filter : MetricFilter.values()) {
            if (exists(filter)) {
                result.add(filter);
            }
        }

        return result;
    }


    public boolean isSimplified() {
        return !exists(Parameters.SORT) && !exists(Parameters.PAGE) && getFilters().isEmpty();
    }

    public Calendar getAsDate(Parameters key) throws ParseException {
        return Utils.parseDate(params.get(key.name()));
    }

    public long getAsLong(Parameters key) {
        return Long.valueOf(params.get(key.name()));
    }

    public Parameters.TimeUnit getTimeUnit() {
        return Parameters.TimeUnit.valueOf(get(Parameters.TIME_UNIT).toUpperCase());
    }

    public String get(Parameters key) {
        return params.get(key.name());
    }

    public String get(MetricFilter key) {
        return params.get(key.name());
    }

    public boolean exists(Parameters key) {
        return params.containsKey(key.name());
    }

    public boolean exists(MetricFilter key) {
        return params.containsKey(key.name());
    }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(params);
    }

    public Context cloneAndPut(MetricFilter param, String value) {
        Builder builder = new Builder(params);
        builder.put(param, value);

        return builder.build();
    }

    public Context cloneAndPut(Parameters param, String value) {
        Builder builder = new Builder(params);
        builder.put(param, value);

        return builder.build();
    }

    public Context cloneAndPut(Parameters param, Calendar value) {
        return cloneAndPut(param, Utils.formatDate(value));
    }

    public Context cloneAndPut(Parameters param, long value) {
        return cloneAndPut(param, Long.toString(value));
    }

    @Override
    public String toString() {
        return params.toString();
    }

    public static class Builder {
        private Map<String, String> params = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder(Context context) {
            this.params.putAll(context.params);
        }

        public Builder(Map<String, String> params) {
            this.params.putAll(params);
        }

        public Builder putIfAbsent(Parameters param, String value) {
            if (!exists(param)) {
                params.put(param.name(), value);
            }
            return this;
        }

        public Builder put(Parameters param, String value) {
            params.put(param.name(), value);
            return this;
        }

        public Builder put(Parameters param, Calendar value) {
            params.put(param.name(), Utils.formatDate(value));
            return this;
        }

        public boolean exists(Parameters param) {
            return params.containsKey(param.name());
        }

        public Builder remove(Parameters param) {
            params.remove(param.name());
            return this;
        }

        public String get(Parameters param) {
            return params.get(param.name());
        }

        public Calendar getAsDate(Parameters key) throws ParseException {
            return Utils.parseDate(params.get(key.name()));
        }

        public Parameters.TimeUnit getTimeUnit() {
            return Parameters.TimeUnit.valueOf(get(Parameters.TIME_UNIT).toUpperCase());
        }

        public Builder put(MetricFilter param, String value) {
            params.put(param.name(), value);
            return this;
        }

        public Builder put(MetricFilter param, long value) {
            return put(param, Long.toString(value));
        }

        public Builder remove(MetricFilter param) {
            params.remove(param.name());
            return this;
        }

        public boolean exists(MetricFilter param) {
            return params.containsKey(param.name());
        }

        public String get(MetricFilter param) {
            return params.get(param.name());
        }

        public Builder put(Parameters param, long value) {
            return put(param, Long.toString(value));
        }

        public Builder putDefaultValue(Parameters param) {
            params.put(param.name(), param.getDefaultValue());
            return this;
        }

        public Builder putAll(Context context) {
            this.params.putAll(context.params);
            return this;
        }

        public Builder put(Parameters.TimeUnit timeUnit) {
            return put(Parameters.TIME_UNIT, timeUnit.name());
        }

        public Context build() {
            return new Context(params);
        }

    }
}
