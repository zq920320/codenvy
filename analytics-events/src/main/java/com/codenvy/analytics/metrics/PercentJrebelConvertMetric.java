/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentJrebelConvertMetric extends ValueFromMapMetric {

    PercentJrebelConvertMetric() throws IOException {
        super(MetricType.PERCENT_JREBEL_USAGE, MetricFactory.createMetric(MetricType.JREBEL_USAGE), "true", ValueType.PERCENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% JRebel Usage";
    }
}
