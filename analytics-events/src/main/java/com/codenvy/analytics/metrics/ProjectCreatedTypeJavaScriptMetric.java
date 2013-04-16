/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeJavaScriptMetric extends ValueFromMapMetric {

    ProjectCreatedTypeJavaScriptMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_JAVASCRIPT, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "JavaScript", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Java Script";
    }
}
