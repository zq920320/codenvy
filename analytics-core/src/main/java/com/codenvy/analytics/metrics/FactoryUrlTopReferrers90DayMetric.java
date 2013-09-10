/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopReferrers90DayMetric extends AbstractTopReferrersMetric {

    public FactoryUrlTopReferrers90DayMetric() {
        super(MetricType.FACTORY_URL_TOP_REFERRERS_BY_90DAY, 90);
    }

    @Override
    public String getDescription() {
        return "Top 100 referrers for factory by time usage during last 90 days";
    }
}
