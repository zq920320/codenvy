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

import com.codenvy.analytics.metrics.*;
import com.mongodb.DBObject;

import java.io.IOException;

import static com.codenvy.analytics.persistent.MongoDataLoader.processFilter;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractWorkspacesProfile extends ReadBasedMetric {

    public AbstractWorkspacesProfile(MetricType metricType) {
        super(metricType);
    }

    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.putIfNotNull(Parameters.PER_PAGE, clauses.getAsString(Parameters.PER_PAGE));
        builder.putIfNotNull(Parameters.PAGE, clauses.getAsString(Parameters.PAGE));
        builder.putIfNotNull(Parameters.SORT, clauses.getAsString(Parameters.SORT));

        for (MetricFilter filter : clauses.getFilters()) {
            Object value = clauses.get(filter);

            if (filter == MetricFilter.WS) {
                builder.put(MetricFilter._ID, processFilter(value, filter.isNumericType()));

            } else if (filter == MetricFilter.WS_NAME) {
                builder.put(filter, processFilter(value, filter.isNumericType()));
            }
        }

        return builder.build();
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        return new DBObject[0];
    }
}
