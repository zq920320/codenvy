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
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class BuildsFinishedNormally extends AbstractLongValueResulted {

    public BuildsFinishedNormally() {
        super(MetricType.BUILDS_FINISHED_NORMALLY, PROJECT_ID);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.BUILDS_FINISHED);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        String field = getTrackedFields()[0];

        DBObject project1 = new BasicDBObject();
        project1.put("_id", 1);
        project1.put(TIMEOUT, new BasicDBObject("$multiply", new Object[]{"$" + TIMEOUT, 1000})); // to millisec
        project1.put(USAGE_TIME, 1);
        project1.put(field, 1);

        DBObject project2 = new BasicDBObject();
        project2.put("_id", 1);
        project2.put(TIMEOUT, 1);
        project2.put("cmpLifetimeToUsageTime", new BasicDBObject("$cmp", new String[]{"$" + TIMEOUT, "$" + USAGE_TIME}));
        project2.put(field, 1);

        DBObject match = new BasicDBObject();
        match.put("$or", new Object[]{new BasicDBObject(TIMEOUT, -1000),
                                      new BasicDBObject("cmpLifetimeToUsageTime", 1)});

        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(field, new BasicDBObject("$sum", "$" + field));

        return new DBObject[]{new BasicDBObject("$project", project1),
                              new BasicDBObject("$project", project2),
                              new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group)};
    }


    @Override
    public String getDescription() {
        return "The number of builds stopped normally";
    }
}
