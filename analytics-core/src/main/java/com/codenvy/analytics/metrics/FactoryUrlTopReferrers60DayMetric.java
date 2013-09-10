/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopReferrers60DayMetric extends AbstractTopReferrersMetric {

    public FactoryUrlTopReferrers60DayMetric() {
        super(MetricType.FACTORY_URL_TOP_REFERRERS_BY_60DAY, 60);
    }

    @Override
    public String getDescription() {
        return "Top 100 referrers for factory by time usage during last 60 days";
    }
}
