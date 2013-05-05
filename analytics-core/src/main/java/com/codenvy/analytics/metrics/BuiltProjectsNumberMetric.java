/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class BuiltProjectsNumberMetric extends ScriptBasedMetric {

    BuiltProjectsNumberMetric() {
        super(MetricType.BUILT_PROJECTS_NUMBER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_DIST_PROJECT_BUILD;
    }
}
