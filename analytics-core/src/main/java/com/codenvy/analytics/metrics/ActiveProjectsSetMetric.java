/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveProjectsSetMetric extends ScriptBasedMetric {

    ActiveProjectsSetMetric() {
        super(MetricType.ACTIVE_PROJECTS_SET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.ACTIVE_PROJECTS;
    }
}
