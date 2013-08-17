/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopCompaniesLifeTimeDayMetric extends AbstractProductUsageTimeMetric {

    public ProductUsageTimeTopCompaniesLifeTimeDayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_LIFETIME, MetricType.PRODUCT_USAGE_TIME_COMPANIES, LIFE_TIME_PERIOD);
    }
}
