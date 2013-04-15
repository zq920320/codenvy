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
abstract class PercentMetric extends AbstractMetric {
    private final Metric baseMetric;
    private final Metric relativeMetric;
    private final boolean residual;

    PercentMetric(MetricType metricType, Metric baseMetric, Metric relativeMetric, boolean residual) {
        super(metricType);

        this.baseMetric = baseMetric;
        this.relativeMetric = relativeMetric;
        this.residual = residual;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getMandatoryParams() {
        Set<ScriptParameters> params = baseMetric.getMandatoryParams();
        params.addAll(relativeMetric.getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getAdditionalParams() {
        Set<ScriptParameters> params = baseMetric.getAdditionalParams();
        params.addAll(relativeMetric.getAdditionalParams());
        params.removeAll(getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Double evaluateValue(Map<String, String> context) throws IOException {
        if (!residual) {
            return new Double(100 * (Long)relativeMetric.getValue(context) / (Long)baseMetric.getValue(context));
        } else {
            return new Double(100 - 100 * (Long)relativeMetric.getValue(context) / (Long)baseMetric.getValue(context));
        }
    }

    /**
     * @return
     */
    @Override
    protected ValueManager getValueManager() {
        return new DoubleValueManager();
    }
}
