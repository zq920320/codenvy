/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class PercentUserSsoLoggedInMetric extends ValueFromMapMetric {

    PercentUserSsoLoggedInMetric(MetricType metricType, Metric metric, String keyName, boolean percent) {
        super(metricType, metric, keyName, percent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long calculateSum(Map<String, Long> valueMetric) {
        Long sum = 0L;
        for (Entry<String, Long> entry : valueMetric.entrySet()) {
            if (!entry.getKey().equals("signed")) {
                sum += entry.getValue();
            }
        }

        return sum;
    }
}
