/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersCreatedMetric extends ScriptBasedMetric {

    UsersCreatedMetric() {
        super(MetricType.USERS_CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Users Created";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_USER_CREATED;
    }

}
