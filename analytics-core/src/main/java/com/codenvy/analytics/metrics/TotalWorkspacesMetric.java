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
        super(MetricType.TOTAL_WORKSPACES_NUMBER, MetricFactory.createMetric(MetricType.WORKSPACES_CREATED_NUMBER),
              MetricFactory.createMetric(MetricType.WORKSPACES_DESTROYED_NUMBER));
    }
}
