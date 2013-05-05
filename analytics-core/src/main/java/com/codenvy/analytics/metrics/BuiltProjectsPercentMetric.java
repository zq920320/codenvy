/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class BuiltProjectsPercentMetric extends PercentMetric {

    BuiltProjectsPercentMetric() throws IOException {
        super(MetricType.BUILT_PROJECTS_PERCENT, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_NUMBER),
              MetricFactory.createMetric(MetricType.BUILT_PROJECTS_NUMBER), false);
    }
}
