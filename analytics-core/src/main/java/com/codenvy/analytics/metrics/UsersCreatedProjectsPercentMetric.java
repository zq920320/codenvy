/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersCreatedProjectsPercentMetric extends PercentMetric {

    UsersCreatedProjectsPercentMetric() throws IOException {
        super(MetricType.USERS_CREATED_PROJECTS_PERCENT, MetricFactory.createMetric(MetricType.USERS_CREATED_NUMBER),
              MetricFactory.createMetric(MetricType.USERS_CREATED_PROJECTS_NUMBER), false);
    }
}
