/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
abstract class PercentMetric extends CalculateBasedMetric {

    private final Metric  totalMetric;
    private final Metric  particalMetric;
    private final boolean residual;

    PercentMetric(MetricType metricType, Metric totalMetric, Metric particalMetric, boolean residual) {
        super(metricType);

        this.totalMetric = totalMetric;
        this.particalMetric = particalMetric;
        this.residual = residual;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<MetricParameter> getParams() {
        Set<MetricParameter> params = (Set<MetricParameter>)((HashSet<MetricParameter>)particalMetric.getParams()).clone();
        params.addAll(totalMetric.getParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        LongValueData totalValue = (LongValueData)totalMetric.getValue(context);
        LongValueData particalValue = (LongValueData)particalMetric.getValue(context);

        double percent = 100D * particalValue.getAsLong() / totalValue.getAsLong();

        if (!residual) {
            return new DoubleValueData(percent);
        } else {
            return new DoubleValueData(100 - percent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }
}
