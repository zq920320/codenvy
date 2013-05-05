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
public class ProjectCreatedTypeNodejsNumberMetric extends ProjectsCreatedListMetric {

    ProjectCreatedTypeNodejsNumberMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_NODEJS_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "nodejs",
              ValueType.NUMBER);
    }
}
