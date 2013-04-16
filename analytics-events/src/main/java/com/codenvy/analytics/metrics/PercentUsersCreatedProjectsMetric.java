/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentUsersCreatedProjectsMetric extends PercentMetric {

    PercentUsersCreatedProjectsMetric() throws IOException {
        super(MetricType.PERCENT_USERS_CREATED_PROJECTS, MetricFactory.createMetric(MetricType.USERS_CREATED),
              MetricFactory.createMetric(MetricType.USERS_CREATED_PROJECTS), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Users Created Projects";
    }

}
