/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class CalculateBasedMetric extends AbstractMetric {

    CalculateBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritedDoc} */
    public synchronized ValueData getValue(Map<String, String> context) throws IOException {
        return evaluate(context);
    }
}
