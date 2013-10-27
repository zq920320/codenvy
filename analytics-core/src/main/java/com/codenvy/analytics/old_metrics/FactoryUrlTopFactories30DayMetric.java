/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopFactories30DayMetric extends AbstractTopFactoriesMetric {

    public FactoryUrlTopFactories30DayMetric() {
        super(MetricType.FACTORY_URL_TOP_FACTORIES_BY_30DAY, 30);
    }

    @Override
    public String getDescription() {
        return "Top 100 factories by time usage per last 30 days";
    }
}
