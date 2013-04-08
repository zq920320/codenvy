/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum MetricType {

    WORKSPACE_CREATED {
        @Override
        public Metric getInstance() throws IOException {
            return new WorkspacesCreatedMetric();
        }
    },
    
    WORKSPACE_DESTROYED {
        @Override
        public Metric getInstance() throws IOException {
            return new WorkspacesDestroyedMetric();
        }
    };

    public abstract Metric getInstance() throws IOException;
}
