/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsDestroyedMetric extends ScriptBasedMetric {

    ProjectsDestroyedMetric() {
        super(MetricType.PROJECTS_DESTROYED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Projects Destroyed";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_PROJECT_DESTROYED;
    }
}
