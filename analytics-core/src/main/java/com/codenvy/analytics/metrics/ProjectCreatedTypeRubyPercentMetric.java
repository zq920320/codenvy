/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeRubyPercentMetric extends ProjectsCreatedListMetric {

    ProjectCreatedTypeRubyPercentMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_RUBY_PERCENT, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Rails",
              ValueType.PERCENT);
    }
}
