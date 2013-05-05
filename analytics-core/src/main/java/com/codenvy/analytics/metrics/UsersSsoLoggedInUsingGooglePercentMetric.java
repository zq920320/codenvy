/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSsoLoggedInUsingGooglePercentMetric extends ValueFromMapMetric {
    UsersSsoLoggedInUsingGooglePercentMetric() throws IOException {
        super(MetricType.USERS_SSO_LOGGED_IN_USING_GOOGLE_PERCENT, MetricFactory.createMetric(MetricType.USERS_SSO_LOGGED_IN),
              ValueType.PERCENT, "google", "signed");
    }
}
