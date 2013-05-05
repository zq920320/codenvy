/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersAddedToWsFromInvitePercentMetric extends ValueFromMapMetric {

    UsersAddedToWsFromInvitePercentMetric() throws IOException {
        super(MetricType.USERS_ADDED_TO_WORKSPACE_FROM_INVITE_PERCENT, MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE), ValueType.PERCENT,
              "invite");
    }
}
