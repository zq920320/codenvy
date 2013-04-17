/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class BuiltProjectsMetric extends ScriptBasedMetric {

    BuiltProjectsMetric() {
        super(MetricType.BUILT_PROJECTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "# Built Projects";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_DIST_PROJECT_BUILD;
    }
}
