/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspacesDestoryedNumberMetric extends ScriptBasedMetric {

    WorkspacesDestoryedNumberMetric() {
        super(MetricType.WORKSPACES_DESTROYED_NUMBER);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_WORKSPACE_DESTROYED;
    }
}
