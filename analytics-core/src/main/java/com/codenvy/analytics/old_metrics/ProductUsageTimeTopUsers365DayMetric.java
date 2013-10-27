/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsers365DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopUsers365DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_365DAY, MetricType.PRODUCT_USAGE_TIME_USERS, 365);
    }

    @Override
    public String getDescription() {
        return "Top 100 users by time working in product during last 365 days";
    }
}
