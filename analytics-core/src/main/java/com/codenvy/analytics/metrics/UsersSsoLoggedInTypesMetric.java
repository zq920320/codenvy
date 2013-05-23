/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSsoLoggedInMetric extends ScriptBasedMetric {

    UsersSsoLoggedInMetric() {
        super(MetricType.USERS_SSO_LOGGED_IN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_USER_SSO_LOGGED_IN;
    }
}
