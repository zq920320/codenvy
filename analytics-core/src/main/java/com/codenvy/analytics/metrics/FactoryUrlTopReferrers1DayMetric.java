/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopReferrers1DayMetric extends AbstractTopReferrersMetric {

    public FactoryUrlTopReferrers1DayMetric() {
        super(MetricType.FACTORY_URL_TOP_REFERRERS_BY_1DAY, 1);
    }

    @Override
    public String getDescription() {
        return "Top 100 referrers for factory by time usage during last day";
    }
}
