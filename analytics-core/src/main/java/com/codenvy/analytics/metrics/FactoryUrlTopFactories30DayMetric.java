/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopFactories30DayMetric extends AbstractTopFactoriesMetric {

    public FactoryUrlTopFactories30DayMetric() {
        super(MetricType.FACTORY_URL_TOP_FACTORIES_BY_30DAY, 30);
    }
}
