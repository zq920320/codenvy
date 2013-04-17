/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSsoLoggedInUsingGithubMetric extends ValueFromMapMetric {

    UsersSsoLoggedInUsingGithubMetric() throws IOException {
        super(MetricType.PERCENT_USERS_SSO_LOGGED_IN_USING_GITHUB, MetricFactory.createMetric(MetricType.USERS_SSO_LOGGED_IN), "github", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Using Github";
    }
}
