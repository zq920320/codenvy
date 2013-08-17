/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopCompanies60DayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopCompanies60DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_60DAY, MetricType.PRODUCT_USAGE_TIME_COMPANIES, 60);
    }
}
