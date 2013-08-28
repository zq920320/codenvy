/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersBuiltOnceMetric extends ToDateValueReadBasedMetric {

    public UsersBuiltOnceMetric() {
        super(MetricType.USERS_BUILT_ONCE);
    }

    @Override
    public String getDescription() {
        return "The number of users who built project at least once";
    }
}
