/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopDomainsLifeTimeMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopDomainsLifeTimeMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_LIFETIME, MetricType.PRODUCT_USAGE_TIME_DOMAINS, LIFE_TIME_PERIOD);
    }

    @Override
    public String getDescription() {
        return "Top 100 domains by time working in product for whole period";
    }
}
