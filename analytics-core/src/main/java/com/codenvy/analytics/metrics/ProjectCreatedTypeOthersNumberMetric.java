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
public class ProjectCreatedTypeOthersNumberMetric extends ProjectsCreatedListMetric {

    ProjectCreatedTypeOthersNumberMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_OTHERS_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), new String[]{"default",
                "null"}, ValueType.NUMBER);
    }
}
