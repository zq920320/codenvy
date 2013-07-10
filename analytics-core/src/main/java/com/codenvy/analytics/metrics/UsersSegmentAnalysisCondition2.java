/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSegmentAnalysisCondition2 extends  ScriptBasedMetric {

    public UsersSegmentAnalysisCondition2() {
        super(MetricType.USERS_SEGMENT_ANALYSIS_CONDITION_2);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.USERS_SEGMENT_ANALYSIS_2;
    }
}
