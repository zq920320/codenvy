/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSegmentAnalysisCondition3 extends  ScriptBasedMetric {

    public UsersSegmentAnalysisCondition3() {
        super(MetricType.USERS_SEGMENT_ANALYSIS_CONDITION_3);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.USERS_SEGMENT_ANALYSIS_3;
    }
}
