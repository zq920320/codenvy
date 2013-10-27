/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAndBuiltPercentMetric extends PercentMetric {

    FactorySessionsAndBuiltPercentMetric() {
        super(MetricType.FACTORY_SESSIONS_AND_BUILT_PERCENT,
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS),
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AND_BUILT));
    }

    @Override
    public String getDescription() {
        return "The percent of sessions where user built a project";
    }
}
