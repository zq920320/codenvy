/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeJavaJarNumberMetric extends AbstractProjectsCreatedMetric {

    ProjectCreatedTypeJavaJarNumberMetric() {
        super(MetricType.PROJECT_TYPE_JAVA_JAR_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Jar",
              ValueType.NUMBER);
    }
}
