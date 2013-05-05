/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersCreatedSetMetric extends ScriptBasedMetric {

    UsersCreatedSetMetric() {
        super(MetricType.USERS_CREATED_SET);
    }

    /** {@inheritedDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_USER_CREATED;
    }
}
