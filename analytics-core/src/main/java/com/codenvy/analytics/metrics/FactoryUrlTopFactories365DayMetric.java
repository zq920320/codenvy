/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopFactories365DayMetric extends AbstractTopFactoriesMetric {

    public FactoryUrlTopFactories365DayMetric() {
        super(MetricType.FACTORY_URL_TOP_FACTORIES_BY_365DAY, 365);
    }

    @Override
    public String getDescription() {
        return "Top 100 factories by time usage per last 365 days";
    }
}
