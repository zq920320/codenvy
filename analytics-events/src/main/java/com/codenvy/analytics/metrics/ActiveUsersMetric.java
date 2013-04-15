/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveUsersMetric extends ScriptBasedMetric {

    ActiveUsersMetric() {
        super(MetricType.ACTIVE_USERS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "# Active Users";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.ACTIVE_USERS;
    }

}
