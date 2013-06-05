/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalProjectsMetric extends CumulativeMetric {

    TotalProjectsMetric() {
        super(MetricType.TOTAL_PROJECTS_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_NUMBER),
              MetricFactory.createMetric(MetricType.PROJECTS_DESTROYED_NUMBER));
    }
}
