/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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


/** @author Dmytro Nochevnov */
public abstract class AbstractTopReferrers extends AbstractTopMetrics {   
    public static final String REFERRER_COUNT = "referrer_count";
    public static final String BUILD_RATE = "build_rate";
    public static final String RUN_RATE = "run_rate";
    public static final String DEPLOY_RATE = "deploy_rate";
    public static final String ANONYMOUS_FACTORY_SESSION_RATE = "anonymous_factory_session_rate";
    public static final String AUTHENTICATED_FACTORY_SESSION_RATE = "authenticated_factory_session_rate";
    public static final String ABANDON_FACTORY_SESSION_RATE = "abandon_factory_session_rate";
    public static final String CONVERTED_FACTORY_SESSION_RATE = "converted_factory_session_rate";
    public static final String FIRST_SESSION_DATE = "first_session_date";
    public static final String LAST_SESSION_DATE = "last_session_date";
    
    public AbstractTopReferrers(MetricType factoryMetricType, int dayCount) {
        super(factoryMetricType, dayCount);
    }
    
    @Override
    public String[] getTrackedFields() {
        return new String[]{ProductUsageFactorySessionsList.REFERRER, 
                            ProductUsageFactorySessionsList.WS_CREATED,
                            ProductUsageFactorySessionsList.USER_CREATED,
                            ProductUsageFactorySessionsList.TIME,
                            BUILD_RATE,
                            RUN_RATE,
                            DEPLOY_RATE,
                            ANONYMOUS_FACTORY_SESSION_RATE,
                            AUTHENTICATED_FACTORY_SESSION_RATE,
                            ABANDON_FACTORY_SESSION_RATE,
                            CONVERTED_FACTORY_SESSION_RATE,
                            FIRST_SESSION_DATE,
                            LAST_SESSION_DATE
                            };
    }

    /**
     * Filter documents with non-exists referrer field, or referrer = "". 
db.product_usage_factory_sessions_list.aggregate([
              {$match: {$and: [{referrer: {$exists: true}}, 
                               {referrer: {$ne: ""}}]}
              },
              {$group: 
                      {_id: "$referrer",
                       ws_created: {$sum: "$ws_created"},
                       user_created: {$sum: "$user_created"},
                       referrer_count: {$sum: 1}, 
                       time: {$sum:"$time"},
                       build_count: {$sum:"$build"},
                       run_count: {$sum: "$run"},
                       deploy_count: {$sum: "$deploy"},
                       authenticated_factory_session_count: {$sum: "$authenticated_factory_session"},
                       converted_factory_session_count: {$sum: "$converted_factory_session"},
                       first_session_date: {$first: "$date"},
                       last_session_date: {$last: "$date"}
                      }
              },
              {$project: {
                         referrer: "$_id", 
                         ws_created: 1,
                         user_created: 1,
                         time: 1, 
                         _id: 0,
                         build_rate: {$multiply: [100, 
                                                  {$divide: ["$build_count", "$referrer_count"]}]},
                         run_rate: {$multiply: [100, 
                                                {$divide: ["$run_count", "$referrer_count"]}]},
                         deploy_rate: {$multiply: [100, 
                                                   {$divide: ["$deploy_count", "$referrer_count"]}]},
                         authenticated_factory_session_rate: {$multiply: [100, 
                                                                         {$divide: ["$authenticated_factory_session_count", "$referrer_count"]}]},
                         converted_factory_session_rate: {$multiply: [100, 
                                                   {$divide: ["$converted_factory_session_count", "$referrer_count"]}]},
                         first_session_date: 1,
                         last_session_date: 1
                      }
              },
              {$project: {
                         referrer:1,
                         ws_created: 1,
                         user_created: 1,
                         time: 1, 
                         build_rate:1,
                         run_rate:1,
                         deploy_rate:1,
                         anonymous_factory_session_rate: {$subtract: [100, "$authenticated_factory_session_rate"]},
                         authenticated_factory_session_rate: 1,
                         abandon_factory_session_rate: {$subtract: [100, "$converted_factory_session_rate"]},
                         converted_factory_session_rate: 1,
                         first_session_date: 1,
                         last_session_date: 1
                      }

              },
              {$sort:{time: -1}},
              {$limit: 100}
       ]) 
     */
    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        dbOperations.add(new BasicDBObject("$match",
            getAndOperation(new BasicDBObject(ProductUsageFactorySessionsList.REFERRER, new BasicDBObject("$exists", true)),  
                            new BasicDBObject(ProductUsageFactorySessionsList.REFERRER, new BasicDBObject("$ne", "")))                              
        ));
        
