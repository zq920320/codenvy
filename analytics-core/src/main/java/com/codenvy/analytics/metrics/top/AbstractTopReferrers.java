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

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.sessions.factory.ProductUsageFactorySessionsList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/** @author Dmytro Nochevnov */
public abstract class AbstractTopReferrers extends AbstractTopMetrics {
    public static final String REFERRER_COUNT                     = "referrer_count";
    public static final String BUILD_RATE                         = "build_rate";
    public static final String RUN_RATE                           = "run_rate";
    public static final String DEPLOY_RATE                        = "deploy_rate";
    public static final String ANONYMOUS_FACTORY_SESSION_RATE     = "anonymous_factory_session_rate";
    public static final String AUTHENTICATED_FACTORY_SESSION_RATE = "authenticated_factory_session_rate";
    public static final String ABANDON_FACTORY_SESSION_RATE       = "abandon_factory_session_rate";
    public static final String CONVERTED_FACTORY_SESSION_RATE     = "converted_factory_session_rate";
    public static final String FIRST_SESSION_DATE                 = "first_session_date";
    public static final String LAST_SESSION_DATE                  = "last_session_date";

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

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        dbOperations.add(
                new BasicDBObject(
                        "$match",
                        getAndOperation(
                                new BasicDBObject(ProductUsageFactorySessionsList.REFERRER,
                                                  new BasicDBObject("$ne", "")))));

        dbOperations.add(
                new BasicDBObject(
                        "$group",
                        new BasicDBObject("_id", "$" + ProductUsageFactorySessionsList.REFERRER)
                                .append(ProductUsageFactorySessionsList.WS_CREATED,
                                        new BasicDBObject("$sum",
                                                          "$" + ProductUsageFactorySessionsList.WS_CREATED))
                                .append(ProductUsageFactorySessionsList.USER_CREATED,
                                        new BasicDBObject("$sum",
                                                          "$" + ProductUsageFactorySessionsList.USER_CREATED))
                                .append(REFERRER_COUNT, new BasicDBObject("$sum", 1))
                                .append(ProductUsageFactorySessionsList.TIME,
                                        new BasicDBObject("$sum",
                                                          "$" + ProductUsageFactorySessionsList.TIME))
                                .append(ProductUsageFactorySessionsList.BUILD + "_count",
                                        new BasicDBObject("$sum",
                                                          "$" + ProductUsageFactorySessionsList.BUILD))
                                .append(ProductUsageFactorySessionsList.RUN + "_count",
                                        new BasicDBObject("$sum",
                                                          "$" + ProductUsageFactorySessionsList.RUN))
                                .append(ProductUsageFactorySessionsList.DEPLOY + "_count",
                                        new BasicDBObject("$sum", "$" +
                                                                  ProductUsageFactorySessionsList
                                                                          .DEPLOY))
                                .append(ProductUsageFactorySessionsList.AUTHENTICATED_SESSION + "_count",
                                        new BasicDBObject("$sum",
                                                          "$" + ProductUsageFactorySessionsList.AUTHENTICATED_SESSION))
                                .append(ProductUsageFactorySessionsList.CONVERTED_SESSION + "_count",
                                        new BasicDBObject("$sum",
                                                          "$" + ProductUsageFactorySessionsList.CONVERTED_SESSION))
                                .append(FIRST_SESSION_DATE,
                                        new BasicDBObject("$first",
                                                          "$" + DATE))
                                .append(LAST_SESSION_DATE,
                                        new BasicDBObject("$last",
                                                          "$" + DATE))
                ));

        dbOperations.add(
                new BasicDBObject(
                        "$project",
                        new BasicDBObject(ProductUsageFactorySessionsList.TIME, 1)
                                .append(ProductUsageFactorySessionsList.WS_CREATED, 1)
                                .append(ProductUsageFactorySessionsList.USER_CREATED, 1)
                                .append(ProductUsageFactorySessionsList.REFERRER, "$_id")
                                .append(FIRST_SESSION_DATE, 1)
                                .append(LAST_SESSION_DATE, 1)
                                .append("_id", 0)
                                .append(BUILD_RATE,
                                        getRateOperation(
                                                "$" + ProductUsageFactorySessionsList.BUILD + "_count",
                                                "$" + REFERRER_COUNT))
                                .append(RUN_RATE,
                                        getRateOperation(
                                                "$" + ProductUsageFactorySessionsList.RUN + "_count",
                                                "$" + REFERRER_COUNT))
                                .append(DEPLOY_RATE,
                                        getRateOperation(
                                                "$" + ProductUsageFactorySessionsList.DEPLOY + "_count",
                                                "$" + REFERRER_COUNT))
                                .append(AUTHENTICATED_FACTORY_SESSION_RATE,
                                        getRateOperation(
                                                "$" + ProductUsageFactorySessionsList.AUTHENTICATED_SESSION +
                                                "_count", "$" + REFERRER_COUNT))
                                .append(CONVERTED_FACTORY_SESSION_RATE,
                                        getRateOperation(
                                                "$" + ProductUsageFactorySessionsList.CONVERTED_SESSION +
                                                "_count", "$" + REFERRER_COUNT))

                ));

        dbOperations.add(
                new BasicDBObject(
                        "$project",
                        new BasicDBObject(ProductUsageFactorySessionsList.REFERRER, 1)
                                .append(ProductUsageFactorySessionsList.WS_CREATED, 1)
                                .append(ProductUsageFactorySessionsList.USER_CREATED, 1)
                                .append(ProductUsageFactorySessionsList.TIME, 1)
                                .append(BUILD_RATE, 1)
                                .append(RUN_RATE, 1)
                                .append(DEPLOY_RATE, 1)
                                .append(FIRST_SESSION_DATE, 1)
                                .append(LAST_SESSION_DATE, 1)
                                .append(CONVERTED_FACTORY_SESSION_RATE, 1)
                                .append(AUTHENTICATED_FACTORY_SESSION_RATE, 1)
                                .append(ANONYMOUS_FACTORY_SESSION_RATE,
                                        getSubtractOperation(
                                                100,
                                                "$" + AUTHENTICATED_FACTORY_SESSION_RATE))
                                .append(ABANDON_FACTORY_SESSION_RATE,
                                        getSubtractOperation(
                                                100,
                                                "$" + CONVERTED_FACTORY_SESSION_RATE))
                ));

        dbOperations.add(new BasicDBObject("$sort", new BasicDBObject(ProductUsageFactorySessionsList.TIME, -1)));
        dbOperations.add(new BasicDBObject("$limit", MAX_DOCUMENT_COUNT));

        return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }
}
