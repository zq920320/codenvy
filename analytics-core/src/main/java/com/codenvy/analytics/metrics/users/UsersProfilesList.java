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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmittedFilters;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmittedFilters({MetricFilter.WS})
public class UsersProfilesList extends AbstractUsersProfile {

    public UsersProfilesList() {
        super(MetricType.USERS_PROFILES_LIST);
    }

    @Override
    public String getDescription() {
        return "Users' profiles";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{ID,
                            USER_FIRST_NAME,
                            USER_LAST_NAME,
                            USER_COMPANY,
                            USER_JOB,
                            USER_PHONE,
                            CREATION_DATE};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
