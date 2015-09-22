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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmytro Nochevnov
 */
public abstract class AbstractUsersDividedByCreationType extends AbstractActiveEntities {
    public static final String USING = "using";

    private String creationType;

    public AbstractUsersDividedByCreationType(MetricType metric, String creationType) {
        super(metric, MetricType.CREATED_USERS, USER);
        this.creationType = creationType;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        List<DBObject> operations = getMiddleDBOperations();

        DBObject count = new BasicDBObject();
        count.put(ID, null);
        count.put(USER, new BasicDBObject("$sum", 1));

        operations.add(new BasicDBObject("$group", count));

        return operations.toArray(new DBObject[0]);
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        List<DBObject> operations = getMiddleDBOperations();

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");
        operations.add(new BasicDBObject("$project", projection));

        return operations.toArray(new DBObject[0]);
    }

    private List<DBObject> getMiddleDBOperations() {
        DBObject match = new BasicDBObject();
        match.put(USING, creationType);

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + USER);

        return new ArrayList<>(Arrays.asList(new DBObject[]{new BasicDBObject("$match", match),
                                                            new BasicDBObject("$group", group)}));
    }
}
