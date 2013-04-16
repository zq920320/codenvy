/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeRubyMetric extends ValueFromMapMetric {

    ProjectCreatedTypeRubyMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_RUBY, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "Rails", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Ruby";
    }
}
