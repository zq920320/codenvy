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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed({"system/admin", "system/manager"})
public class ProductUsageSessionsFailsList extends AbstractListValueResulted implements ReadBasedSummariziable {

    public ProductUsageSessionsFailsList() {
        super(MetricType.PRODUCT_USAGE_SESSIONS_FAILS_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            USER,
                            DATE,
                            END_TIME,
                            SESSION_ID,
                            FACTORY,
                            TIME};
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS_FAILS);
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));

        DBObject project = new BasicDBObject();
        project.put(TIME, 1);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Failed sessions";
    }
}
