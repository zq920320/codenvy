/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalUsersMetric extends CumulativeMetric {

    TotalUsersMetric() throws IOException {
        super(MetricType.TOTAL_USERS, MetricFactory.createMetric(MetricType.USERS_CREATED),
              MetricFactory.createMetric(MetricType.USERS_DESTROYED));
    }

    /**
     * @return
     */
    @Override
    public String getTitle() {
        return "Total Users";
    }

}
