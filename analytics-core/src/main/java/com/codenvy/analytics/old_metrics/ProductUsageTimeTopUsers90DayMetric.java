/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsers90DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopUsers90DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_90DAY, MetricType.PRODUCT_USAGE_TIME_USERS, 90);
    }

    @Override
    public String getDescription() {
        return "Top 100 users by time working in product during last 90 days";
    }
}
