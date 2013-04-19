/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercnetUsersAddedToWsFromInviteMetric extends ValueFromMapMetric {

    PercnetUsersAddedToWsFromInviteMetric() throws IOException {
        super(MetricType.PERCENT_USERS_ADDED_TO_WORKSPACE_FROM_INVITE, MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE), "invite",
              ValueType.PERCENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% From Invites";
    }

}
