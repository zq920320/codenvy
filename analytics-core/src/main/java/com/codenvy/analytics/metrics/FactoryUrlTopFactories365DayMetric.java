/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopFactories365DayMetric extends AbstractTopFactoriesMetric {

    public FactoryUrlTopFactories365DayMetric() {
        super(MetricType.FACTORY_URL_TOP_FACTORIES_BY_365DAY, 365);
    }
}
