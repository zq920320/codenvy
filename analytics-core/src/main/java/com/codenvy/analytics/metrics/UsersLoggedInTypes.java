/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersLoggedInTypes extends AbstractMapValueResulted {

    public UsersLoggedInTypes() {
        super(MetricType.USERS_LOGGED_IN_TYPES);
    }

    public UsersLoggedInTypes(String metricName) {
        super(metricName);
    }

    @Override
    public String getDescription() {
        return "The number of logged in users with specific authentication type";
    }
}