/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeAndroidNumberMetric extends AbstractProjectsCreatedMetric {

    ProjectCreatedTypeAndroidNumberMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_ANDROID_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Android",
              ValueType.NUMBER);
    }
}
