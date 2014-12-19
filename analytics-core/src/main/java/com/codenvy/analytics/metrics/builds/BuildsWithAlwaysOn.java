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
package com.codenvy.analytics.metrics.builds;

import com.codenvy.analytics.metrics.AbstractLongValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author Anatoliy Bazko */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class BuildsWithAlwaysOn extends AbstractLongValueResulted {

    public BuildsWithAlwaysOn() {
        super(MetricType.BUILDS_WITH_ALWAYS_ON, TASK_ID);
    }

    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(context);
        builder.put(MetricFilter.TIMEOUT, -1);
        return builder.build();
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        String field = getTrackedFields()[0];

        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(field, new BasicDBObject("$sum", "$" + field));

        return new DBObject[]{new BasicDBObject("$group", group)};
    }


    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.BUILDS);
    }

    @Override
    public String getDescription() {
        return "The number of always on builds";
    }
}
