/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsDestroyedNumberMetric extends ScriptBasedMetric {

    ProjectsDestroyedNumberMetric() {
        super(MetricType.PROJECTS_DESTROYED_NUMBER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_PROJECT_DESTROYED;
    }
}
