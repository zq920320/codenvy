/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class InvitationsSentMetric extends ScriptBasedMetric {

    InvitationsSentMetric() {
        super(MetricType.INVITATIONS_SENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENT_COUNT_USER_INVITE;
    }
}
