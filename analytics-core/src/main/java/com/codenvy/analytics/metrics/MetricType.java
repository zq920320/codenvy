/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.EventType;

import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum MetricType {
    FILE_MANIPULATION {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.FILE_MANIPULATION.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    TENANT_CREATED {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.TENANT_CREATED.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    TENANT_DESTROYED {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.TENANT_DESTROYED.toString());
            DataProcessing.calculateForWs(this, context);
        }
    },
    ACTIVE_WORKSPACES_NUMBER,
    USERS_CREATED_LIST,
    USERS_DESTROYED_LIST,
    USERS_CREATED_NUMBER,
    USERS_DESTROYED_NUMBER,
    TOTAL_USERS_NUMBER,
    ACTIVE_USERS_WORKAPCES_LIST,
    ACTIVE_USERS_NUMBER,
    USERS_CREATED_PROJECTS_NUMBER, // number of users, who create project at least once
    USERS_BUILT_PROJECTS_NUMBER, // number of users, who built project at least once
    USERS_DEPLOYED_PROJECTS_NUMBER, // number of users, who deployed project at least once
    USERS_DEPLOYED_PAAS_PROJECTS_NUMBER, // number of users, who deployed projects locally and paas
    USERS_INVITATIONS_SENT_NUMBER, // number of users, who sent invitation at least once
    USERS_ADDED_TO_WORKSPACE_LIST,
    USERS_UPDATE_PROFILE_LIST,
    USERS_COMPLETED_PROFILE,
    USER_CODE_REFACTOR {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.USER_CODE_REFACTOR.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    USER_CODE_COMPLETE {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.USER_CODE_COMPLETE.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    USER_ACTIVITY,
    USER_SESSIONS,
    USER_PROFILE,
    USER_PROFILE_EMAIL,
    USER_PROFILE_FIRSTNAME,
    USER_PROFILE_LASTNAME,
    USER_PROFILE_COMPANY,
    USER_PROFILE_PHONE,
    USERS_SEGMENT_ANALYSIS_CONDITION_1,
    USERS_SEGMENT_ANALYSIS_CONDITION_2,
    USERS_SEGMENT_ANALYSIS_CONDITION_3,
    USERS_SSO_LOGGED_IN_TYPES,
    USERS_SSO_LOGGED_IN_LIST,
    USERS_SSO_LOGGED_IN_USING_GOOGLE_PERCENT,
    USERS_SSO_LOGGED_IN_USING_GITHUB_PERCENT,
    USERS_SSO_LOGGED_IN_USING_FORM_PERCENT,
    BUILD_STARTED {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.BUILD_STARTED.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    RUN_STARTED {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.RUN_STARTED.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    DEBUG_STARTED {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.DEBUG_STARTED.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    PRODUCT_USAGE_TIME_TOTAL,
    PRODUCT_USAGE_TIME_0_10,
    PRODUCT_USAGE_TIME_10_60,
    PRODUCT_USAGE_TIME_60_MORE,
    PRODUCT_USAGE_USER_SESSIONS_NUMBER,
    PRODUCT_USAGE_USER_SESSIONS_NUMBER_0_10,
    PRODUCT_USAGE_USER_SESSIONS_NUMBER_10_60,
    PRODUCT_USAGE_USER_SESSIONS_NUMBER_60_MORE,
    PRODUCT_USAGE_TIME_LIST,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_1DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_7DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_30DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_60DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_90DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_365DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_LIFETIME,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_1DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_7DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_30DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_60DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_90DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_365DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_LIFETIME,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_1DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_7DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_30DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_60DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_90DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_365DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_LIFETIME,
    PROJECT_DESTROYED {
        @Override
        public void process(Map<String, String> context) throws Exception {
            Utils.putEvent(context, EventType.PROJECT_DESTROYED.toString());
            DataProcessing.calculateForWsUser(this, context);
        }
    },
    TOTAL_PROJECTS_NUMBER,
    ACTIVE_PROJECTS_LIST,
    ACTIVE_PROJECTS_NUMBER,
    PROJECTS_CREATED_NUMBER,
    PROJECTS_CREATED_LIST,
    PROJECTS_BUILT_NUMBER,
    PROJECTS_BUILT_LIST,
    PROJECTS_CREATED_TYPES,
    PROJECT_TYPE_JAVA_JAR_NUMBER,
    PROJECT_TYPE_JAVA_WAR_NUMBER,
    PROJECT_TYPE_JAVA_JSP_NUMBER,
    PROJECT_TYPE_JAVA_SPRING_NUMBER,
    PROJECT_TYPE_PHP_NUMBER,
    PROJECT_TYPE_PYTHON_NUMBER,
    PROJECT_TYPE_JAVASCRIPT_NUMBER,
    PROJECT_TYPE_RUBY_NUMBER,
    PROJECT_TYPE_MMP_NUMBER,
    PROJECT_TYPE_NODEJS_NUMBER,
    PROJECT_TYPE_ANDROID_NUMBER,
    PROJECT_TYPE_OTHERS_NUMBER,
    PROJECTS_DEPLOYED_LIST,
    PROJECTS_DEPLOYED_LOCAL_LIST,
    PROJECTS_DEPLOYED_PAAS_LIST,
    PROJECTS_DEPLOYED_NUMBER,
    PAAS_DEPLOYMENT_TYPES,
    PAAS_DEPLOYMENT_TYPE_AWS_NUMBER,
    PAAS_DEPLOYMENT_TYPE_APPFOG_NUMBER,
    PAAS_DEPLOYMENT_TYPE_CLOUDBEES_NUMBER,
    PAAS_DEPLOYMENT_TYPE_CLOUDFOUNDRY_NUMBER,
    PAAS_DEPLOYMENT_TYPE_GAE_NUMBER,
    PAAS_DEPLOYMENT_TYPE_HEROKU_NUMBER,
    PAAS_DEPLOYMENT_TYPE_OPENSHIFT_NUMBER,
    PAAS_DEPLOYMENT_TYPE_TIER3_NUMBER,
    PAAS_DEPLOYMENT_TYPE_LOCAL_NUMBER,
    JREBEL_USAGE_LIST,
    JREBEL_USER_PROFILE_INFO_GATHERING,
    INVITATIONS_SENT_LIST,
    INVITATIONS_SENT_NUMBER,
    USERS_SHELL_LAUNCHED_LIST,
    USERS_SHELL_LAUNCHED_NUMBER,
    INVITATIONS_ACCEPTED_PERCENT,
    TOTAL_WORKSPACES_NUMBER;

    public void process(Map<String, String> context) throws Exception {
        return;
    }
}
