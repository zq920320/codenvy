/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public abstract class AbstractTopSessions extends AbstractTopMetrics {
    public AbstractTopSessions(MetricType factoryMetricType, int dayCount) {
        super(factoryMetricType, dayCount);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{TIME,
                            SESSION_ID,
                            FACTORY,
                            REFERRER,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject[] dbOperations = new DBObject[4];

        dbOperations[0] = new BasicDBObject("$match", new BasicDBObject(FACTORY, new BasicDBObject("$ne", "")));
        dbOperations[1] = new BasicDBObject("$match", new BasicDBObject(FACTORY, new BasicDBObject("$ne", null)));
        dbOperations[2] = new BasicDBObject("$sort", new BasicDBObject(TIME, -1));
        dbOperations[3] = new BasicDBObject("$limit", MAX_DOCUMENT_COUNT);

        return dbOperations;
    }


    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }
}
