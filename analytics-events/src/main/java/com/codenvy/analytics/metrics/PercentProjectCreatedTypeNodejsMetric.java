/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentProjectCreatedTypeNodejsMetric extends ValueFromMapMetric {

    PercentProjectCreatedTypeNodejsMetric() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_NODEJS, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "nodejs",
              ValueType.BOTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Node.js";
    }
}
