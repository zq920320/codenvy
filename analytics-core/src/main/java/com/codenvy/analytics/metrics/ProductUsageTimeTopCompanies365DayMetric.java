/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopCompanies365DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopCompanies365DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_365DAY, MetricType.PRODUCT_USAGE_TIME_COMPANIES, 365);
    }

    @Override
    public String getDescription() {
        return "Top 100 companies by time working in product during last 365 days";
    }
}
