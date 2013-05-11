/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.codenvy.analytics.metrics.value.ValueData;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractMetric implements Metric {

    protected final MetricType       metricType;

    AbstractMetric(MetricType metricType) {
        this.metricType = metricType;
    }

    /** {@inheritedDoc} */
    @Override
    public MetricType getType() {
        return metricType;
    }


    /** Preparation unique sequences to identify stored value. */
    protected LinkedHashMap<String, String> makeUUID(Map<String, String> context) throws IOException {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();

        for (MetricParameter param : getParams()) {
            String paramKey = param.getName();
            String paramValue = context.get(paramKey);

            if (paramValue == null) {
                throw new IOException("There is no parameter " + paramKey + " in context");
            }

            keys.put(paramKey, paramValue);
        }

        return keys;
    }

    /** @return what data type is represented in result */
    protected abstract Class< ? extends ValueData> getValueDataClass();

    /** Evaluating */
    protected abstract ValueData evaluate(Map<String, String> context) throws IOException;
}
