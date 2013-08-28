/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopSessionsLifeTimeMetric extends AbstractTopSessionsMetric {

    public FactoryUrlTopSessionsLifeTimeMetric() {
        super(MetricType.FACTORY_URL_TOP_SESSIONS_BY_LIFETIME, LIFE_TIME_PERIOD);
    }

    @Override
    public String getDescription() {
        return "Top 100 sessions in temporary workspaces by time usage for whole period";
    }
}
