/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopDomains30DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopDomains30DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_30DAY, MetricType.PRODUCT_USAGE_TIME_DOMAINS, 30);
    }

    @Override
    public String getDescription() {
        return "Top 100 domains by time working in product during last 30 days";
    }
}
