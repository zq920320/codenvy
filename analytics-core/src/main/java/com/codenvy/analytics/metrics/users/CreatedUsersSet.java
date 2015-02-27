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

import com.codenvy.analytics.metrics.AbstractSetValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS_ID, MetricFilter.PERSISTENT_WS})
public class CreatedUsersSet extends AbstractSetValueResulted {

    public CreatedUsersSet() {
        super(MetricType.CREATED_USERS_SET, USER);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.CREATED_USERS);
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context clauses) {
        if (!clauses.exists(MetricFilter.USER)) {
            return clauses.cloneAndPut(MetricFilter.REGISTERED_USER, 1);
        }
        return clauses;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Created registered users";
    }
}
