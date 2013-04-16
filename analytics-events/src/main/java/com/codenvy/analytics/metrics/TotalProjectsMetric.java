/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalProjectsMetric extends CumulativeMetric {

    TotalProjectsMetric() throws IOException {
        super(MetricType.TOTAL_RPOJECTS, MetricFactory.createMetric(MetricType.PROJECTS_CREATED),
              MetricFactory.createMetric(MetricType.PROJECTS_DESTROYED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Total Projects";
    }
}
