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
public class ProjectCreatedTypeJavaJarNumberMetric extends ProjectsCreatedListMetric {

    ProjectCreatedTypeJavaJarNumberMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_JAVA_JAR_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Jar",
              ValueType.NUMBER);
    }
}
