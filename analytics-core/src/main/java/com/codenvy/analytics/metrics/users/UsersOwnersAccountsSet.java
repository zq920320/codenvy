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
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.USER})
public class UsersOwnersAccountsSet extends AbstractListValueResulted {
    public static final String ACCOUNTS = "accounts";

    public UsersOwnersAccountsSet() {
        super(MetricType.USERS_OWNERS_ACCOUNTS_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Users' accounts set where user is owner.";
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_ACCOUNTS);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{USER,
                            ACCOUNTS};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject();
        match.put(ROLES, new BasicDBObject("$in", new String[]{"account/owner"}));
        match.put(REMOVED, new BasicDBObject("$exists", false));

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
