/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentInactiveWorkspacesMetric extends PercentMetric {

    PercentInactiveWorkspacesMetric() throws IOException {
        super(MetricType.PERCENT_INACTIVE_WORKSPACES, MetricFactory.createMetric(MetricType.TOTAL_WORKSPACES),
              MetricFactory.createMetric(MetricType.ACTIVE_WORKSPACES), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Inactive Workspaces";
    }

}
