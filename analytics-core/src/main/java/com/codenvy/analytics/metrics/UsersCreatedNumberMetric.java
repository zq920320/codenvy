/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersCreatedNumberMetric extends ScriptBasedMetric {

    UsersCreatedNumberMetric() {
        super(MetricType.USERS_CREATED_NUMBER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_USER_CREATED;
    }
}
