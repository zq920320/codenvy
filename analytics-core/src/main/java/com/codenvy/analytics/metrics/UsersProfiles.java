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
public class UsersProfiles extends AbstractUsersData {

    public static final String USER_EMAIL_ATTR   = "user_email";
    public static final String USER_PROFILE_ATTR = "user_profile";
    public static final String USER_COMPANY_ATTR = "user_company";
    public static final String USER_JOB_ATTR     = "user_job";
    public static final String USER_PHONE_ATTR   = "user_phone";

    public UsersProfiles() {
        super(MetricType.USERS_PROFILES);
    }

    @Override
    public boolean isSingleTable() {
        return true;
    }

    @Override
    public String getDescription() {
        return "User's profiles";
    }
}
