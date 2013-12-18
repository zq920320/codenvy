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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersProfiles extends AbstractUsersProfile {

    public static final String USER_EMAIL      = "_id";
    public static final String USER_FIRST_NAME = "user_first_name";
    public static final String USER_LAST_NAME  = "user_last_name";
    public static final String USER_COMPANY    = "user_company";
    public static final String USER_JOB        = "user_job";
    public static final String USER_PHONE      = "user_phone";

    public UsersProfiles() {
        super(MetricType.USERS_PROFILES);
    }

    @Override
    public String getDescription() {
        return "Users' profiles";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME, USER_COMPANY, USER_JOB, USER_PHONE};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
