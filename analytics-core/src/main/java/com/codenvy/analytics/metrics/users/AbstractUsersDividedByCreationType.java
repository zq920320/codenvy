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

import com.codenvy.analytics.metrics.AbstractActiveEntities;
import com.codenvy.analytics.metrics.AbstractLongValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Dmytro Nochevnov
 */
public abstract class AbstractUsersDividedByCreationType extends AbstractActiveEntities {
    public static final String USING = "using";

    private String creationType;

    public AbstractUsersDividedByCreationType(MetricType metric, String creationType) {
        super(metric, MetricType.USERS_LOGGED_IN_TYPES, USER);
        this.creationType = creationType;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject count = new BasicDBObject();
        count.put(ID, null);
        count.put(USER, new BasicDBObject("$sum", 1));

        DBObject[] dbOperation = getSpecificExpandedDBOperations(clauses);
        dbOperation[dbOperation.length - 1] = new BasicDBObject("$group", count);  // replace $project with $group with count number of documents

        return dbOperation;
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject sort = new BasicDBObject();
        sort.put(DATE, 1);

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + USER);
        group.put(USING, new BasicDBObject("$first", "$" + USING));
        group.put(DATE, new BasicDBObject("$first", "$" + DATE));

        DBObject match = new BasicDBObject();
        match.put(USING, creationType);

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$sort", sort),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$match", match),
                              new BasicDBObject("$project", projection)};
    }
}
