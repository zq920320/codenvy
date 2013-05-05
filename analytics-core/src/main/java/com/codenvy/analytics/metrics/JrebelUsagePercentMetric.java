/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JrebelUsagePercentMetric extends ValueFromMapMetric {

    JrebelUsagePercentMetric() throws IOException {
        super(MetricType.JREBEL_USAGE_PERCENT, MetricFactory.createMetric(MetricType.JREBEL_USAGE), ValueType.PERCENT, "true");
    }
}
