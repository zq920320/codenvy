/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveWorkspacesSetMetric extends ScriptBasedMetric {

    ActiveWorkspacesSetMetric() {
        super(MetricType.ACTIVE_WORKSPACES_SET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.ACTIVE_WORKSPACES;
    }
}
