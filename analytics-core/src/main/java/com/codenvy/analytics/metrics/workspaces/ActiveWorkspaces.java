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
package com.codenvy.analytics.metrics.workspaces;

import com.codenvy.analytics.metrics.AbstractActiveEntities;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.USER, MetricFilter.REGISTERED_USER})
public class ActiveWorkspaces extends AbstractActiveEntities {

    public ActiveWorkspaces() {
        super(MetricType.ACTIVE_WORKSPACES, MetricType.ACTIVE_WORKSPACES_SET, WS);
    }

    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        Context.Builder builder = new Context.Builder(super.applySpecificFilter(clauses));
        builder.put(MetricFilter.PERSISTENT_WS, 1);
        return builder.build();
    }

    @Override
    public String getDescription() {
        return "The number of active persistent workspaces";
    }
}
