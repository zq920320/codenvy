/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsCreatedMetric extends ScriptBasedMetric {

    ProjectsCreatedMetric() {
        super(MetricType.PROJECTS_CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Projects Created";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_PROJECT_CREATED;
    }
}
