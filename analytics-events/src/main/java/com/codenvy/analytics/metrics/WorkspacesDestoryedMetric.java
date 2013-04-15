/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspacesDestoryedMetric extends ScriptBasedMetric implements Metric {

    WorkspacesDestoryedMetric() {
        super(MetricType.WORKSPACES_DESTROYED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Workspaces Destroyed";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_WORKSPACE_DESTROYED;
    }
}
