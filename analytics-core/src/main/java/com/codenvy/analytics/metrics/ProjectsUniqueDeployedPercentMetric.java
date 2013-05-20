/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsUniqueDeployedPercentMetric extends PercentMetric {

    ProjectsUniqueDeployedPercentMetric() throws IOException {
        super(MetricType.PROJECTS_UNIQUE_DEPLOYED_PERCENT, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_NUMBER),
              MetricFactory.createMetric(MetricType.PROJECTS_UNIQUE_DEPLOYED_NUMBER), false);
    }
}
