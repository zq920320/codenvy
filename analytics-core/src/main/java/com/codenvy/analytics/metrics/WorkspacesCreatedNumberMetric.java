/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspacesCreatedNumberMetric extends ScriptBasedMetric {

    WorkspacesCreatedNumberMetric() {
        super(MetricType.WORKSPACES_CREATED_NUMBER);
    }


    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_WORKSPACE_CREATED;
    }
}
