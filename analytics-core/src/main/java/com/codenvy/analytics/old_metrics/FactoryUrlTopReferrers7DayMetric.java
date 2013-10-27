/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopReferrers7DayMetric extends AbstractTopReferrersMetric {

    public FactoryUrlTopReferrers7DayMetric() {
        super(MetricType.FACTORY_URL_TOP_REFERRERS_BY_7DAY, 7);
    }

    @Override
    public String getDescription() {
        return "Top 100 referrers for factory by time usage during last 7 days";
    }
}
