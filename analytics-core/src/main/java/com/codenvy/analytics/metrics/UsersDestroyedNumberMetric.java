/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersDestroyedNumberMetric extends ScriptBasedMetric {

    UsersDestroyedNumberMetric() {
        super(MetricType.USERS_DESTROYED_NUMBER);
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_USER_REMOVED;
    }
}
