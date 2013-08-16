/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopDomains1DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopDomains1DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_1DAY, MetricType.PRODUCT_USAGE_TIME_DOMAINS, 1);
    }
}
