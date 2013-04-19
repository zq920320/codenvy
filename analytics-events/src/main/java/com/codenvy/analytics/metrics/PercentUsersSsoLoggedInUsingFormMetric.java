/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentUsersSsoLoggedInUsingFormMetric extends PercentUserSsoLoggedInMetric {
    PercentUsersSsoLoggedInUsingFormMetric() throws IOException {
        super(MetricType.PERCENT_USERS_SSO_LOGGED_IN_USING_FORM, MetricFactory.createMetric(MetricType.USERS_SSO_LOGGED_IN), "jaas");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Using Login Form";
    }
}
