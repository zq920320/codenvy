/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics;

/** @author Anatoliy Bazko */
public abstract class AbstractMetric implements Metric {

    public static final String ID                    = "_id";
    public static final String VALUE                 = "value";
    public static final String DATE                  = "date";
    public static final String WS                    = "ws";
    public static final String WS_NAME               = "ws_name";
    public static final String USER                  = "user";
    public static final String ALIASES               = "aliases";
    public static final String DOMAIN                = "domain";
    public static final String TIME                  = "time";
    public static final String RUN_TIME              = "run_time";
    public static final String BUILD_TIME            = "build_time";
    public static final String PAAS_DEPLOYS          = "paas_deploys";
    public static final String CUMULATIVE_TIME       = "cumulative_time";
    public static final String STATE                 = "state";
    public static final String LOGOUT_INTERVAL       = "logout_interval";
    public static final String SESSIONS              = "sessions";
    public static final String END_TIME              = "end_time";
    public static final String SESSION_ID            = "session_id";
    public static final String SESSION               = "session";
    public static final String ORG_ID                = "org_id";
    public static final String AFFILIATE_ID          = "affiliate_id";
    public static final String REPOSITORY            = "repository";
    public static final String ENCODED_FACTORY       = "encoded_factory";
    public static final String FACTORY               = "factory";
    public static final String FACTORY_ID            = "factory_id";
    public static final String PROJECT               = "project";
    public static final String PROJECT_TYPE          = "project_type";
    public static final String PROJECT_PAAS          = "project_paas";
    public static final String PROJECT_ID            = "project_id";
    public static final String REFERRER              = "referrer";
    public static final String AUTHENTICATED_SESSION = "authenticated_factory_session";
    public static final String CONVERTED_SESSION     = "converted_factory_session";
    public static final String WS_CREATED            = "ws_created";
    public static final String USER_CREATED          = "user_created";
    public static final String PROJECTS              = "projects";
    public static final String INVITES               = "invites";
    public static final String LOGINS                = "logins";
    public static final String FACTORIES             = "factories";
    public static final String BUILDS                = "builds";
    public static final String DEPLOYS               = "deploys";
    public static final String RUNS                  = "runs";
    public static final String DEBUGS                = "debugs";
    public static final String MESSAGE               = "message";
    public static final String EVENT                 = "event";
    public static final String ACTION                = "action";
    public static final String USER_FIRST_NAME       = "user_first_name";
    public static final String USER_LAST_NAME        = "user_last_name";
    public static final String USER_COMPANY          = "user_company";
    public static final String USER_JOB              = "user_job";
    public static final String USER_PHONE            = "user_phone";
    public static final String PROJECT_CREATES       = "project_creates";
    public static final String PROJECT_DESTROYS      = "project_destroys";
    public static final String CODE_REFACTORIES      = "code_refactories";
    public static final String CODE_COMPLETES        = "code_completes";
    public static final String BUILD_INTERRUPTS      = "build_interrupts";
    public static final String ARTIFACT_DEPLOYS      = "artifact_deploys";
    public static final String DEBUG_TIME            = "debug_time";
    public static final String REGISTERED_USER       = "registered_user";
    public static final String PERSISTENT_WS         = "persistent_ws";
    public static final String DEFAULT_VALUE         = "default";
    public static final String MEMORY                = "memory";
    public static final String LIFETIME              = "lifetime";
    public static final String USAGE_TIME            = "usage_time";

    private final String metricName;

    public AbstractMetric(String metricName) {
        this.metricName = metricName.toLowerCase();
    }

    public AbstractMetric(MetricType metricType) {
        this(metricType.toString());
    }

    @Override
    public String getName() {
        return metricName;
    }
}
