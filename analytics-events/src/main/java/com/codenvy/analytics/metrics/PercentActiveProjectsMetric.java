/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentActiveProjectsMetric extends PercentMetric {

    PercentActiveProjectsMetric() throws IOException {
        super(MetricType.PERCENT_ACTIVE_PROJECTS, MetricFactory.createMetric(MetricType.TOTAL_PROJECTS),
              MetricFactory.createMetric(MetricType.ACTIVE_PROJECTS), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Active Projects";
    }

}
