/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersCreatedProjectOnceMetric extends ToDateValueReadBasedMetric {

    UsersCreatedProjectOnceMetric() {
        super(MetricType.USERS_CREATED_PROJECT_ONCE);
    }

    @Override
    public String getDescription() {
        return "The number of users who created project at least once";
    }
}
