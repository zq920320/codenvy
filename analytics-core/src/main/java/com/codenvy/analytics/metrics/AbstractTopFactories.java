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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public abstract class AbstractTopFactories extends AbstractTopMetrics {   
    public static final String FACTORY_COUNT = "factory_count";
    public static final String BUILD_RATE = "build_rate";
    public static final String RUN_RATE = "run_rate";
    public static final String DEPLOY_RATE = "deploy_rate";
    public static final String ANONYMOUS_FACTORY_SESSION_RATE = "anonymous_factory_session_rate";
    public static final String AUTHENTICATED_FACTORY_SESSION_RATE = "authenticated_factory_session_rate";
    public static final String ABANDON_FACTORY_SESSION_RATE = "abandon_factory_session_rate";
    public static final String CONVERTED_FACTORY_SESSION_RATE = "converted_factory_session_rate";
    
    public AbstractTopFactories(MetricType factoryMetricType, int dayCount) {
        super(factoryMetricType, dayCount);
    }
    
    @Override
    public String[] getTrackedFields() {
        return new String[]{ProductUsageFactorySessionsList.FACTORY, 
                            ProductUsageFactorySessionsList.TIME,
                            FACTORY_COUNT,
                            BUILD_RATE,
                            RUN_RATE,
                            DEPLOY_RATE,
                            ANONYMOUS_FACTORY_SESSION_RATE,
                            AUTHENTICATED_FACTORY_SESSION_RATE,
                            ABANDON_FACTORY_SESSION_RATE,
                            CONVERTED_FACTORY_SESSION_RATE
                            };
    }

    /**
       db.product_usage_factory_sessions_list.aggregate([
              {$group: 
                      {_id: "$factory", 
                       factory_count: {$sum: 1}, 
                       time: {$sum:"$time"},
                       build_count: {$sum:"$build"},
                       run_count: {$sum: "$run"},
                       deploy_count: {$sum: "$deploy"},
                       authenticated_factory_session_count: {$sum: "$authenticated_factory_session"},
                       converted_factory_session_count: {$sum: "$converted_factory_session"}
                      }
              },
              {$project: {
                         factory: "$_id", 
                         time: 1, 
                         factory_count:1, 
                         _id: 0,
                         build_rate: {$multiply: [100, 
                                                  {$divide: ["$build_count", "$factory_count"]}]},
                         run_rate: {$multiply: [100, 
                                                {$divide: ["$run_count", "$factory_count"]}]},
                         deploy_rate: {$multiply: [100, 
                                                   {$divide: ["$deploy_count", "$factory_count"]}]},
                         authenticated_factory_session_rate: {$multiply: [100, 
                                                                         {$divide: ["$authenticated_factory_session_count", "$factory_count"]}]},
                         converted_factory_session_rate: {$multiply: [100, 
                                                   {$divide: ["$converted_factory_session_count", "$factory_count"]}]}
                      }
              },
              {$project: {
                         factory:1,
                         time: 1, 
                         factory_count:1, 
                         build_rate:1,
                         run_rate:1,
                         deploy_rate:1,
                         anonymous_factory_session_rate: {$subtract: [100, "$authenticated_factory_session_rate"]},
                         authenticated_factory_session_rate: 1,
                         abandon_factory_session_rate: {$subtract: [100, "$converted_factory_session_rate"]},
                         converted_factory_session_rate:1
                      }

              },
              {$sort:{time: -1}},
              {$limit: 100}
       ]) 
     */
    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        dbOperations.add(new BasicDBObject("$group",
            new BasicDBObject("_id", "$" + ProductUsageFactorySessionsList.FACTORY)
                      .append(FACTORY_COUNT, new BasicDBObject("$sum", 1))
                      .append(ProductUsageFactorySessionsList.TIME, new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.TIME))
                      .append(ProductUsageFactorySessionsList.BUILD + "_count", new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.BUILD))
                      .append(ProductUsageFactorySessionsList.RUN + "_count", new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.RUN))
                      .append(ProductUsageFactorySessionsList.DEPLOY + "_count", new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.DEPLOY))
                      .append(ProductUsageFactorySessionsList.AUTHENTICATED_SESSION + "_count", 
                              new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.AUTHENTICATED_SESSION))
                      .append(ProductUsageFactorySessionsList.CONVERTED_SESSION + "_count", 
                              new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.CONVERTED_SESSION))
                      ));
                
        dbOperations.add(new BasicDBObject("$project", 
            new BasicDBObject(ProductUsageFactorySessionsList.TIME, 1)
                      .append(FACTORY_COUNT, 1)
                      .append(ProductUsageFactorySessionsList.FACTORY, "$_id")
                      .append("_id", 0)
                      .append(BUILD_RATE, getRateOperation("$" + ProductUsageFactorySessionsList.BUILD + "_count", "$" + FACTORY_COUNT))
                      .append(RUN_RATE, getRateOperation("$" + ProductUsageFactorySessionsList.RUN + "_count", "$" + FACTORY_COUNT))
                      .append(DEPLOY_RATE, getRateOperation("$" + ProductUsageFactorySessionsList.DEPLOY + "_count", "$" + FACTORY_COUNT))
                      .append(AUTHENTICATED_FACTORY_SESSION_RATE,
                              getRateOperation("$" + ProductUsageFactorySessionsList.AUTHENTICATED_SESSION + "_count", "$" + FACTORY_COUNT))
                      .append(CONVERTED_FACTORY_SESSION_RATE, 
                              getRateOperation("$" + ProductUsageFactorySessionsList.CONVERTED_SESSION + "_count", "$" + FACTORY_COUNT))
                      ));

        dbOperations.add(new BasicDBObject("$project", 
            new BasicDBObject(ProductUsageFactorySessionsList.FACTORY, 1)
                      .append(ProductUsageFactorySessionsList.TIME, 1)
                      .append(FACTORY_COUNT, 1)
                      .append(BUILD_RATE, 1)
                      .append(RUN_RATE, 1)
                      .append(DEPLOY_RATE, 1)                      
                      .append(ANONYMOUS_FACTORY_SESSION_RATE, getSubtractOperation(100, "$" + AUTHENTICATED_FACTORY_SESSION_RATE))
                      .append(AUTHENTICATED_FACTORY_SESSION_RATE, 1)
                      .append(ABANDON_FACTORY_SESSION_RATE, getSubtractOperation(100, "$" + CONVERTED_FACTORY_SESSION_RATE))
                      .append(CONVERTED_FACTORY_SESSION_RATE, 1)                      
                      ));
        
        dbOperations.add(new BasicDBObject("$sort", new BasicDBObject(ProductUsageFactorySessionsList.TIME, -1)));
        dbOperations.add(new BasicDBObject("$limit", MAX_DOCUMENT_COUNT));

        return dbOperations.toArray(new DBObject[0]);
    }

    /**
     * @return mongodb operation (100% * (subjectField / predicateField))
     */
    private BasicDBObject getRateOperation(String subjectField, String predicateField) {        
        BasicDBList divideArgs = new BasicDBList();
        divideArgs.add(subjectField);
        divideArgs.add(predicateField);
        
        BasicDBList multiplyArgs = new BasicDBList();
        multiplyArgs.add(100);
        multiplyArgs.add(new BasicDBObject("$divide", divideArgs));
        
        BasicDBObject rateOperation = new BasicDBObject("$multiply", multiplyArgs);
        
        return rateOperation;        
    }

    /** 
     * @return mongodb operation (arg1 - arg2Field)
     */
    private BasicDBObject getSubtractOperation(long arg1, String arg2Field) {
        BasicDBList subtractArgs = new BasicDBList();
        subtractArgs.add(arg1);
        subtractArgs.add(arg2Field);

        BasicDBObject subtractOperation = new BasicDBObject("$subtract", subtractArgs); 
        
        return subtractOperation;
    }
    
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }
}
