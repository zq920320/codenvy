/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalUsersMetric extends CumulativeMetric {

    TotalUsersMetric() {
        super(MetricType.TOTAL_USERS_NUMBER, MetricFactory.createMetric(MetricType.USERS_CREATED_NUMBER),
              MetricFactory.createMetric(MetricType.USERS_DESTROYED_NUMBER));
    }
}
