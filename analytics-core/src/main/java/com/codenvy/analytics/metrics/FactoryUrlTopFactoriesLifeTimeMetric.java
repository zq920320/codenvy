/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopFactoriesLifeTimeMetric extends AbstractTopFactoriesMetric {

    public FactoryUrlTopFactoriesLifeTimeMetric() {
        super(MetricType.FACTORY_URL_TOP_FACTORIES_BY_1DAY, LIFE_TIME_PERIOD);
    }

    @Override
    public String getDescription() {
        return "Top 100 factories by time usage for whole period";
    }
}
