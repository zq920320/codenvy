/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentUsersAddedToWsFromWebsiteMetric extends ValueFromMapMetric {

    PercentUsersAddedToWsFromWebsiteMetric() throws IOException {
        super(MetricType.PERCENT_USERS_ADDED_TO_WORKSPACE_FROM_WEBSITE, MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE), "website",
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
