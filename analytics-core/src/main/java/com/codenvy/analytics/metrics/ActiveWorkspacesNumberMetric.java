/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveWorkspacesNumberMetric extends SizeOfSetMetric {

    ActiveWorkspacesNumberMetric() throws IOException {
        super(MetricType.ACTIVE_WORKSPACES_NUMBER, MetricFactory.createMetric(MetricType.ACTIVE_WORKSPACES_SET));
    }
}
