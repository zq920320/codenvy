/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveProjectsMetric extends ScriptBasedMetric {

    ActiveProjectsMetric() {
        super(MetricType.ACTIVE_PROJECTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "# Active Projects";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.ACTIVE_PROJECTS;
    }

}
