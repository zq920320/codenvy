/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAbanPercentMetric extends PercentMetric {
    FactorySessionsAbanPercentMetric() {
        super(MetricType.FACTORY_SESSIONS_ABAN_PERCENT,
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS),
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_ABAN));

    }

    @Override
    public String getDescription() {
        return "The percent of abandoned sessions in temporary workspaces";
    }
}