        dbOperations.add(new BasicDBObject("$group",
            new BasicDBObject("_id", "$" + ProductUsageFactorySessionsList.REFERRER)
                      .append(ProductUsageFactorySessionsList.WS_CREATED, new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.WS_CREATED))
                      .append(ProductUsageFactorySessionsList.USER_CREATED, new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.USER_CREATED))
                      .append(REFERRER_COUNT, new BasicDBObject("$sum", 1))
                      .append(ProductUsageFactorySessionsList.TIME, new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.TIME))
                      .append(ProductUsageFactorySessionsList.BUILD + "_count", new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.BUILD))
                      .append(ProductUsageFactorySessionsList.RUN + "_count", new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.RUN))
                      .append(ProductUsageFactorySessionsList.DEPLOY + "_count", new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.DEPLOY))
                      .append(ProductUsageFactorySessionsList.AUTHENTICATED_SESSION + "_count", 
                              new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.AUTHENTICATED_SESSION))
                      .append(ProductUsageFactorySessionsList.CONVERTED_SESSION + "_count", 
                              new BasicDBObject("$sum", "$" + ProductUsageFactorySessionsList.CONVERTED_SESSION))
                      .append(FIRST_SESSION_DATE, new BasicDBObject("$first", "$" + ProductUsageFactorySessionsList.DATE))
                      .append(LAST_SESSION_DATE, new BasicDBObject("$last", "$" + ProductUsageFactorySessionsList.DATE))
                      ));
                
        dbOperations.add(new BasicDBObject("$project", 
            new BasicDBObject(ProductUsageFactorySessionsList.TIME, 1)
                      .append(ProductUsageFactorySessionsList.WS_CREATED, 1)
                      .append(ProductUsageFactorySessionsList.USER_CREATED, 1)
                      .append(ProductUsageFactorySessionsList.REFERRER, "$_id")
                      .append("_id", 0)
                      .append(BUILD_RATE, getRateOperation("$" + ProductUsageFactorySessionsList.BUILD + "_count", "$" + REFERRER_COUNT))
                      .append(RUN_RATE, getRateOperation("$" + ProductUsageFactorySessionsList.RUN + "_count", "$" + REFERRER_COUNT))
                      .append(DEPLOY_RATE, getRateOperation("$" + ProductUsageFactorySessionsList.DEPLOY + "_count", "$" + REFERRER_COUNT))
                      .append(AUTHENTICATED_FACTORY_SESSION_RATE,
                              getRateOperation("$" + ProductUsageFactorySessionsList.AUTHENTICATED_SESSION + "_count", "$" + REFERRER_COUNT))
                      .append(CONVERTED_FACTORY_SESSION_RATE, 
                              getRateOperation("$" + ProductUsageFactorySessionsList.CONVERTED_SESSION + "_count", "$" + REFERRER_COUNT))
                      .append(FIRST_SESSION_DATE, 1)
                      .append(LAST_SESSION_DATE, 1)
                              
                      ));

        dbOperations.add(new BasicDBObject("$project", 
            new BasicDBObject(ProductUsageFactorySessionsList.REFERRER, 1)
                      .append(ProductUsageFactorySessionsList.WS_CREATED, 1)
                      .append(ProductUsageFactorySessionsList.USER_CREATED, 1)
                      .append(ProductUsageFactorySessionsList.TIME, 1)
                      .append(BUILD_RATE, 1)
                      .append(RUN_RATE, 1)
                      .append(DEPLOY_RATE, 1)                      
                      .append(ANONYMOUS_FACTORY_SESSION_RATE, getSubtractOperation(100, "$" + AUTHENTICATED_FACTORY_SESSION_RATE))
                      .append(AUTHENTICATED_FACTORY_SESSION_RATE, 1)
                      .append(ABANDON_FACTORY_SESSION_RATE, getSubtractOperation(100, "$" + CONVERTED_FACTORY_SESSION_RATE))
                      .append(CONVERTED_FACTORY_SESSION_RATE, 1)
                      .append(FIRST_SESSION_DATE, 1)
                      .append(LAST_SESSION_DATE, 1)
                      ));
        
        dbOperations.add(new BasicDBObject("$sort", new BasicDBObject(ProductUsageFactorySessionsList.TIME, -1)));
        dbOperations.add(new BasicDBObject("$limit", MAX_DOCUMENT_COUNT));

        return dbOperations.toArray(new DBObject[0]);
    }

    private BasicDBObject getAndOperation(BasicDBObject... predicates) {
        BasicDBList andArgs = new BasicDBList();

        for (BasicDBObject predicate: predicates) {
            andArgs.add(predicate);
        }
        
        BasicDBObject andOperation = new BasicDBObject("$and", andArgs);
        
        return andOperation;
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
