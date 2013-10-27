/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsersLifeTimeDayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopUsersLifeTimeDayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_LIFETIME, MetricType.PRODUCT_USAGE_TIME_USERS,
              LIFE_TIME_PERIOD);
    }

    @Override
    public String getDescription() {
        return "Top 100 users by time working in product for whole period";
    }
}
