/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopSessions365DayMetric extends AbstractTopSessionsMetric {

    public FactoryUrlTopSessions365DayMetric() {
        super(MetricType.FACTORY_URL_TOP_SESSIONS_BY_365DAY, 365);
    }
}
