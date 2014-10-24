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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.metrics.AbstractLongValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS_ID, MetricFilter.PERSISTENT_WS})
public class AuthenticatedFactorySessions extends AbstractLongValueResulted {

    public AuthenticatedFactorySessions() {
        super(MetricType.AUTHENTICATED_FACTORY_SESSIONS, SESSION_ID);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
    }

    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        if (!context.exists(MetricFilter.USER_ID)) {
            Context.Builder builder = new Context.Builder(context);
            builder.put(MetricFilter.REGISTERED_USER, 1);
            return builder.build();
        }

        return context;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{REGISTERED_USER};
    }

    @Override
    public String getDescription() {
        return "The number sessions in temporary workspaces with registered users";
    }
}
