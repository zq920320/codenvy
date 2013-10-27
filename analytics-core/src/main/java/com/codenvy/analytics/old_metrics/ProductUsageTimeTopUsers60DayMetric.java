/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsers60DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopUsers60DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_60DAY, MetricType.PRODUCT_USAGE_TIME_USERS, 60);
    }

    @Override
    public String getDescription() {
        return "Top 100 users by time working in product during last 60 days";
    }
}
