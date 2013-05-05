/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersAddedToWorkspaceMetric extends ScriptBasedMetric {

    UsersAddedToWorkspaceMetric() {
        super(MetricType.USERS_ADDED_TO_WORKSPACE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_USER_ADDED_TO_WS;
    }
}
