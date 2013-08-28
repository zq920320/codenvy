/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsConvPercentMetric extends PercentMetric {

    FactorySessionsConvPercentMetric() {
        super(MetricType.FACTORY_SESSIONS_CONV_PERCENT,
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS),
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_CONV));

    }

    @Override
    public String getDescription() {
        return "The percent of converted sessions in temporary workspaces";
    }
}
