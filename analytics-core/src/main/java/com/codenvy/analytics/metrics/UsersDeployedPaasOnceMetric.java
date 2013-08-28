/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersDeployedPaasOnceMetric extends ToDateValueReadBasedMetric {

    public UsersDeployedPaasOnceMetric() {
        super(MetricType.USERS_DEPLOYED_PAAS_ONCE);
    }

    @Override
    public String getDescription() {
        return "The number of users who deployed project at least once";
    }
}
