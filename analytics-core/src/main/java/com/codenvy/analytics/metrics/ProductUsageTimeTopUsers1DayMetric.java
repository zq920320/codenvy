/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsers1DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopUsers1DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_1DAY, MetricType.PRODUCT_USAGE_TIME_USERS, 1);
    }

    @Override
    public String getDescription() {
        return "Top 100 users by time working in product during last day";
    }
}
