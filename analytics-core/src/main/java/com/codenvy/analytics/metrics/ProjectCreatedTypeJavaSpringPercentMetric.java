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
public class ProjectCreatedTypeJavaSpringPercentMetric extends AbstractProjectsCreatedMetric {


    ProjectCreatedTypeJavaSpringPercentMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_JAVA_SPRING_PERCENT, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Spring",
              ValueType.PERCENT);
    }
}
