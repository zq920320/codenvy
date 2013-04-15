/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalWorkspacesMetric extends CumulativeMetric {

    TotalWorkspacesMetric() throws IOException {
        super(MetricType.TOTAL_WORKSPACES, MetricFactory.createMetric(MetricType.WORKSPACES_CREATED),
              MetricFactory.createMetric(MetricType.WORKSPACES_DESTROYED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Cumulative Total Workspaces";
    }

}
