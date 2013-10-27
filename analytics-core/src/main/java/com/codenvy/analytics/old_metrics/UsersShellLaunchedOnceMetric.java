/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersShellLaunchedOnceMetric extends ToDateValueReadBasedMetric {

    public UsersShellLaunchedOnceMetric() {
        super(MetricType.USERS_SHELL_LAUNCHED_ONCE);
    }

    @Override
    public String getDescription() {
        return "The number of users who launched shell at least once";
    }
}
