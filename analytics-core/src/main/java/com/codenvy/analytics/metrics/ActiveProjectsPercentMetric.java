/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveProjectsPercentMetric extends PercentMetric {

    ActiveProjectsPercentMetric() throws IOException {
        super(MetricType.ACTIVE_PROJECTS_PERCENT, MetricFactory.createMetric(MetricType.TOTAL_PROJECTS_NUMBER),
              MetricFactory.createMetric(MetricType.ACTIVE_PROJECTS_NUMBER), false);
    }
}
