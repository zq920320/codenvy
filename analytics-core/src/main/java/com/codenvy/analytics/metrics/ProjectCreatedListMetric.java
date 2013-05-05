/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedListMetric extends ScriptBasedMetric {

    ProjectCreatedListMetric() {
        super(MetricType.PROJECTS_CREATED_LIST);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_PROJECT_CREATED;
    }
}
