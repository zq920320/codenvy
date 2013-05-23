/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSsoLoggedInUsingFormPercentMetric extends ValueFromMapMetric {
    UsersSsoLoggedInUsingFormPercentMetric() throws IOException {
        super(MetricType.USERS_SSO_LOGGED_IN_USING_FORM_PERCENT, MetricFactory.createMetric(MetricType.USERS_SSO_LOGGED_IN_TYPES), ValueType.PERCENT,
              "jaas", "signed");
    }
}
