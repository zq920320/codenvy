/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentActiveUsersMetric extends PercentMetric {

    PercentActiveUsersMetric() throws IOException {
        super(MetricType.PERCENT_ACTIVE_USERS, MetricFactory.createMetric(MetricType.TOTAL_USERS),
              MetricFactory.createMetric(MetricType.ACTIVE_USERS), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Active Users";
    }

}
