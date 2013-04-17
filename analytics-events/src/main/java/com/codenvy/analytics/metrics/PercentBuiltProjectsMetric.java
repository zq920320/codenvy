/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentBuiltProjectsMetric extends PercentMetric {

    PercentBuiltProjectsMetric() throws IOException {
        super(MetricType.PERCENT_BUILT_PROJECTS, MetricFactory.createMetric(MetricType.PROJECTS_CREATED),
              MetricFactory.createMetric(MetricType.BUILT_PROJECTS), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Built Projects";
    }
}
