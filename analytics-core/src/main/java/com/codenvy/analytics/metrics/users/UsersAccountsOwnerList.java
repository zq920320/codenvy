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

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Alexander Reshetnyak
 */
@InternalMetric
public class UsersAccountsOwnerList extends AbstractListValueResulted {
    public static final String ACCOUNTS = "accounts";

    public UsersAccountsOwnerList() {
        super(MetricType.USERS_ACCOUNTS_OWNER_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Active accounts owners";
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_ACCOUNTS);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{USER, ACCOUNTS};
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject();
        match.put(ROLES, new BasicDBObject("$in", new String[]{"account/owner"}));
        match.put(REMOVED, 0);

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + USER);
        group.put(ACCOUNTS, new BasicDBObject("$addToSet", "$" + ACCOUNT));

        DBObject project = new BasicDBObject();
        project.put(USER, "$" + ID);
        project.put(ACCOUNTS, "$" + ACCOUNTS);

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }
}
