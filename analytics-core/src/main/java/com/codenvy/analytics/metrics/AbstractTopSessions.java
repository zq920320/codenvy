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
package com.codenvy.analytics.metrics;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Map;


/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public abstract class AbstractTopSessions extends AbstractTopMetrics {
    public AbstractTopSessions(MetricType factoryMetricType, int dayCount) {
        super(factoryMetricType, dayCount);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{ProductUsageFactorySessionsList.TIME,
                            ProductUsageFactorySessionsList.FACTORY,
                            ProductUsageFactorySessionsList.REFERRER,
                            ProductUsageFactorySessionsList.AUTHENTICATED_SESSION,
                            ProductUsageFactorySessionsList.CONVERTED_SESSION};
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject[] dbOperations = new DBObject[2];

        dbOperations[0] = new BasicDBObject("$sort", new BasicDBObject(ProductUsageFactorySessionsList.TIME, -1));
        dbOperations[1] = new BasicDBObject("$limit", MAX_DOCUMENT_COUNT);

        return dbOperations;
    }


    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }
}
