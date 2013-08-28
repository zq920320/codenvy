/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAuthPercentMetric extends PercentMetric {

    FactorySessionsAuthPercentMetric() {
        super(MetricType.FACTORY_SESSIONS_AUTH_PERCENT,
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS),
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AUTH));
    }

    @Override
    public String getDescription() {
        return "The percent of sessions with authenticated users";
    }
}
