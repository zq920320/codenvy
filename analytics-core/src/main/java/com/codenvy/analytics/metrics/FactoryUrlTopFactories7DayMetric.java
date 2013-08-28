/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopFactories7DayMetric extends AbstractTopFactoriesMetric {

    public FactoryUrlTopFactories7DayMetric() {
        super(MetricType.FACTORY_URL_TOP_FACTORIES_BY_7DAY, 7);
    }

    @Override
    public String getDescription() {
        return "Top 100 factories by time usage per last 7 days";
    }
}
