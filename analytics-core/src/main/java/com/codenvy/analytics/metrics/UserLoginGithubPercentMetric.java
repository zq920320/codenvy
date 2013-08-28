/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserLoginGithubPercentMetric extends PercentMetric {

    public UserLoginGithubPercentMetric() {
        super(MetricType.USER_LOGIN_GITHUB_PERCENT,
              MetricFactory.createMetric(MetricType.USER_LOGIN_TOTAL),
              MetricFactory.createMetric(MetricType.USER_LOGIN_GITHUB));
    }

    @Override
    public String getDescription() {
        return "The percent of authentication with GitHub account";
    }
}
