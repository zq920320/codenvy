/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JrebelUsageMetric extends ScriptBasedMetric {

    JrebelUsageMetric() {
        super(MetricType.JREBEL_USAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "JRebel Usage";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_JREBEL_USAGE;
    }
}
