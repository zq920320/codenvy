/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopDomains90DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopDomains90DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_90DAY, MetricType.PRODUCT_USAGE_TIME_DOMAINS, 90);
    }

    @Override
    public String getDescription() {
        return "Top 100 domains by time working in product during last 90 days";
    }
}
