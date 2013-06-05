/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JrebelUserProfileInfoGatheringMetric extends PersistableScriptBasedMetric {

    JrebelUserProfileInfoGatheringMetric() {
        super(MetricType.JREBEL_USER_PROFILE_INFO_GATHERING);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.JREBEL_USER_PROFILE_GATHERING;
    }
}
