/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspacesCreatedMetric extends ScriptCalculatedMetric {

    public WorkspacesCreatedMetric() {
        super(MetricType.WORKSPACES_CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Workspaces Created";
    }


    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_WORKSPACE_CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueManager getValueManager() {
        return new LongValueManager();
    }
}
