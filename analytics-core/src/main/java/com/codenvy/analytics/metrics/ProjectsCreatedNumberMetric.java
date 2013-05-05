/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsCreatedNumberMetric extends ScriptBasedMetric {

    ProjectsCreatedNumberMetric() {
        super(MetricType.PROJECTS_CREATED_NUMBER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_PROJECT_CREATED;
    }
}
