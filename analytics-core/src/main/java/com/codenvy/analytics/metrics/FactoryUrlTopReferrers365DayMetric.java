/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopReferrers365DayMetric extends AbstractTopReferrersMetric {

    public FactoryUrlTopReferrers365DayMetric() {
        super(MetricType.FACTORY_URL_TOP_REFERRERS_BY_365DAY, 365);
    }

    @Override
    public String getDescription() {
        return "Top 100 referrers for factory by time usage during last 365 days";
    }
}
