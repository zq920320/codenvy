/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveProjectsNumberMetric extends SizeOfSetMetric {

    ActiveProjectsNumberMetric() throws IOException {
        super(MetricType.ACTIVE_PROJECTS_NUMBER, MetricFactory.createMetric(MetricType.ACTIVE_PROJECTS_SET));
    }
}
