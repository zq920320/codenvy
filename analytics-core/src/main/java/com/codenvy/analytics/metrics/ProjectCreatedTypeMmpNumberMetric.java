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
public class ProjectCreatedTypeMmpNumberMetric extends ProjectsCreatedListMetric {

    ProjectCreatedTypeMmpNumberMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_MMP_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Maven Multi-module",
              ValueType.NUMBER);
    }
}
