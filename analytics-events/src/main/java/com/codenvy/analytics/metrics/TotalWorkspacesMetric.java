/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalWorkspacesMetric extends CumulativeCalculatedMetric {

    TotalWorkspacesMetric() {
        super(MetricType.TOTAL_WORKSPACES, MetricType.WORKSPACES_CREATED, MetricType.WORKSPACES_DESTROYED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Cumulative Total Workspaces";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueManager getValueManager() {
        return new LongValueManager();
    }

}
