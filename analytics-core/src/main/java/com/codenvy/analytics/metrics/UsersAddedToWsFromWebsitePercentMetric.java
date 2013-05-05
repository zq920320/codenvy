/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersAddedToWsFromWebsitePercentMetric extends ValueFromMapMetric {

    UsersAddedToWsFromWebsitePercentMetric() throws IOException {
        super(MetricType.USERS_ADDED_TO_WORKSPACE_FROM_WEBSITE_PERCENT, MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE), ValueType.PERCENT,
              "website");
    }
}
