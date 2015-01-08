/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.api.dao.mongo;

import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionQueryBuilder;
import com.codenvy.api.account.shared.dto.SubscriptionState;
import com.codenvy.api.core.ServerException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import static com.codenvy.api.dao.mongo.AccountDaoImpl.SUBSCRIPTION_COLLECTION;
import static com.codenvy.api.dao.mongo.AccountDaoImpl.toSubscription;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Alexander Garagatyi
 */
public class MongoSubscriptionQueryBuilder implements SubscriptionQueryBuilder {
    private final DBCollection subscriptionCollection;

    @Inject
    public MongoSubscriptionQueryBuilder(DB db,
                                         @Named(SUBSCRIPTION_COLLECTION) String subscriptionCollectionName) {
        subscriptionCollection = db.getCollection(subscriptionCollectionName);
    }

    @Override
    public SubscriptionQuery getTrialQuery(final String service, final String accountId) {
        return new SubscriptionQuery() {
            @Override
            public List<Subscription> execute() throws ServerException {
                final BasicDBObject query = new BasicDBObject("accountId", accountId);
                query.append("serviceId", service);
                query.append("trialEndDate", new BasicDBObject("$type", 10));

                return executeQuery(query);
            }
        };
    }

    @Override
    public SubscriptionQuery getChargeQuery(final String service) {
        return new SubscriptionQuery() {
            @Override
            public List<Subscription> execute() throws ServerException {
                final BasicDBObject query = new BasicDBObject("serviceId", service);
                query.append("usePaymentSystem", true);
                query.append("state", SubscriptionState.ACTIVE.toString());
                query.append("nextBillingDate", new BasicDBObject("$lt", new Date()));

                return executeQuery(query);
            }
        };
    }

    @Override
    public SubscriptionQuery getExpiringQuery(final String service, final int days) {
        return new SubscriptionQuery() {
            @Override
            public List<Subscription> execute() throws ServerException {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, days);

                final BasicDBObject query = new BasicDBObject("serviceId", service);
                query.append("state", SubscriptionState.ACTIVE.toString());
                query.append("trialEndDate", new BasicDBObject("$lt", calendar.getTime()));
                query.append(String.format("codenvy:subscription-email-trialExpiring-%s", days), null);

                return executeQuery(query);
            }
        };
    }

    @Override
    public SubscriptionQuery getExpiredQuery(final String service, final int days) {
        return new SubscriptionQuery() {
            @Override
            public List<Subscription> execute() throws ServerException {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -days);

                final BasicDBObject query = new BasicDBObject("serviceId", service);
                query.append("state", SubscriptionState.INACTIVE.toString());
                query.append("trialEndDate", new BasicDBObject("$lt", calendar.getTime()));
                query.append(String.format("codenvy:subscription-email-trialExpired-%s", days), null);
                query.append("trialEndDate", new BasicDBObject("$lt", new Date()));

                return executeQuery(query);
            }
        };
    }

    @Override
    public SubscriptionQuery getTrialExpiredQuery(final String service) {
        return new SubscriptionQuery() {
            @Override
            public List<Subscription> execute() throws ServerException {
                final BasicDBObject query = new BasicDBObject("serviceId", service);
                query.append("state", SubscriptionState.ACTIVE.toString());
                query.append("trialEndDate", new BasicDBObject("$lt", new Date()));

                return executeQuery(query);
            }
        };
    }

    private List<Subscription> executeQuery(DBObject query) throws ServerException {
        final List<Subscription> result = new ArrayList<>();
        try (DBCursor subscriptions = subscriptionCollection.find(query)) {
            for (DBObject subscription : subscriptions) {
                result.add(toSubscription(subscription));
            }
        } catch (MongoException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
        return result;
    }
}
