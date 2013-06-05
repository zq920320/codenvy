/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSsoLoggedInUsingGithubPercentMetric extends ValueFromMapMetric {

    UsersSsoLoggedInUsingGithubPercentMetric() {
        super(MetricType.USERS_SSO_LOGGED_IN_USING_GITHUB_PERCENT, MetricFactory.createMetric(MetricType.USERS_SSO_LOGGED_IN_TYPES),
              ValueType.PERCENT, "github", "signed");
    }
}
