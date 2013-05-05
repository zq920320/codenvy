/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveWorkspacesPercentMetric extends PercentMetric {

    ActiveWorkspacesPercentMetric() throws IOException {
        super(MetricType.ACTIVE_WORKSPACES_PERCENT, MetricFactory.createMetric(MetricType.TOTAL_WORKSPACES_NUMBER),
              MetricFactory.createMetric(MetricType.ACTIVE_WORKSPACES_NUMBER), false);
    }
}
