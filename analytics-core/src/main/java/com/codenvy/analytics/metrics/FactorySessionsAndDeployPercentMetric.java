/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAndDeployPercentMetric extends PercentMetric {

    FactorySessionsAndDeployPercentMetric() {
        super(MetricType.FACTORY_SESSIONS_AND_DEPLOY_PERCENT,
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS),
              MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AND_DEPLOY));
    }
}
