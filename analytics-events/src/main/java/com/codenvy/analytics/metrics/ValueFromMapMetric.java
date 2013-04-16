/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.DoubleValueManager;
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
    private final boolean  percent;

    ValueFromMapMetric(MetricType metricType, Metric metric, String keyName, boolean percent) {
        super(metricType);
        this.metric = metric;
        this.keyName = keyName;
        this.percent = percent;
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

        if (percent) {
            return calculatePercent(valueMetric);
        } else {
            return getParticalValue(valueMetric);
        }
    }

    protected Double calculatePercent(Map<String, Long> valueMetric) {
        Long sum = 0L;
        for (Long value : valueMetric.values()) {
            sum += value;
        }
        
        return Double.valueOf(100D * getParticalValue(valueMetric) / sum);
    }

    protected Long getParticalValue(Map<String, Long> valueMetric) {
        return valueMetric.containsKey(keyName) ? valueMetric.get(keyName) : Long.valueOf(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueManager getValueManager() {
        return new DoubleValueManager();
    }
}
