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
public class ProjectCreatedTypeJavaPhpPercentMetric extends AbstractProjectsCreatedMetric {

    ProjectCreatedTypeJavaPhpPercentMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_PHP_PERCENT, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "PHP", ValueType.PERCENT);
    }
}
