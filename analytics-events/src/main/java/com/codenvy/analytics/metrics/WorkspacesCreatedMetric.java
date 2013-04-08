/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspacesCreatedMetric extends AbstractMetric {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Workspace Created";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_WORKSPACE_CREATED;
    }
}
