/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserLoggedInTypes extends AbstractMapValueResulted {

    public UserLoggedInTypes() {
        super(MetricType.USER_LOGGED_IN_TYPES);
    }

    public UserLoggedInTypes(String metricName) {
        super(metricName);
    }

    @Override
    public String getDescription() {
        return "The number of logged in users with specific authentication type";
    }
}