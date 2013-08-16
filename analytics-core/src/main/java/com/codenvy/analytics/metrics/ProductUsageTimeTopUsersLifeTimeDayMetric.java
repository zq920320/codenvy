/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsersLifeTimeDayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopUsersLifeTimeDayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_LIFETIME, MetricType.PRODUCT_USAGE_TIME_USERS, LIFE_TIME_PERIOD);
    }
}
