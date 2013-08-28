/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopCompanies1DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopCompanies1DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_1DAY, MetricType.PRODUCT_USAGE_TIME_COMPANIES, 1);
    }

    @Override
    public String getDescription() {
        return "Top 100 companies by time working in product during last day";
    }
}
