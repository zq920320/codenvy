/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopSessions365DayMetric extends AbstractTopSessionsMetric {

    public FactoryUrlTopSessions365DayMetric() {
        super(MetricType.FACTORY_URL_TOP_SESSIONS_BY_365DAY, 365);
    }

    @Override
    public String getDescription() {
        return "Top 100 sessions in temporary workspaces by time usage during last 365 days";
    }
}
