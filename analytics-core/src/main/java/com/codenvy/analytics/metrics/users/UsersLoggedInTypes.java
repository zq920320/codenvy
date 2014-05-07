/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.AbstractMapValueResulted;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS})
public class UsersLoggedInTypes extends AbstractMapValueResulted {

    public static final String GITHUB  = "github";
    public static final String JAAS    = "jaas";
    public static final String GOOGLE  = "google";
    public static final String ORG     = "org";
    public static final String SYSLDAP = "sysldap";
    public static final String EMAIL   = "email";

    public UsersLoggedInTypes() {
        super(MetricType.USERS_LOGGED_IN_TYPES);
    }

    public UsersLoggedInTypes(String metricName) {
        super(metricName);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{GITHUB,
                            JAAS,
                            GOOGLE,
                            ORG,
                            SYSLDAP,
                            EMAIL};
    }

    @Override
    public String getDescription() {
        return "The number of logged in users with specific authentication type";
    }
}