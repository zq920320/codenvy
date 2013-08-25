/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopSessions1DayMetric extends AbstractTopSessionsMetric {

    public FactoryUrlTopSessions1DayMetric() {
        super(MetricType.FACTORY_URL_TOP_SESSIONS_BY_1DAY, 1);
    }
}
