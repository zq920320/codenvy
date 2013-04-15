/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersDestroyedMetric extends ScriptBasedMetric {

    UsersDestroyedMetric() {
        super(MetricType.USERS_DESTROYED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Users Purged";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_USER_REMOVED;
    }

}
