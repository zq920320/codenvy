/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentProjectCreatedTypePythonMetric extends ValueFromMapMetric {

    PercentProjectCreatedTypePythonMetric() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_PYTHON, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "Python",
              ValueType.BOTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Python";
    }
}
