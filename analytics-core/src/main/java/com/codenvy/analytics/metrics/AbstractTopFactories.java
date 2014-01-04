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

import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public abstract class AbstractTopFactories extends AbstractTopMetrics {   
    private static final long MAX_DOCUMENT_COUNT = 100;
    
    public static final String COUNT = "count";
    
    public AbstractTopFactories(MetricType factoryMetricType, int dayCount) {
        super(factoryMetricType, dayCount);
    }
    
    @Override
    public String[] getTrackedFields() {
        return new String[]{ProductUsageFactorySessionsList.FACTORY, 
                            ProductUsageFactorySessionsList.TIME,
                            COUNT};
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject[] dbOperations = new DBObject[4];

        // operations: db.product_usage_factory_sessions_list.aggregate([
        // {$group: {_id: "$factory", count: {$sum:1}, time: {$sum:"$time"}}},
        // {$project:{"time": 1, "count":1, "factory": "$_id", "_id": 0}},
        // {$sort:{time: -1}},
        // {$limit: 100}])
        dbOperations[0] = new BasicDBObject("$group",
                                            new BasicDBObject("_id", "$" + ProductUsageFactorySessionsList.FACTORY)
                                                      .append(COUNT, new BasicDBObject("$sum", 1))
                                                      .append(ProductUsageFactorySessionsList.TIME, new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.TIME)));
        
        dbOperations[1] = new BasicDBObject("$sort", new BasicDBObject(ProductUsageFactorySessionsList.TIME, -1));
        
        dbOperations[2] = new BasicDBObject("$project", new BasicDBObject(ProductUsageFactorySessionsList.TIME, 1)
                                                                  .append(COUNT, 1)
                                                                  .append(ProductUsageFactorySessionsList.FACTORY, "$_id")
                                                                  .append("_id", 0));
        
        dbOperations[3] = new BasicDBObject("$limit", MAX_DOCUMENT_COUNT);

        return dbOperations;
    }

    
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }
}
