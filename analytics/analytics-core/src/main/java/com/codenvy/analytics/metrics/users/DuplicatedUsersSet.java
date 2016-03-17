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

import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Alexander Reshetnyak
 */
public class DuplicatedUsersSet extends ReadBasedMetric {

    public DuplicatedUsersSet() {
        super(MetricType.DUPLICATED_USERS_SET);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{ID};
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return SetValueData.class;
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_PROFILES);
    }


    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + ALIASES);
        group.put("count", new BasicDBObject("$sum", 1));

        DBObject match = new BasicDBObject();
        match.put("count", new BasicDBObject("$gt", 1));

        return new DBObject[]{new BasicDBObject("$unwind", "$" + ALIASES),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$match", match),
        };
    }

    @Override
    public String getDescription() {
        return "Set of users email which duplicated";
    }
}

