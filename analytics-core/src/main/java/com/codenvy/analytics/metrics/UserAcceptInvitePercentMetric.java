/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserAcceptInvitePercentMetric extends PercentMetric {

    UserAcceptInvitePercentMetric() {
        super(MetricType.USER_ACCEPT_INVITE_PERCENT,
              MetricFactory.createMetric(MetricType.USER_INVITE),
              MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE));
    }

    @Override
    public String getDescription() {
        return "The percent of users who accepted invitations";
    }
}
