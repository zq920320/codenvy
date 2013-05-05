/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveUsersPercentMetric extends PercentMetric {

    ActiveUsersPercentMetric() throws IOException {
        super(MetricType.ACTIVE_USERS_PERCENT, MetricFactory.createMetric(MetricType.TOTAL_USERS_NUMBER),
              MetricFactory.createMetric(MetricType.ACTIVE_USERS_NUMBER), false);
    }
}
