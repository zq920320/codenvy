/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopReferrersLifeTimeMetric extends AbstractTopReferrersMetric {

    public FactoryUrlTopReferrersLifeTimeMetric() {
        super(MetricType.FACTORY_URL_TOP_REFERRERS_BY_LIFETIME, LIFE_TIME_PERIOD);
    }

    @Override
    public String getDescription() {
        return "Top 100 referrers for factory by time usage for whole period";
    }
}
