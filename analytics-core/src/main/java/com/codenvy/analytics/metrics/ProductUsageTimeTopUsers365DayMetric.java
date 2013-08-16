/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsers365DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopUsers365DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_365DAY, MetricType.PRODUCT_USAGE_TIME_USERS, 365);
    }
}
