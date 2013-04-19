/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ArrayDoubleValueManager;
import com.codenvy.analytics.scripts.DoubleValueManager;
import com.codenvy.analytics.scripts.LongValueManager;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ValueManager;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ValueFromMapMetric extends AbstractMetric {

    protected final String keyName;
    private final Metric   metric;
    private final ValueType valueType;

    ValueFromMapMetric(MetricType metricType, Metric metric, String keyName, ValueType valueType) {
        super(metricType);
        this.metric = metric;
        this.keyName = keyName;
        this.valueType = valueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getMandatoryParams() {
        return metric.getMandatoryParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getAdditionalParams() {
        return metric.getAdditionalParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object evaluateValue(Map<String, String> context) throws IOException {
        Map<String, Long> valueMetric = (Map<String, Long>)metric.getValue(context);

        switch (valueType) {
            case NUMBER:
                return getParticalValue(valueMetric);
            case PERCENT:
                return calculatePercent(valueMetric);
            case BOTH:
                return new Double[]{getParticalValue(valueMetric).doubleValue(), calculatePercent(valueMetric)};
        }

        throw new IllegalArgumentException("Unkonw type " + valueType);
    }

    protected Double calculatePercent(Map<String, Long> valueMetric) {
        Long sum = calculateSum(valueMetric);
        return Double.valueOf(100D * getParticalValue(valueMetric) / sum);
    }

    protected Long calculateSum(Map<String, Long> valueMetric) {
        Long sum = 0L;
        for (Long value : valueMetric.values()) {
            sum += value;
        }
        return sum;
    }

    protected Long getParticalValue(Map<String, Long> valueMetric) {
        return valueMetric.containsKey(keyName) ? valueMetric.get(keyName) : Long.valueOf(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueManager getValueManager() {
        switch (valueType) {
            case NUMBER:
                return new LongValueManager();
            case PERCENT:
                return new DoubleValueManager();
            case BOTH:
                return new ArrayDoubleValueManager();
        }

        throw new IllegalArgumentException("Unkonw type " + valueType);
    }

    public static enum ValueType {
        NUMBER,
        PERCENT,
        BOTH
    }
}
