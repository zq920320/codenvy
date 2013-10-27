/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAndRunPercentMetric extends PercentMetric {

    FactorySessionsAndRunPercentMetric() {
        super(MetricType.FACTORY_SESSIONS_AND_RUN_PERCENT,
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS),
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AND_RUN));
    }

    @Override
    public String getDescription() {
        return "The percent of sessions where user run an application";
    }
}
