/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypePythonMetric extends ValueFromMapMetric {

    ProjectCreatedTypePythonMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_PYTHON, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "Python", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Python";
    }
}
