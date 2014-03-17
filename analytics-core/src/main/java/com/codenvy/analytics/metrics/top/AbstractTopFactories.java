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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;


/** @author Dmytro Nochevnov */
public abstract class AbstractTopFactories extends AbstractTopMetrics {

    public static final String FACTORY_COUNT                      = "factory_count";
    public static final String BUILD_RATE                         = "build_rate";
    public static final String RUN_RATE                           = "run_rate";
    public static final String DEPLOY_RATE                        = "deploy_rate";
    public static final String ANONYMOUS_FACTORY_SESSION_RATE     = "anonymous_factory_session_rate";
    public static final String AUTHENTICATED_FACTORY_SESSION_RATE = "authenticated_factory_session_rate";
    public static final String ABANDON_FACTORY_SESSION_RATE       = "abandon_factory_session_rate";
    public static final String CONVERTED_FACTORY_SESSION_RATE     = "converted_factory_session_rate";

    public AbstractTopFactories(MetricType factoryMetricType, int dayCount) {
        super(factoryMetricType, dayCount);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{FACTORY,
                            WS_CREATED,
                            USER_CREATED,
                            TIME,
                            BUILD_RATE,
                            RUN_RATE,
                            DEPLOY_RATE,
                            ANONYMOUS_FACTORY_SESSION_RATE,
                            AUTHENTICATED_FACTORY_SESSION_RATE,
                            ABANDON_FACTORY_SESSION_RATE,
                            CONVERTED_FACTORY_SESSION_RATE};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        dbOperations.add(new BasicDBObject("$match", new BasicDBObject(FACTORY, new BasicDBObject("$ne", ""))));
        dbOperations.add(new BasicDBObject("$match", new BasicDBObject(FACTORY, new BasicDBObject("$ne", null))));

        dbOperations.add(
                new BasicDBObject("$group",
                                  new BasicDBObject(ID, "$" + FACTORY)
                                          .append(WS_CREATED, new BasicDBObject("$sum", "$" + WS_CREATED))
                                          .append(USER_CREATED, new BasicDBObject("$sum", "$" + USER_CREATED))
                                          .append(FACTORY_COUNT, new BasicDBObject("$sum", 1))
                                          .append(TIME, new BasicDBObject("$sum", "$" + TIME))
                                          .append(BUILD + "_count", new BasicDBObject("$sum", "$" + BUILD))
                                          .append(RUN + "_count", new BasicDBObject("$sum", "$" + RUN))
                                          .append(DEPLOY + "_count", new BasicDBObject("$sum", "$" + DEPLOY))
                                          .append(AUTHENTICATED_SESSION + "_count",
                                                  new BasicDBObject("$sum", "$" + AUTHENTICATED_SESSION))
                                          .append(CONVERTED_SESSION + "_count",
                                                  new BasicDBObject("$sum", "$" + CONVERTED_SESSION))));

        dbOperations.add(
                new BasicDBObject(
                        "$project",
                        new BasicDBObject(ID, 0)
                                .append(WS_CREATED, 1)
                                .append(USER_CREATED, 1)
                                .append(FACTORY, "$_id")
                                .append(TIME, 1)
                                .append(BUILD_RATE, getRateOperation("$" + BUILD + "_count", "$" + FACTORY_COUNT))
                                .append(RUN_RATE, getRateOperation("$" + RUN + "_count", "$" + FACTORY_COUNT))
                                .append(DEPLOY_RATE, getRateOperation("$" + DEPLOY + "_count", "$" + FACTORY_COUNT))
                                .append(AUTHENTICATED_FACTORY_SESSION_RATE,
                                        getRateOperation("$" + AUTHENTICATED_SESSION + "_count",
                                                         "$" + FACTORY_COUNT))
                                .append(CONVERTED_FACTORY_SESSION_RATE,
                                        getRateOperation("$" + CONVERTED_SESSION + "_count",
                                                         "$" + FACTORY_COUNT))
                ));

        dbOperations.add(
                new BasicDBObject
                        ("$project",
                         new BasicDBObject(FACTORY, 1)
                                 .append(WS_CREATED, 1)
                                 .append(USER_CREATED, 1)
                                 .append(TIME, 1)
                                 .append(CONVERTED_FACTORY_SESSION_RATE, 1)
                                 .append(AUTHENTICATED_FACTORY_SESSION_RATE, 1)
                                 .append(BUILD_RATE, 1)
                                 .append(RUN_RATE, 1)
                                 .append(DEPLOY_RATE, 1)
                                 .append(ANONYMOUS_FACTORY_SESSION_RATE,
                                         getSubtractOperation(100, "$" + AUTHENTICATED_FACTORY_SESSION_RATE))
                                 .append(ABANDON_FACTORY_SESSION_RATE,
                                         getSubtractOperation(100, "$" + CONVERTED_FACTORY_SESSION_RATE))
                        ));

        dbOperations.add(new BasicDBObject("$sort", new BasicDBObject(TIME, -1)));
        dbOperations.add(new BasicDBObject("$limit", MAX_DOCUMENT_COUNT));

        return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }
}
