/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed({})
public class UsersStatisticsListPrecomputed extends AbstractListValueResulted implements PrecomputedDataMetric {

    public UsersStatisticsListPrecomputed() {
        super(MetricType.USERS_STATISTICS_LIST_PRECOMPUTED);
    }

    @Override
    public String getDescription() {
        return "Users' statistics data";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{USER,
                            UsersStatisticsList.PROJECTS,
                            UsersStatisticsList.RUNS,
                            UsersStatisticsList.DEBUGS,
                            UsersStatisticsList.BUILDS,
                            UsersStatisticsList.DEPLOYS,
                            UsersStatisticsList.FACTORIES,
                            UsersStatisticsList.TIME,
                            UsersStatisticsList.SESSIONS,
                            UsersStatisticsList.INVITES,
                            UsersStatisticsList.LOGINS,
                            UsersStatisticsList.RUN_TIME,
                            UsersStatisticsList.BUILD_TIME,
                            UsersStatisticsList.PAAS_DEPLOYS,
                            USER_FIRST_NAME,
                            USER_LAST_NAME,
                            USER_COMPANY,
                            USER_JOB
        };
    }

    @Override
    public Context getContextForBasedMetric() {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, Parameters.USER_TYPES.REGISTERED.name());

        return builder.build();
    }

    @Override
    public MetricType getBasedMetric() {
        return MetricType.USERS_STATISTICS_LIST;
    }
}