/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum MetricType {

    WORKSPACES_CREATED {
        @Override
        public Metric getInstance() {
            return new WorkspacesCreatedMetric();
        }
    },

    WORKSPACES_DESTROYED {
        @Override
        public Metric getInstance() {
            return new WorkspacesDestoryedMetric();
        }
    },

    TOTAL_WORKSPACES {
        @Override
        public Metric getInstance() {
            return new TotalWorkspacesMetric();
        }
    };

    public abstract Metric getInstance();
}
