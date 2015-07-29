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
package com.codenvy.api.subscription.server.dao.mongo;

import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.server.dao.SubscriptionQueryBuilder;
import com.codenvy.api.subscription.shared.dto.BillingCycleType;
import com.codenvy.api.subscription.shared.dto.SubscriptionState;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Sergii Leschenko
 */
public class SubscriptionDaoImpl implements SubscriptionDao {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionDaoImpl.class);

    private static final String SUBSCRIPTION_COLLECTION = "organization.storage.db.subscription.collection";

    private final DBCollection             subscriptionCollection;
    private final AccountDao               accountDao;
    private final SubscriptionQueryBuilder subscriptionQueryBuilder;

    @Inject
    public SubscriptionDaoImpl(DB db,
                               @Named(SUBSCRIPTION_COLLECTION) String subscriptionCollectionName,
                               AccountDao accountDao,
                               SubscriptionQueryBuilder subscriptionQueryBuilder) {
        this.accountDao = accountDao;
        this.subscriptionQueryBuilder = subscriptionQueryBuilder;
        subscriptionCollection = db.getCollection(subscriptionCollectionName);
        subscriptionCollection.createIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        subscriptionCollection.createIndex(new BasicDBObject("accountId", 1));
        subscriptionCollection.createIndex(new BasicDBObject("state", 1));
        subscriptionCollection.createIndex(new BasicDBObject("serviceId", 1));
        subscriptionCollection.createIndex(new BasicDBObject("nextBillingDate", 1));
        subscriptionCollection.createIndex(new BasicDBObject("endDate", 1));
    }

    @Override
    public List<Subscription> getActive(String accountId) throws ServerException, NotFoundException {
        //ensure existence of account
        accountDao.getById(accountId);

        try {
            final BasicDBObject query = new BasicDBObject("accountId", accountId);
            query.append("state", "ACTIVE");

            final List<Subscription> result = new ArrayList<>();
            try (DBCursor subscriptions = subscriptionCollection.find(query)) {
                for (DBObject currentSubscription : subscriptions) {
                    result.add(toSubscription(currentSubscription));
                }
            }

            return result;
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve subscriptions");
        }
    }

    @Override
    public List<Subscription> getByAccountId(String accountId) throws ServerException {
        try {
            final BasicDBObject query = new BasicDBObject("accountId", accountId);

            final List<Subscription> result = new ArrayList<>();
            try (DBCursor subscriptions = subscriptionCollection.find(query)) {
                for (DBObject currentSubscription : subscriptions) {
                    result.add(toSubscription(currentSubscription));
                }
            }

            return result;
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve subscriptions");
        }
    }

    @Override
    public Subscription getActiveByServiceId(String accountId, String serviceId) throws ServerException, NotFoundException {
        //ensure existence of account
        accountDao.getById(accountId);
        try {
            final BasicDBObject query = new BasicDBObject("accountId", accountId);
            query.append("state", "ACTIVE");
            query.append("serviceId", serviceId);

            final DBObject dbSubscription = subscriptionCollection.findOne(query);

            if (dbSubscription != null) {
                return toSubscription(dbSubscription);
            }

            return null;
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve subscriptions");
        }
    }

    @Override
    public Subscription getById(String subscriptionId) throws NotFoundException, ServerException {
        try {
            final DBObject subscriptionObj = subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId));
            if (null == subscriptionObj) {
                throw new NotFoundException("Subscription not found " + subscriptionId);
            }
            return toSubscription(subscriptionObj);
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve subscription");
        }
    }

    @Override
    public void update(Subscription subscription) throws NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", subscription.getId());
        try {
            if (null == subscriptionCollection.findOne(query)) {
                throw new NotFoundException("Subscription not found " + subscription.getId());
            }
            subscriptionCollection.update(query, toDBObject(subscription));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to update subscription");
        }
    }

    @Override
    public void create(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        try {
            ensureConsistency(subscription);
            //ensure existence of account
            accountDao.getById(subscription.getAccountId());
            subscriptionCollection.save(toDBObject(subscription));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to persist subscription");
        }
    }

    @Override
    public void remove(String subscriptionId) throws NotFoundException, ServerException {
        try {
            if (null == subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId))) {
                LOG.warn(format("Subscription with id = %s, cant be removed because it doesn't exist", subscriptionId));
                throw new NotFoundException("Subscription not found " + subscriptionId);
            }
            subscriptionCollection.remove(new BasicDBObject("id", subscriptionId));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to remove subscription");
        }
    }

    @Override
    public void deactivate(String subscriptionId) throws NotFoundException, ServerException {
        try {
            final BasicDBObject query = new BasicDBObject("id", subscriptionId);
            final DBObject subscriptionObj;

            if (null == (subscriptionObj = subscriptionCollection.findOne(query))) {
                LOG.warn(format("Subscription with id = %s, cant be removed because it doesn't exist", subscriptionId));
                throw new NotFoundException("Subscription not found " + subscriptionId);
            }

            Subscription subscription = toSubscription(subscriptionObj);
            subscription.setState(SubscriptionState.INACTIVE);

            subscriptionCollection.update(query, toDBObject(subscription));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to remove subscription");
        }
    }

    public SubscriptionQueryBuilder getSubscriptionQueryBuilder() {
        return subscriptionQueryBuilder;
    }

    /**
     * Converts database object to subscription ready-to-use object
     */
    static Subscription toSubscription(Object dbObject) {
        final BasicDBObject basicSubscriptionObj = (BasicDBObject)dbObject;
        @SuppressWarnings("unchecked") //properties is always Map of Strings
        final Map<String, String> properties = new HashMap<>((Map)basicSubscriptionObj.get("properties"));

        Subscription subscription = new Subscription().withId(basicSubscriptionObj.getString("id"))
                                                      .withAccountId(basicSubscriptionObj.getString("accountId"))
                                                      .withServiceId(basicSubscriptionObj.getString("serviceId"))
                                                      .withPlanId(basicSubscriptionObj.getString("planId"))
                                                      .withProperties(properties)
                                                      .withStartDate(basicSubscriptionObj.getDate("startDate"))
                                                      .withEndDate(basicSubscriptionObj.getDate("endDate"))
                                                      .withBillingStartDate(basicSubscriptionObj.getDate("billingStartDate"))
                                                      .withBillingEndDate(basicSubscriptionObj.getDate("billingEndDate"))
                                                      .withNextBillingDate(basicSubscriptionObj.getDate("nextBillingDate"))
                                                      .withBillingContractTerm(basicSubscriptionObj.containsField("billingContractTerm") ?
                                                                               basicSubscriptionObj.getInt("billingContractTerm") :
                                                                               null)
                                                      .withBillingCycle(basicSubscriptionObj.containsField("billingCycle") ?
                                                                        basicSubscriptionObj.getInt("billingCycle") :
                                                                        null)
                                                      .withDescription(basicSubscriptionObj.getString("description"))
                                                      .withUsePaymentSystem(basicSubscriptionObj.containsField("usePaymentSystem") ?
                                                                            basicSubscriptionObj.getBoolean("usePaymentSystem") :
                                                                            null);

        final String state = basicSubscriptionObj.getString("state");
        if (state != null) {
            subscription.setState(SubscriptionState.valueOf(state));
        }

        final String billingCycleType = basicSubscriptionObj.getString("billingCycleType");
        if (billingCycleType != null) {
            subscription.setBillingCycleType(BillingCycleType.valueOf(billingCycleType));
        }

        return subscription;
    }

    /**
     * Converts subscription to database ready-to-use object
     */
    DBObject toDBObject(Subscription subscription) {
        final DBObject properties = new BasicDBObject();
        properties.putAll(subscription.getProperties());
        return new BasicDBObject().append("id", subscription.getId())
                                  .append("accountId", subscription.getAccountId())
                                  .append("planId", subscription.getPlanId())
                                  .append("serviceId", subscription.getServiceId())
                                  .append("properties", properties)
                                  .append("usePaymentSystem", subscription.getUsePaymentSystem())
                                  .append("billingContractTerm", subscription.getBillingContractTerm())
                                  .append("description", subscription.getDescription())
                                  .append("state", subscription.getState().toString())
                                  .append("startDate", subscription.getStartDate())
                                  .append("endDate", subscription.getEndDate())
                                  .append("billingCycle", subscription.getBillingCycle())
                                  .append("billingCycleType", subscription.getBillingCycleType().toString())
                                  .append("billingStartDate", subscription.getBillingStartDate())
                                  .append("billingEndDate", subscription.getBillingEndDate())
                                  .append("nextBillingDate", subscription.getNextBillingDate());

    }

    /**
     * Check that subscription object has legal state
     *
     * @throws org.eclipse.che.api.core.ConflictException
     *         when end date goes before start date or subscription state is not set
     */
    private void ensureConsistency(Subscription subscription) throws ConflictException {
        if (subscription == null) {
            throw new ConflictException("Subscription information is missing");
        }
        if (subscription.getPlanId() == null) {
            throw new ConflictException("Plan id is missing");
        }
        if (subscription.getServiceId() == null) {
            throw new ConflictException("Subscription service id is missing");
        }
        if (subscription.getAccountId() == null) {
            throw new ConflictException("Subscription account id is missing");
        }
        if (subscription.getId() == null) {
            throw new ConflictException("Subscription id is missing");
        }
        if (subscription.getProperties() == null) {
            throw new ConflictException("Subscription properties are missing");
        }
        if (subscription.getUsePaymentSystem() == null) {
            throw new ConflictException("Subscription parameter usePaymentSystem is missing");
        }
        if (subscription.getBillingContractTerm() == null) {
            throw new ConflictException("Subscription parameter billingContractTerm is missing");
        }
        if (subscription.getDescription() == null) {
            throw new ConflictException("Subscription description is missing");
        }
        if (subscription.getState() == null) {
            throw new ConflictException("Subscription state is missing");
        }
        if (subscription.getBillingCycle() == null) {
            throw new ConflictException("Subscription parameter billingCycle is missing");
        }
        if (subscription.getBillingCycleType() == null) {
            throw new ConflictException("Subscription parameter billingCycleType is missing");
        }
    }

}
