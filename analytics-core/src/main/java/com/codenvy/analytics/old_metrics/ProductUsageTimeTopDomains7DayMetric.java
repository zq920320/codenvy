/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopDomains7DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopDomains7DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_7DAY, MetricType.PRODUCT_USAGE_TIME_DOMAINS, 7);
    }

    @Override
    public String getDescription() {
        return "Top 100 domains by time working in product during last 7 days";
    }
}
