/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JrebelElibigleMetric extends ScriptBasedMetric {

    JrebelElibigleMetric() {
        super(MetricType.JREBEL_ELIGIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "JRrebel Eligible";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_JREBEL_USAGE;
    }
}
