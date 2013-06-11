/*

 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.scripts;


import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum ScriptType {
    JREBEL_USER_PROFILE_GATHERING,
    WORKSPACES_CREATED,
    USERS_CREATED,
    USERS_REMOVED,
    WORKSPACES_DESTROYED,
    PROJECTS_DESTROYED,
    USERS_INVITATIONS,
    JREBEL_USAGE,
    ACTIVE_PROJECTS,
    ACTIVE_USERS_WORKSPACES,
    USERS_ADDED_TO_WS,
    PROJECTS_CREATED,
    PROJECTS_DEPLOYED,
    PROJECTS_DEPLOYED_LOCAL,
    PROJECTS_DEPLOYED_PAAS,
    PROJECTS_BUILT,
    USERS_SSO_LOGGED_IN,
    USERS_WITHOUT_PROJECTS,
    USERS_WITHOUT_BUILDS,
    USERS_WITHOUT_DEPLOYES,
    USERS_WITHOUT_INVITES,
    PRODUCT_USAGE_TIME,
    USERS_PROFILE_PREPARATION,

    USERS_PROFILE_LOG_PREPARATION {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR}));
        }
    },

    PRODUCT_USAGE_TIME_LOG_PREPARATION {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR}));
        }
    },

    PRODUCT_USAGE_TIME_TOP {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    },

    PRODUCT_USAGE_TIME_COMPANIES {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    },

    PRODUCT_USAGE_TIME_USERS {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    },


    PRODUCT_USAGE_TIME_DOMAINS {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    };

    /** @return what date type is represented in result */
    public Class< ? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    /** @return list of mandatory parameters required to be passed to the script */
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE, MetricParameter.TO_DATE}));
    }
}
