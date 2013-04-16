/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersCreatedProjectsMetric extends ScriptBasedMetric {

    UsersCreatedProjectsMetric() {
        super(MetricType.USERS_CREATED_PROJECTS);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "# Users Created Projects";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_USERS_CREATED_PROJECTS;
    }
}
