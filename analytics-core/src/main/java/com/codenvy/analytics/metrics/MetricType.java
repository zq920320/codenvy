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
import com.codenvy.analytics.scripts.ScriptType;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum MetricType {
    FILE_MANIPULATION {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.FILE_MANIPULATION.toString());
        }
    },
    TENANT_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.TENANT_CREATED.toString());
        }
    },
    TENANT_DESTROYED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.TENANT_DESTROYED.toString());
        }
    },
    ACTIVE_USERS_SET {
        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, "*");
        }

        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_ACTIVE_USERS,
                              ScriptType.SET_ACTIVE_USERS_BY_DOMAINS,
                              ScriptType.SET_ACTIVE_USERS_BY_USERS);
        }
    },
    ACTIVE_USERS,
    ACTIVE_WS_SET {
        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, "*");
        }

        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_ACTIVE_WS,
                              ScriptType.SET_ACTIVE_WS_BY_USERS,
                              ScriptType.SET_ACTIVE_WS_BY_DOMAINS);
        }
    },
    ACTIVE_WS,
    USER_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_CREATED.toString());
        }
    },
    USER_REMOVED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_REMOVED.toString());
        }
    },
    TOTAL_USERS,
    TOTAL_WORKSPACES,
    USER_SSO_LOGGED_IN {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS_WITH_TYPE,
                              ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_DOMAINS,
                              ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_SSO_LOGGED_IN.toString());
            Utils.putParam(context, "USING");
        }
    },
    USER_LOGIN_GITHUB,
    USER_LOGIN_GOOGLE,
    USER_LOGIN_FORM,
    USER_LOGIN_TOTAL,
    USER_LOGIN_GITHUB_PERCENT,
    USER_LOGIN_GOOGLE_PERCENT,
    USER_LOGIN_FORM_PERCENT,

    USER_CODE_REFACTOR {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_CODE_REFACTOR.toString());
        }
    },
    USER_CODE_COMPLETE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_CODE_COMPLETE.toString());
        }
    },
    BUILD_STARTED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.BUILD_STARTED.toString());
        }
    },
    RUN_STARTED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.RUN_STARTED.toString());
        }
    },
    DEBUG_STARTED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.DEBUG_STARTED.toString());
        }
    },
    PRODUCT_USAGE_TIME {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PRODUCT_USAGE_TIME,
                              ScriptType.PRODUCT_USAGE_TIME_BY_USERS,
                              ScriptType.PRODUCT_USAGE_TIME_BY_DOMAINS);
        }
    },
    PRODUCT_USAGE_TIME_0_10,
    PRODUCT_USAGE_TIME_10_60,
    PRODUCT_USAGE_TIME_60_MORE,
    PRODUCT_USAGE_TIME_TOTAL,
    PRODUCT_USAGE_SESSIONS_0_10,
    PRODUCT_USAGE_SESSIONS_10_60,
    PRODUCT_USAGE_SESSIONS_60_MORE,
    PRODUCT_USAGE_SESSIONS_TOTAL,
    USER_INVITE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_INVITE.toString());
        }
    },
    USER_INVITE_ACTIVE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_INVITE.toString());
        }
    },
    USERS_SENT_INVITE_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.USER_INVITE.toString());
        }
    },
    USER_ACCEPT_INVITE,
    USER_ACCEPT_INVITE_PERCENT,
    USER_ADDED_TO_WORKSPACE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS_WITH_TYPE,
                              ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_DOMAINS,
                              ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.USER_ADDED_TO_WS.toString());
            Utils.putParam(context, "FROM");
        }
    },
    USER_ADDED_TO_WORKSPACE_INVITE,
    PROJECT_DESTROYED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.PROJECT_DESTROYED.toString());
        }
    },
    TOTAL_PROJECTS,
    PROJECT_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.PROJECT_CREATED.toString());
        }
    },
    PROJECT_CREATED_TYPES {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS_WITH_TYPE,
                              ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_DOMAINS,
                              ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.PROJECT_CREATED.toString());
            Utils.putParam(context, "TYPE");
        }
    },
    PROJECT_TYPE_JAR,
    PROJECT_TYPE_WAR,
    PROJECT_TYPE_JSP,
    PROJECT_TYPE_SPRING,
    PROJECT_TYPE_PHP,
    PROJECT_TYPE_PYTHON,
    PROJECT_TYPE_JAVASCRIPT,
    PROJECT_TYPE_RUBY,
    PROJECT_TYPE_MMP,
    PROJECT_TYPE_NODEJS,
    PROJECT_TYPE_ANDROID,
    PROJECT_TYPE_OTHERS,
    PROJECT_CREATED_USER_ACTIVE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            Utils.putEvent(context, EventType.PROJECT_CREATED.toString());
        }
    },
    USERS_CREATED_PROJECT_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.PROJECT_CREATED.toString());
        }
    },
    PROJECT_DEPLOYED_TYPES {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PROJECT_DEPLOYED,
                              ScriptType.PROJECT_DEPLOYED_BY_DOMAINS,
                              ScriptType.PROJECT_DEPLOYED_BY_USERS);
        }
    },
    PROJECT_PAAS_AWS,
    PROJECT_PAAS_APPFOG,
    PROJECT_PAAS_CLOUDBEES,
    PROJECT_PAAS_CLOUDFOUNDRY,
    PROJECT_PAAS_GAE,
    PROJECT_PAAS_HEROKU,
    PROJECT_PAAS_OPENSHIFT,
    PROJECT_PAAS_TIER3,
    PROJECT_PAAS_LOCAL,
    USERS_SHELL_LAUNCHED_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.SHELL_LAUNCHED.toString());
        }
    },
    USERS_BUILT_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.PROJECT_BUILT.toString() + "," +
                                    EventType.PROJECT_DEPLOYED.toString() + "," +
                                    EventType.APPLICATION_CREATED.toString());
        }
    },
    USERS_DEPLOYED_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.PROJECT_DEPLOYED.toString() + "," +
                                    EventType.APPLICATION_CREATED.toString());
        }
    },
    USERS_DEPLOYED_PAAS_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.APPLICATION_CREATED.toString());
        }
    },
    USER_BUILT {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putEvent(context, EventType.PROJECT_BUILT.toString() + "," +
                                    EventType.PROJECT_DEPLOYED.toString() + "," +
                                    EventType.APPLICATION_CREATED.toString());
        }
    },
    USER_DEPLOY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putEvent(context, EventType.PROJECT_DEPLOYED.toString() + "," +
                                    EventType.APPLICATION_CREATED.toString());
        }
    },
    ACTIVITY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.ACTIVITY_BY_USERS);
        }
    },
    FACTORY_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_CREATED, ScriptType.FACTORY_CREATED_BY_URL);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.FACTORY_CREATED.toString());
        }
    },
    FACTORY_URL_ACCEPTED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_URL_ACCEPTED);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putLoadDir(context, this);
            Utils.putStoreDir(context, this);
            Utils.putEvent(context, EventType.FACTORY_URL_ACCEPTED.toString());
        }
    },
    TEMPORARY_WORKSPACE_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.TEMPORARY_WORKSPACE_CREATED, ScriptType.TEMPORARY_WORKSPACE_CREATED_BY_WS);
        }
    },
    FACTORY_SESSIONS_TYPES {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS_WITH_TYPE);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putParam(context, "AUTHENTICATED");
            Utils.putEvent(context, EventType.SESSION_FACTORY_STARTED.toString());
        }
    },
    FACTORY_SESSIONS_AUTH,
    FACTORY_SESSIONS_ANON,
    FACTORY_SESSIONS,
    PRODUCT_USAGE_TIME_FACTORY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PRODUCT_USAGE_TIME_FACTORY);
        }
    },
    PRODUCT_USAGE_TIME_FACTORY_TOTAL,
    PRODUCT_USAGE_FACTORY_SESSIONS_0_10,
    PRODUCT_USAGE_FACTORY_SESSIONS_10_MORE,
    FACTORY_PROJECT_IMPORTED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putEvent(context, EventType.FACTORY_PROJECT_IMPORTED.toString());
        }
    },
    FACTORY_SESSIONS_CONV,
    FACTORY_SESSIONS_ABAN,
    FACTORY_SESSIONS_AND_BUILT {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_FACTORY_SESSIONS_AND_EVENT);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putEvent(context, EventType.PROJECT_BUILT.toString() + "," +
                                    EventType.BUILD_STARTED.toString() + "," +
                                    EventType.PROJECT_DEPLOYED.toString() + "," +
                                    EventType.APPLICATION_CREATED.toString());
        }
    },
    FACTORY_SESSIONS_AND_RUN {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_FACTORY_SESSIONS_AND_EVENT);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putEvent(context, EventType.RUN_STARTED.toString());
        }
    },
    FACTORY_SESSIONS_AND_DEPLOY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_FACTORY_SESSIONS_AND_EVENT);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            Utils.putEvent(context, EventType.PROJECT_DEPLOYED.toString() + "," +
                                    EventType.APPLICATION_CREATED.toString());
        }
    },
    FACTORY_SESSIONS_AND_BUILT_PERCENT,
    FACTORY_SESSIONS_AND_RUN_PERCENT,
    FACTORY_SESSIONS_AND_DEPLOY_PERCENT,
    JREBEL_USER_PROFILE_INFO {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.JREBEL_USER_PROFILE_INFO);
        }
    },


    USERS_UPDATE_PROFILE_LIST,
    USERS_COMPLETED_PROFILE,
    USER_PROFILE,
    USER_PROFILE_EMAIL,
    USER_PROFILE_FIRSTNAME,
    USER_PROFILE_LASTNAME,
    USER_PROFILE_COMPANY,
    USER_PROFILE_PHONE,

    USERS_SEGMENT_ANALYSIS_CONDITION_1,
    USERS_SEGMENT_ANALYSIS_CONDITION_2,
    USERS_SEGMENT_ANALYSIS_CONDITION_3,
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
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_LIFETIME;


    /** @return set of scripts that are responsible for calculation value of the metric */
    public EnumSet<ScriptType> getScripts() {
        return EnumSet.noneOf(ScriptType.class);
    }

    /** Should be overridden if there is necessity to pass additional parameters into context. */
    public void modifyContext(Map<String, String> context) throws IOException {
        // do nothing by default
    }
}
