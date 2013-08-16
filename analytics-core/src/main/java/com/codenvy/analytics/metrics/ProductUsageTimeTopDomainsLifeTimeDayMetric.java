/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopDomainsLifeTimeDayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopDomainsLifeTimeDayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_LIFETIME, MetricType.PRODUCT_USAGE_TIME_DOMAINS, LIFE_TIME_PERIOD);
    }
}
