/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
@OmitFilters({MetricFilter.WS_ID, MetricFilter.PERSISTENT_WS})
public class UsersProfilesList extends AbstractUsersProfile {

    public UsersProfilesList() {
        super(MetricType.USERS_PROFILES_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Users' profiles";
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_PROFILES);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{ID,
                            DATE,
                            ALIASES,
                            USER_FIRST_NAME,
                            USER_LAST_NAME,
                            USER_COMPANY,
                            USER_JOB,
                            USER_PHONE,
                            REGISTERED_USER};
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    /** Utility method. */
    public static ListValueData getByID(Object id) throws IOException {
        return getByFilter(MetricFilter._ID, id);
    }

    private static ListValueData getByFilter(MetricFilter filter, Object valueFilter) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(filter, valueFilter);
        Context context = builder.build();

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        return ValueDataUtil.getAsList(metric, context);
    }
}
