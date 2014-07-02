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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class ActiveUsersFromBeginning extends AbstractActiveEntities implements WithoutFromDateParam {

    public ActiveUsersFromBeginning() {
        super(MetricType.ACTIVE_USERS_FROM_BEGINNING, MetricType.ACTIVE_USERS_SET, USER);
    }

    @Override
    public String getDescription() {
        return "The number of active registered users";
    }

    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        Context.Builder builder = new Context.Builder(clauses);
        builder.putDefaultValue(Parameters.FROM_DATE);
        return super.applySpecificFilter(builder.build());
    }
}
