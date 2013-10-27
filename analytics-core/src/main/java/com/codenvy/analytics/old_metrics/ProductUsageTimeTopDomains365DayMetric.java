/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopDomains365DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopDomains365DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_365DAY, MetricType.PRODUCT_USAGE_TIME_DOMAINS, 365);
    }

    @Override
    public String getDescription() {
        return "Top 100 domains by time working in product during last 365 days";
    }
}
