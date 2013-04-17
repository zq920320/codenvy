/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeOthersMetric extends ValueFromMapMetric {

    ProjectCreatedTypeOthersMetric() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_OTHERS, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "default", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Others";
    }
}
