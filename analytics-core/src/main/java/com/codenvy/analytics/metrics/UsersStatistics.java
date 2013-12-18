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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersStatistics extends AbstractListValueResulted {

    public static final String USER_EMAIL      = "_id";
    public static final String USER_FIRST_NAME = "user_first_name";
    public static final String USER_LAST_NAME  = "user_last_name";
    public static final String USER_COMPANY    = "user_company";
    public static final String USER_PHONE      = "user_phone";
    public static final String PROJECTS        = "projects";
    public static final String BUILDS          = "builds";
    public static final String DEPLOYS         = "deploys";
    public static final String RUNS            = "runs";
    public static final String DEBUGS          = "debugs";
    public static final String FACTORIES       = "factories";
    public static final String SESSIONS        = "sessions";
    public static final String TIME            = "time";

    public UsersStatistics() {
        super(MetricType.USERS_STATISTICS);
    }

    @Override
    public String getDescription() {
        return "Users' statistics data";
    }
}
