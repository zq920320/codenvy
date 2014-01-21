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
package com.codenvy.analytics.pig;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unmodifiable execution context.
 *
 * @author Anatoliy Bazko
 */
public class Context {
    private Map<String, String> context;

    private Context(Map<String, String> context) {
        this.context = context;
    }

    public String get(Parameters key) {
        return context.get(key.name());
    }

    public String get(MetricFilter key) {
        return context.get(key.name());
    }

    public boolean exists(Parameters key) {
        return context.containsKey(key.name());
    }

    public boolean exists(MetricFilter key) {
        return context.containsKey(key.name());
    }

    public Context cloneAndRemove(Parameters param) {
        Builder builder = new Builder();
        builder.putAll(context);
        builder.remove(param);

        return builder.build();
    }

    public Context cloneAndPut(Parameters param, String value) {
        Builder builder = new Builder();
        builder.putAll(context);
        builder.put(param, value);

        return builder.build();
    }

    public Context cloneAndPut(MetricFilter param, String value) {
        Builder builder = new Builder();
        builder.putAll(context);
        builder.put(param, value);

        return builder.build();
    }

    public Context clone() {
        Builder builder = new Builder();
        builder.putAll(context);

        return builder.build();
    }

    public static class Builder {
        private Map<String, String> context = new LinkedHashMap<>();

        public Builder put(Parameters param, String value) {
            context.put(param.name(), value);
            return this;
        }

        public Builder putDefaultValue(Parameters param) {
            context.put(param.name(), param.getDefaultValue());
            return this;
        }

        public Builder remove(Parameters param) {
            context.remove(param.name());
            return this;
        }

        public Builder put(MetricFilter param, String value) {
            context.put(param.name(), value);
            return this;
        }

        public Builder putAll(Map<String, String> context) {
            context.putAll(context);
            return this;
        }

        public Context build() {
            return new Context(context);
        }
    }
}
