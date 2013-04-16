/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeGroovyMetric extends ValueFromMapMetric {

    ProjectCreatedTypeGroovyMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_GROOVY, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "eXo", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Groovy";
    }
}
