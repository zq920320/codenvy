/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersAddedToWsFromWebsiteMetric extends ValueFromMapMetric {

    UsersAddedToWsFromWebsiteMetric() throws IOException {
        super(MetricType.USERS_ADDED_TO_WORKSPACE_FROM_WEBSITE, MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE), "website",
              true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% From Website";
    }
}
