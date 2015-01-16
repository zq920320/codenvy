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

import com.codenvy.analytics.metrics.AbstractCount;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;
import com.codenvy.analytics.metrics.RequiredAnyFilter;
import com.codenvy.analytics.persistent.MongoDataLoader;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.codenvy.analytics.Utils.getFilterAsSet;
import static com.codenvy.analytics.Utils.getFilterAsString;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed({"any"})
@RequiredAnyFilter({MetricFilter.FACTORY, MetricFilter.FACTORY_ID})
@OmitFilters({MetricFilter.USER_ID, MetricFilter.WS_ID})
public class FactoryUsed extends AbstractCount {

    public FactoryUsed() {
        super(MetricType.FACTORY_USED, MetricType.FACTORIES_ACCEPTED_LIST, FACTORY);
    }

    @Override
    public String getDescription() {
        return "The number of factory usage";
    }

    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        Object obj = clauses.get(MetricFilter.FACTORY);
        if (obj instanceof String) {
            String factory = (String)obj;
            boolean exclude = factory.startsWith(MongoDataLoader.EXCLUDE_SIGN);
            if (exclude) {
                factory = factory.substring(MongoDataLoader.EXCLUDE_SIGN.length());
            }

            Set<String> filters = new HashSet<>();
            for (String s : getFilterAsSet(factory)) {
                filters.add(s);
                filters.add(s.replace("/factory?id=", "/f?id="));
                filters.add(s.replace("/f?id=", "/factory?id="));
            }

            clauses = clauses.cloneAndPut(MetricFilter.FACTORY, (exclude ? MongoDataLoader.EXCLUDE_SIGN : "") + getFilterAsString(filters));
        }


        return super.applySpecificFilter(clauses);
    }
}
