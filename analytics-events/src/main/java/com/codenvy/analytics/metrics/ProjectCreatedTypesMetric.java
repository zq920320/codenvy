/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypesMetric extends ScriptBasedMetric {

    ProjectCreatedTypesMetric() {
        super(MetricType.PROJECT_CREATED_TYPES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Project By Types";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_PROJECT_CREATED_TYPES;
    }
}
