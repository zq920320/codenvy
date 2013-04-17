/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentInactiveProjectsMetric extends PercentMetric {


    PercentInactiveProjectsMetric() throws IOException {
        super(MetricType.PERCENT_INACTIVE_PROJECTS, MetricFactory.createMetric(MetricType.TOTAL_RPOJECTS),
              MetricFactory.createMetric(MetricType.ACTIVE_PROJECTS), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Inactive Projects";
    }
}
