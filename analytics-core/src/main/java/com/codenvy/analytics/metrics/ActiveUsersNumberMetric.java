/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveUsersNumberMetric extends SizeOfSetMetric {

    ActiveUsersNumberMetric() throws IOException {
        super(MetricType.ACTIVE_USERS_NUMBER, MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET));
    }
}
