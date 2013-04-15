/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveWorkspacesMetric extends ScriptBasedMetric {

    ActiveWorkspacesMetric() {
        super(MetricType.ACTIVE_WORKSPACES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "# Active Workspaces";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.ACTIVE_WORKSPACES;
    }
}
