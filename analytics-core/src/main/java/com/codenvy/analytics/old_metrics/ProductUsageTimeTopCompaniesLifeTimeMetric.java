/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopCompaniesLifeTimeMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopCompaniesLifeTimeMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_LIFETIME, MetricType.PRODUCT_USAGE_TIME_COMPANIES, LIFE_TIME_PERIOD);
    }

    @Override
    public String getDescription() {
        return "Top 100 companies by time working in product for whole period";
    }
}
