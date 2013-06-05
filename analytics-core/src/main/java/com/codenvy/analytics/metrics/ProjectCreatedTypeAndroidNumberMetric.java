/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeAndroidNumberMetric extends AbstractProjectsCreatedMetric {

    ProjectCreatedTypeAndroidNumberMetric() {
        super(MetricType.PROJECT_TYPE_ANDROID_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Android",
              ValueType.NUMBER);
    }
}
