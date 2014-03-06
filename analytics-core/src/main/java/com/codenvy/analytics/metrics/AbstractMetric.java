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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.persistent.OrganizationClient;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractMetric implements Metric {

    public static final String ID                    = "_id";
    public static final String VALUE                 = "value";
    public static final String DATE                  = "date";
    public static final String WS                    = "ws";
    public static final String USER                  = "user";
    public static final String DOMAIN                = "domain";
    public static final String TIME                  = "time";
    public static final String CUMULATIVE_TIME       = "cumulative_time";
    public static final String LOGOUT_INTERVAL       = "logout_interval";
    public static final String SESSIONS              = "sessions";
    public static final String START_TIME            = "start_time";
    public static final String END_TIME              = "end_time";
    public static final String SESSION_ID            = "session_id";
    public static final String ORG_ID                = "org_id";
    public static final String AFFILIATE_ID          = "affiliate_id";
    public static final String REPOSITORY            = "repository";
    public static final String FACTORY               = "factory";
    public static final String REFERRER              = "referrer";
    public static final String AUTHENTICATED_SESSION = "authenticated_factory_session";
    public static final String CONVERTED_SESSION     = "converted_factory_session";
    public static final String WS_CREATED            = "ws_created";
    public static final String USER_CREATED          = "user_created";
    public static final String BUILD                 = "build";
    public static final String DEPLOY                = "deploy";
    public static final String RUN                   = "run";
    public static final String MESSAGE               = "message";
    public static final String EVENT                 = "event";
    public static final String USER_FIRST_NAME       = "user_first_name";
    public static final String USER_LAST_NAME        = "user_last_name";
    public static final String USER_COMPANY          = "user_company";
    public static final String USER_JOB              = "user_job";
    public static final String USER_PHONE            = "user_phone";
    public static final String ACTION                = "action";
    public static final String COUNT                 = "count";

    protected final String             metricName;
    protected final OrganizationClient organizationClient;

    public AbstractMetric(String metricName) {
        this.metricName = metricName.toLowerCase();
        this.organizationClient = Injector.getInstance(OrganizationClient.class);
    }

    public AbstractMetric(MetricType metricType) {
        this(metricType.toString());
    }

    @Override
    public String getName() {
        return metricName;
    }
}
