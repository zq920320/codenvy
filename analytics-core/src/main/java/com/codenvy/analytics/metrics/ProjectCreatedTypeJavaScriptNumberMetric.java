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
public class ProjectCreatedTypeJavaScriptNumberMetric extends ProjectsCreatedListMetric {

    ProjectCreatedTypeJavaScriptNumberMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_JAVASCRIPT_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "JavaScript",
              ValueType.NUMBER);
    }
}
