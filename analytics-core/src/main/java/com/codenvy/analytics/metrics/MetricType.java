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
    FACTORY_URL_ACCEPTED_NUMBER,
    FACTORY_URL_ACCEPTED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_URL_ACCEPTED,
                              ScriptType.FACTORY_URL_ACCEPTED_BY_FACTORY_URL,
                              ScriptType.FACTORY_URL_ACCEPTED_BY_REFERRER_URL);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        }
    },
    USER_UPDATE_PROFILE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.UPDATE_PROFILE_BY_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }

    },
    USER_PROFILE_EMAIL,
    USER_PROFILE_FIRSTNAME,
    USER_PROFILE_LASTNAME,
    USER_PROFILE_COMPANY,
    USER_PROFILE_PHONE,
    USERS_COMPLETED_PROFILE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.USERS_COMPLETED_PROFILE);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.USER_UPDATE_PROFILE));
        }
    },
    FILE_MANIPULATION {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, EventType.FILE_MANIPULATION.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.TENANT_CREATED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    TENANT_DESTROYED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, EventType.TENANT_DESTROYED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    ACTIVE_USERS_SET {
        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, "*");
            MetricParameter.FIELD.put(context, "user");
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }

        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_ACTIVE,
                              ScriptType.SET_ACTIVE_BY_DOMAINS,
                              ScriptType.SET_ACTIVE_BY_USERS);
        }
    },
    ACTIVE_USERS,
    ACTIVE_WS_SET {
        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, "*");
            MetricParameter.FIELD.put(context, "ws");
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }

        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_ACTIVE,
                              ScriptType.SET_ACTIVE_BY_USERS,
                              ScriptType.SET_ACTIVE_BY_DOMAINS);
        }
    },
    ACTIVE_WS,
    RETURNED_ACTIVE_WS,
    INACTIVE_WS,
    USER_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, EventType.USER_CREATED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    USER_CREATED_FROM_AUTH,
    RETURNED_ACTIVE_USERS,
    INACTIVE_USERS,
    SET_USER_CREATED_FROM_FACTORY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_USER_CREATED_FROM_FACTORY_BY_FACTORY_URL);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
            MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));
        }
    },
    USER_CREATED_FROM_FACTORY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.USER_CREATED_FROM_FACTORY,
                              ScriptType.USER_CREATED_FROM_FACTORY_BY_USERS,
                              ScriptType.USER_CREATED_FROM_FACTORY_BY_DOMAINS,
                              ScriptType.USER_CREATED_FROM_FACTORY_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
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
            MetricParameter.EVENT.put(context, EventType.USER_REMOVED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
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
            MetricParameter.EVENT.put(context, EventType.USER_SSO_LOGGED_IN.toString());
            MetricParameter.PARAM.put(context, "USING");
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.USER_CODE_REFACTOR.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.USER_CODE_COMPLETE.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.BUILD_STARTED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.RUN_STARTED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.DEBUG_STARTED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    PRODUCT_USAGE_SESSIONS {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PRODUCT_USAGE_SESSIONS,
                              ScriptType.PRODUCT_USAGE_SESSIONS_BY_USERS,
                              ScriptType.PRODUCT_USAGE_SESSIONS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    PRODUCT_USAGE_TIME_0_1,
    PRODUCT_USAGE_TIME_1_10,
    PRODUCT_USAGE_TIME_10_60,
    PRODUCT_USAGE_TIME_60_MORE,
    PRODUCT_USAGE_TIME_TOTAL,
    PRODUCT_USAGE_SESSIONS_0_1,
    PRODUCT_USAGE_SESSIONS_1_10,
    PRODUCT_USAGE_SESSIONS_10_60,
    PRODUCT_USAGE_SESSIONS_60_MORE,
    PRODUCT_USAGE_SESSIONS_TOTAL,
    PRODUCT_USAGE_USERS_0_10,
    PRODUCT_USAGE_USERS_10_60,
    PRODUCT_USAGE_USERS_60_300,
    PRODUCT_USAGE_USERS_300_MORE,
    USER_INVITE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, EventType.USER_INVITE.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    USERS_SENT_INVITE_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.EVENT.put(context, EventType.USER_INVITE.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
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
            MetricParameter.EVENT.put(context, EventType.USER_ADDED_TO_WS.toString());
            MetricParameter.PARAM.put(context, "FROM");
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.PROJECT_DESTROYED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.PROJECT_CREATED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
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
            MetricParameter.EVENT.put(context, EventType.PROJECT_CREATED.toString());
            MetricParameter.PARAM.put(context, "TYPE");
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    PROJECT_TYPE_JAR,
    PROJECT_TYPE_WAR,
    PROJECT_TYPE_JSP,
    PROJECT_TYPE_SPRING,
    PROJECT_TYPE_PHP,
    PROJECT_TYPE_DJANGO,
    PROJECT_TYPE_PYTHON,
    PROJECT_TYPE_JAVASCRIPT,
    PROJECT_TYPE_RUBY,
    PROJECT_TYPE_MMP,
    PROJECT_TYPE_NODEJS,
    PROJECT_TYPE_ANDROID,
    PROJECT_TYPE_OTHERS,
    USERS_CREATED_PROJECT_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.EVENT.put(context, EventType.PROJECT_CREATED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    PROJECT_DEPLOYED_TYPES {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PROJECT_DEPLOYED,
                              ScriptType.PROJECT_DEPLOYED_BY_DOMAINS,
                              ScriptType.PROJECT_DEPLOYED_BY_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    PROJECT_PAAS_ANY,
    PROJECT_PAAS_AWS,
    PROJECT_PAAS_APPFOG,
    PROJECT_PAAS_CLOUDBEES,
    PROJECT_PAAS_CLOUDFOUNDRY,
    PROJECT_PAAS_GAE,
    PROJECT_PAAS_HEROKU,
    PROJECT_PAAS_OPENSHIFT,
    PROJECT_PAAS_TIER3,
    PROJECT_NO_PAAS_DEFINED,
    USERS_SHELL_LAUNCHED_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
            MetricParameter.EVENT.put(context, EventType.SHELL_LAUNCHED.toString());
        }
    },
    USERS_BUILT_ONCE {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_ACTIVE_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
            MetricParameter.EVENT.put(context, EventType.PROJECT_BUILT.toString() + "," +
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
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
            MetricParameter.EVENT.put(context, EventType.PROJECT_DEPLOYED.toString() + "," +
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
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
            MetricParameter.EVENT.put(context, EventType.APPLICATION_CREATED.toString());
        }
    },
    PROJECT_BUILT {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
            MetricParameter.EVENT.put(context, EventType.PROJECT_BUILT.toString() + "," +
                                               EventType.PROJECT_DEPLOYED.toString() + "," +
                                               EventType.APPLICATION_CREATED.toString());
        }
    },
    PROJECT_DEPLOYED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS,
                              ScriptType.NUMBER_EVENTS_BY_USERS,
                              ScriptType.NUMBER_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
            MetricParameter.EVENT.put(context, EventType.PROJECT_DEPLOYED.toString() + "," +
                                               EventType.APPLICATION_CREATED.toString());
        }
    },
    ACTIVITY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.ACTIVITY_BY_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    SET_FACTORY_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_FACTORY_CREATED,
                              ScriptType.SET_FACTORY_CREATED_BY_AFFILIATE_ID,
                              ScriptType.SET_FACTORY_CREATED_BY_ORG_ID,
                              ScriptType.SET_FACTORY_CREATED_BY_PROJECT_TYPE,
                              ScriptType.SET_FACTORY_CREATED_BY_REPOSITORY_URL,
                              ScriptType.SET_FACTORY_CREATED_BY_USERS,
                              ScriptType.SET_FACTORY_CREATED_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    FACTORY_CREATED,
    ACTIVE_FACTORY_SET {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.SET_ACTIVE);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.FIELD.put(context, "url");
            MetricParameter.EVENT.put(context, EventType.FACTORY_URL_ACCEPTED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        }
    },
    TEMPORARY_WORKSPACE_CREATED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS, ScriptType.NUMBER_EVENTS_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.EVENT.put(context, EventType.TENANT_CREATED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        }
    },
    FACTORY_SESSIONS_TYPES {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_SESSIONS_TYPE, ScriptType.FACTORY_SESSIONS_TYPE_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        }
    },
    FACTORY_SESSIONS_AUTH,
    FACTORY_SESSIONS_AUTH_PERCENT,
    FACTORY_SESSIONS_ANON,
    FACTORY_SESSIONS_ANON_PERCENT,
    FACTORY_SESSIONS,
    FACTORY_SESSIONS_LIST {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_SESSIONS, ScriptType.FACTORY_SESSIONS_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
            MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));
        }
    },
    FACTORY_SESSION_FIRST,
    FACTORY_SESSION_LAST,
    PRODUCT_USAGE_SESSIONS_FACTORY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet
                    .of(ScriptType.PRODUCT_USAGE_SESSIONS_FACTORY,
                        ScriptType.PRODUCT_USAGE_SESSIONS_FACTORY_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
            MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));
        }
    },
    PRODUCT_USAGE_TIME_FACTORY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet
                    .of(ScriptType.PRODUCT_USAGE_TIME_FACTORY, ScriptType.PRODUCT_USAGE_TIME_FACTORY_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        }
    },
    PRODUCT_USAGE_FACTORY_SESSIONS_0_10,
    PRODUCT_USAGE_FACTORY_SESSIONS_10_MORE,
    FACTORY_PROJECT_IMPORTED {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_EVENTS, ScriptType.NUMBER_EVENTS_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.EVENT.put(context, EventType.FACTORY_PROJECT_IMPORTED.toString());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        }
    },
    FACTORY_SESSIONS_CONV,
    FACTORY_SESSIONS_CONV_PERCENT,
    FACTORY_SESSIONS_ABAN,
    FACTORY_SESSIONS_ABAN_PERCENT,
    FACTORY_SESSIONS_AND_BUILT {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_SESSIONS_AND_EVENT, ScriptType.FACTORY_SESSIONS_AND_EVENT_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
            MetricParameter.EVENT.put(context, EventType.PROJECT_BUILT.toString() + "," +
                                               EventType.BUILD_STARTED.toString() + "," +
                                               EventType.PROJECT_DEPLOYED.toString() + "," +
                                               EventType.APPLICATION_CREATED.toString());
        }
    },
    FACTORY_SESSIONS_AND_RUN {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_SESSIONS_AND_EVENT, ScriptType.FACTORY_SESSIONS_AND_EVENT_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
            MetricParameter.EVENT.put(context, EventType.RUN_STARTED.toString());
        }
    },
    FACTORY_SESSIONS_AND_DEPLOY {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.FACTORY_SESSIONS_AND_EVENT, ScriptType.FACTORY_SESSIONS_AND_EVENT_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
            MetricParameter.EVENT.put(context, EventType.PROJECT_DEPLOYED.toString() + "," +
                                               EventType.APPLICATION_CREATED.toString());
        }
    },
    FACTORY_SESSIONS_AND_BUILT_PERCENT,
    FACTORY_SESSIONS_AND_RUN_PERCENT,
    FACTORY_SESSIONS_AND_DEPLOY_PERCENT,
    FACTORY_URL_TOP_FACTORIES_BY_1DAY,
    FACTORY_URL_TOP_FACTORIES_BY_7DAY,
    FACTORY_URL_TOP_FACTORIES_BY_30DAY,
    FACTORY_URL_TOP_FACTORIES_BY_60DAY,
    FACTORY_URL_TOP_FACTORIES_BY_90DAY,
    FACTORY_URL_TOP_FACTORIES_BY_365DAY,
    FACTORY_URL_TOP_FACTORIES_BY_LIFETIME,
    JREBEL_USER_PROFILE_INFO {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.JREBEL_USER_PROFILE_INFO);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    PRODUCT_USAGE_TIME_USERS {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PRODUCT_USAGE_TIME_USERS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    PRODUCT_USAGE_TIME_TOP_USERS_BY_1DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_7DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_30DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_60DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_90DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_365DAY,
    PRODUCT_USAGE_TIME_TOP_USERS_BY_LIFETIME,
    PRODUCT_USAGE_TIME_DOMAINS {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PRODUCT_USAGE_TIME_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) throws IOException {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_1DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_7DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_30DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_60DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_90DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_365DAY,
    PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_LIFETIME,
    PRODUCT_USAGE_TIME_COMPANIES {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.PRODUCT_USAGE_TIME_COMPANIES);
        }


        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.USER_UPDATE_PROFILE));
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_1DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_7DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_30DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_60DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_90DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_365DAY,
    PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_LIFETIME,
    USERS_SEGMENT_ANALYSIS_CONDITION_1,
    USERS_SEGMENT_ANALYSIS_CONDITION_2,
    USERS_SEGMENT_ANALYSIS_CONDITION_3,
    FACTORY_URL_TOP_SESSIONS_BY_1DAY,
    FACTORY_URL_TOP_SESSIONS_BY_7DAY,
    FACTORY_URL_TOP_SESSIONS_BY_30DAY,
    FACTORY_URL_TOP_SESSIONS_BY_60DAY,
    FACTORY_URL_TOP_SESSIONS_BY_90DAY,
    FACTORY_URL_TOP_SESSIONS_BY_365DAY,
    FACTORY_URL_TOP_SESSIONS_BY_LIFETIME,
    ACTON {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.ACTON);
        }


        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        }
    },
    BUILD_TIME {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.TIME_BETWEEN_EVENTS,
                              ScriptType.TIME_BETWEEN_EVENTS_BY_USERS,
                              ScriptType.TIME_BETWEEN_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, EventType.BUILD_STARTED.getRootType());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    RUN_TIME {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.TIME_BETWEEN_EVENTS,
                              ScriptType.TIME_BETWEEN_EVENTS_BY_USERS,
                              ScriptType.TIME_BETWEEN_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, EventType.RUN_STARTED.getRootType());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    DEBUG_TIME {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.TIME_BETWEEN_EVENTS,
                              ScriptType.TIME_BETWEEN_EVENTS_BY_USERS,
                              ScriptType.TIME_BETWEEN_EVENTS_BY_DOMAINS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.EVENT.put(context, EventType.DEBUG_STARTED.getRootType());
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    PROJECT_WITH_JREBEL {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_PROJECT_WITH_JREBEL);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    PROJECT_RUNNED_WITH_JREBEL {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_PROJECT_RUNNED_WITH_JREBEL);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    PROJECT_DEPLOYED_WITH_JREBEL {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.NUMBER_PROJECT_DEPLOYED_WITH_JREBEL);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        }
    },
    REFERRERS {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.REFERRERS, ScriptType.REFERRERS_BY_WS);
        }

        @Override
        public void modifyContext(Map<String, String> context) {
            MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
            MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
            MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));
        }
    },
    FACTORY_URL_TOP_REFERRERS_BY_1DAY,
    FACTORY_URL_TOP_REFERRERS_BY_7DAY,
    FACTORY_URL_TOP_REFERRERS_BY_30DAY,
    FACTORY_URL_TOP_REFERRERS_BY_60DAY,
    FACTORY_URL_TOP_REFERRERS_BY_90DAY,
    FACTORY_URL_TOP_REFERRERS_BY_365DAY,
    FACTORY_URL_TOP_REFERRERS_BY_LIFETIME,
    ERROR_TYPES {
        @Override
        public EnumSet<ScriptType> getScripts() {
            return EnumSet.of(ScriptType.ERROR_TYPES);
        }
    };

    /**
     * @return set of scripts that are responsible for calculation value of the metric. If it returns nothing
     *         then value of the metric is based on value of another metric
     */
    public EnumSet<ScriptType> getScripts() {
        return EnumSet.noneOf(ScriptType.class);
    }

    /**
     * Should be overridden if there is necessity to pass additional parameters into context or
     * override default values.
     */
    public void modifyContext(Map<String, String> context) throws IOException {
        // do nothing by default
    }
}

