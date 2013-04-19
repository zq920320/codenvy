/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentProjectCreatedTypeRubyMetric extends ValueFromMapMetric {

    PercentProjectCreatedTypeRubyMetric() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_RUBY, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "Rails", ValueType.BOTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Ruby";
    }
}
