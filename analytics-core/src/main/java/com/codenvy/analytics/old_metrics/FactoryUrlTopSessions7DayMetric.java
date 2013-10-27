/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopSessions7DayMetric extends AbstractTopSessionsMetric {

    public FactoryUrlTopSessions7DayMetric() {
        super(MetricType.FACTORY_URL_TOP_SESSIONS_BY_7DAY, 7);
    }

    @Override
    public String getDescription() {
        return "Top 100 sessions in temporary workspaces by time usage during last 7 days";
    }
}
