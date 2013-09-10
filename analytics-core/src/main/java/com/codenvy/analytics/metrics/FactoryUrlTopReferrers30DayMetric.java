/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopReferrers30DayMetric extends AbstractTopReferrersMetric {

    public FactoryUrlTopReferrers30DayMetric() {
        super(MetricType.FACTORY_URL_TOP_REFERRERS_BY_30DAY, 30);
    }

    @Override
    public String getDescription() {
        return "Top 100 referrers for factory by time usage during last 30 days";
    }
}
