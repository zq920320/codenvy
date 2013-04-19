/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentProjectCreatedTypeJavaScriptMetric extends ValueFromMapMetric {

    PercentProjectCreatedTypeJavaScriptMetric() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_JAVASCRIPT, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "JavaScript",
              ValueType.BOTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Java Script";
    }
}
