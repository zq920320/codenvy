/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersCompletedProfileMetric extends ToDateValueReadBasedMetric {

    public UsersCompletedProfileMetric() {
        super(MetricType.USERS_COMPLETED_PROFILE);
    }

    @Override
    public String getDescription() {
        return "The number of users who completed their profiles";
    }
}
