/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAndRunPercentMetric extends PercentMetric {

    FactorySessionsAndRunPercentMetric() {
        super(MetricType.FACTORY_SESSIONS_AND_RUN_PERCENT,
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS),
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AND_RUN));
    }
}
