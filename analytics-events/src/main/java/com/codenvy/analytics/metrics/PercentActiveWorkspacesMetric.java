/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentActiveWorkspacesMetric extends PercentMetric {

    PercentActiveWorkspacesMetric() throws IOException {
        super(MetricType.PERCENT_ACTIVE_WORKSPACES, MetricFactory.createMetric(MetricType.TOTAL_WORKSPACES),
              MetricFactory.createMetric(MetricType.ACTIVE_WORKSPACES), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Active Workspaces";
    }

}
