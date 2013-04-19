/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentUsersSsoLoggedInUsingGoogleMetric extends PercentUserSsoLoggedInMetric {
    PercentUsersSsoLoggedInUsingGoogleMetric() throws IOException {
        super(MetricType.PERCENT_USERS_SSO_LOGGED_IN_USING_GOOGLE, MetricFactory.createMetric(MetricType.USERS_SSO_LOGGED_IN), "google");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Using Google";
    }
}
