/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopFactories90DayMetric extends AbstractTopFactoriesMetric {

    public FactoryUrlTopFactories90DayMetric() {
        super(MetricType.FACTORY_URL_TOP_FACTORIES_BY_90DAY, 90);
    }
}
