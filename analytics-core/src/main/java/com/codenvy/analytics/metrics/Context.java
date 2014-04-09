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

    public static final Context EMPTY = new Context(Collections.<String, Object>emptyMap());

    private Map<String, Object> params;

    private Context(Map<String, Object> params) {
        this.params = params;
    }

    public static Context valueOf(Map<String, Object> params) {
        Builder builder = new Builder(params);
        return builder.build();
    }

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

    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(params);
    }

    public Map<String, String> getAllAsString() {
        Map<String, String> result = new HashMap<>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());
        }

        return result;
    }

    public Calendar getAsDate(Parameters key) throws ParseException {
        return Utils.parseDate(getAsString(key));
    }

    public long getAsLong(Parameters key) {
        return Long.valueOf(getAsString(key));
    }

    public Parameters.TimeUnit getTimeUnit() {
        return Parameters.TimeUnit.valueOf(getAsString(Parameters.TIME_UNIT).toUpperCase());
    }

    public String getAsString(Parameters key) {
        Object object = params.get(key.name());
        return object == null ? null : object.toString();
    }

    public String getAsString(MetricFilter key) {
        Object object = params.get(key.name());
        return object == null ? null : object.toString();
    }

    public Object get(MetricFilter key) {
        return params.get(key.name());
    }

    public boolean exists(Parameters key) {
        return params.containsKey(key.name());
    }

    public boolean exists(MetricFilter key) {
        return params.containsKey(key.name());
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

    /**
     * Context builder.
     */
    public static class Builder {
        private Map<String, Object> params = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder(Context context) {
            this.params.putAll(context.params);
        }

        public Builder(Map<String, Object> params) {
            this.params.putAll(params);
        }

        public Builder putIfAbsent(Parameters param, String value) {
            if (!exists(param)) {
                params.put(param.name(), value);
            }
            return this;
        }

        public Builder putAll(Context context) {
            this.params.putAll(context.params);
            return this;
        }

        public Builder putDefaultValue(Parameters param) {
            params.put(param.name(), param.getDefaultValue());
            return this;
        }

        public Builder put(Parameters param, String value) {
            params.put(param.name(), value);
            return this;
        }

        public Builder put(MetricFilter param, String value) {
            params.put(param.name(), value);
            return this;
        }

        public Builder put(MetricFilter param, Object value) {
            params.put(param.name(), value);
            return this;
        }

        public Builder put(Parameters param, Calendar value) {
            params.put(param.name(), Utils.formatDate(value));
            return this;
        }

        public Builder put(MetricFilter param, long value) {
            return put(param, Long.toString(value));
        }

        public Builder put(Parameters param, long value) {
            return put(param, Long.toString(value));
        }

        public Builder put(Parameters.TimeUnit timeUnit) {
            return put(Parameters.TIME_UNIT, timeUnit.name());
        }

        public boolean exists(Parameters param) {
            return params.containsKey(param.name());
        }

        public boolean exists(MetricFilter param) {
            return params.containsKey(param.name());
        }

        public Builder remove(Parameters param) {
            params.remove(param.name());
            return this;
        }

        public Builder remove(MetricFilter param) {
            params.remove(param.name());
            return this;
        }

        public String getAsString(MetricFilter param) {
            Object object = params.get(param.name());
            return object == null ? null : object.toString();
        }

        public String getAsString(Parameters param) {
            Object object = params.get(param.name());
            return object == null ? null : object.toString();
        }

        public Calendar getAsDate(Parameters key) throws ParseException {
            return Utils.parseDate(getAsString(key));
        }

        public Parameters.TimeUnit getTimeUnit() {
            return Parameters.TimeUnit.valueOf(getAsString(Parameters.TIME_UNIT).toUpperCase());
        }

        public Context build() {
            return new Context(params);
        }
    }
}
