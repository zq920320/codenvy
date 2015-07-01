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
import com.codenvy.api.subscription.server.dao.SubscriptionQueryBuilder;
import com.codenvy.api.subscription.shared.dto.BillingCycleType;
import com.codenvy.api.subscription.shared.dto.SubscriptionState;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.subscription.server.dao.mongo.SubscriptionDaoImpl.toSubscription;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * Tests for {@link SubscriptionDaoImpl}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SubscriptionDaoImplTest extends BaseDaoTest {
    private static final String ACC_COLL_NAME          = "accounts";
    private static final String SUBSCRIPTION_COLL_NAME = "subscriptions";

    @Mock
    private SubscriptionQueryBuilder subscriptionQueryBuilder;
    @Mock
    private AccountDao               accountDao;

    private SubscriptionDaoImpl subscriptionDao;
    private DBCollection        collection;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(ACC_COLL_NAME);
        db = spy(db);
        collection = spy(db.getCollection(ACC_COLL_NAME));
        collection = spy(db.getCollection(SUBSCRIPTION_COLL_NAME));
        when(db.getCollection(ACC_COLL_NAME)).thenReturn(collection);
        when(db.getCollection(SUBSCRIPTION_COLL_NAME)).thenReturn(collection);
        subscriptionDao = new SubscriptionDaoImpl(db,
                                                  SUBSCRIPTION_COLL_NAME,
                                                  accountDao,
                                                  subscriptionQueryBuilder);
    }

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void shouldBeAbleToUpdateSubscription() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        //prepare update
        Date updatedDate = new Date();
        subscription.withPlanId(subscription.getPlanId() + "suffix")
                    .withServiceId(subscription.getServiceId() + "suffix")
                    .withAccountId("updatedAccountId")
                    .withProperties(Collections.singletonMap("updatedKey", "updatedValue"))
                    .withDescription("updated description")
                    .withUsePaymentSystem(false)
                    .withState(SubscriptionState.INACTIVE)
                    .withStartDate(updatedDate)
                    .withEndDate(updatedDate)
                    .withBillingStartDate(updatedDate)
                    .withBillingEndDate(updatedDate)
                    .withNextBillingDate(updatedDate)
                    .withBillingCycle(0)
                    .withBillingCycleType(BillingCycleType.NoRenewal)
                    .withBillingContractTerm(0);

        subscriptionDao.update(subscription);

        final DBObject subscriptionDocument = collection.findOne(new BasicDBObject("id", subscription.getId()));
        assertEquals(toSubscription(subscriptionDocument), subscription);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfSubscriptionDoesNotExistOnUpdateSubscription() throws Exception {
        subscriptionDao.update(createSubscription());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnUpdateSubscriptionsIfMongoExceptionOccurs()
            throws ServerException, NotFoundException, ConflictException {
        final Subscription subscription = createSubscription();
        doThrow(new MongoException("")).when(collection).findOne(new BasicDBObject("id", subscription.getId()));
        subscriptionDao.update(subscription);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnUpdateSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        doThrow(new MongoException("")).when(collection).update(any(DBObject.class), any(DBObject.class));

        subscriptionDao.update(subscription);
    }

    @Test
    public void shouldBeAbleToAddSubscription() throws Exception {
        final Account account = createAccount();
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        when(accountDao.getById(account.getId())).thenReturn(account);

        subscriptionDao.create(subscription);

        final DBObject subscriptionDocument = collection.findOne(new BasicDBObject("accountId", account.getId()));
        assertNotNull(subscriptionDocument);
        assertEquals(subscriptionDao.toSubscription(subscriptionDocument), subscription);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowExceptionOnAddSubscriptionToNotExistedAccount() throws Exception {
        when(accountDao.getById("test_account_id")).thenThrow(new NotFoundException("account not found"));
        subscriptionDao.create(createSubscription());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnAddSubscriptionsIfMongoExceptionOccurs() throws Exception {
        doThrow(new MongoException("")).when(collection).save(any(DBObject.class));

        subscriptionDao.create(createSubscription());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnAddSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final Account account = createAccount();
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        when(accountDao.getById(account.getId())).thenReturn(account);

        doThrow(new MongoException("")).when(collection).save(subscriptionDao.toDBObject(subscription));

        subscriptionDao.create(subscription);
    }

    @Test(dataProvider = "nullFieldProvider")
    public void shouldThrowConflictExceptionOnAddSubscriptionIfMandatoryFiledIsNull(Subscription subscription, String message)
            throws Exception {
        final Account account = createAccount();
        when(accountDao.getById(account.getId())).thenReturn(account);

        try {
            subscriptionDao.create(subscription);
        } catch (ConflictException e) {
            assertEquals(e.getLocalizedMessage(), message);
            return;
        }
        fail();
    }

    @DataProvider(name = "nullFieldProvider")
    public Object[][] nullFieldProvider() {
        Subscription spySubscription = spy(createSubscription().withAccountId("account ID"));
        doReturn(null).when(spySubscription).getProperties();
        return new Object[][]{
                {null, "Subscription information is missing"},
                {createSubscription().withAccountId("account ID").withPlanId(null), "Plan id is missing"},
                {createSubscription().withAccountId("account ID").withServiceId(null), "Subscription service id is missing"},
                {createSubscription().withAccountId("account ID").withAccountId(null), "Subscription account id is missing"},
                {createSubscription().withAccountId("account ID").withId(null), "Subscription id is missing"},
                {spySubscription, "Subscription properties are missing"},
                {createSubscription().withAccountId("account ID").withUsePaymentSystem(null),
                 "Subscription parameter usePaymentSystem is missing"},
                {createSubscription().withAccountId("account ID").withBillingContractTerm(null),
                 "Subscription parameter billingContractTerm is missing"},
                {createSubscription().withAccountId("account ID").withDescription(null), "Subscription description is missing"},
                {createSubscription().withAccountId("account ID").withState(null), "Subscription state is missing"},
                {createSubscription().withAccountId("account ID").withBillingCycle(null), "Subscription parameter billingCycle is missing"},
                {createSubscription().withAccountId("account ID").withBillingCycleType(null),
                 "Subscription parameter billingCycleType is missing"},
        };
    }

    @Test
    public void shouldBeAbleToGetAllSubscriptionsByAccountId() throws Exception {
        final Subscription subscription1 = createSubscription().withAccountId("account_id")
                                                               .withState(SubscriptionState.INACTIVE);
        final Subscription subscription2 = createSubscription().withAccountId("account_id")
                                                               .withId(subscription1.getId() + "other");
        insertSubscriptions(subscription1, subscription2);

        final List<Subscription> found = subscriptionDao.getByAccountId("account_id");

        assertEquals(new HashSet<>(found), new HashSet<>(asList(subscription1, subscription2)));
    }


    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsIfMongoExceptionOccurs() throws Exception {
        doThrow(new MongoException("")).when(collection).find(any(DBObject.class));

        subscriptionDao.getByAccountId("account");
    }

    @Test
    public void shouldBeAbleToGetActiveSubscriptionsByAccount() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId()).withServiceId("SomeService");
        final Subscription subscription2 = createSubscription().withAccountId(account.getId()).withId(subscription1.getId() + "other");
        insertSubscriptions(subscription1, subscription2);
        when(accountDao.getById(account.getId())).thenReturn(account);

        final List<Subscription> found = subscriptionDao.getActive(account.getId());

        assertEquals(new HashSet<>(found), new HashSet<>(asList(subscription1, subscription2)));
    }

    @Test
    public void shouldNotReturnSubscriptionIfServiceDoesNotMatch() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId());
        final Subscription subscription2 = createSubscription().withAccountId(account.getId())
                                                               .withId(subscription1.getId() + "other")
                                                               .withServiceId(subscription1.getServiceId() + "other");
        when(accountDao.getById(account.getId())).thenReturn(account);
        insertSubscriptions(subscription1, subscription2);

        final Subscription found = subscriptionDao.getActiveByServiceId(account.getId(), subscription2.getServiceId());

        assertEquals(found, subscription2);
    }

    @Test
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetActiveSubscriptionsAndServiceIsFactory() throws Exception {
        final Account account = createAccount();
        when(accountDao.getById(account.getId())).thenReturn(account);

        final Subscription found = subscriptionDao.getActiveByServiceId(account.getId(), "Factory");

        assertNull(found);
    }

    @Test
    public void shouldBeAbleToGetActiveSubscriptionsByAccountAndService() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId());
        final Subscription subscription2 = createSubscription().withAccountId(account.getId())
                                                               .withId(subscription1.getId() + "other2")
                                                               .withServiceId(subscription1.getServiceId() + "other");
        when(accountDao.getById(account.getId())).thenReturn(account);
        insertSubscriptions(subscription1, subscription2);

        final Subscription found = subscriptionDao.getActiveByServiceId(account.getId(), subscription1.getServiceId());

        assertEquals(found, subscription1);
    }

    @Test(dataProvider = "notSaasServiceIdProvider")
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetActiveSubscriptionsWithNotSaasService(String serviceId) throws Exception {
        final Account account = createAccount();
        when(accountDao.getById(account.getId())).thenReturn(account);

        final Subscription found = subscriptionDao.getActiveByServiceId(account.getId(), serviceId);

        assertNull(found);
    }

    @DataProvider(name = "notSaasServiceIdProvider")
    public String[][] notSaasServiceIdProvider() {
        return new String[][]{
                {"Factory"},
                {"OnPremises"},
                {"NotSaas"}
        };
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetActiveSubscriptionsIfMongoExceptionOccurs() throws Exception {
        final Account account = createAccount();
        when(accountDao.getById(account.getId())).thenReturn(account);
        doThrow(new MongoException("")).when(collection).find(any(DBObject.class));

        subscriptionDao.getActive(account.getId());
    }

    @Test
    public void shouldBeAbleToRemoveSubscription() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);

        subscriptionDao.remove(subscription.getId());

        assertNull(collection.findOne(new BasicDBObject("id", subscription.getId())));
    }

    @Test
    public void shouldBeAbleToDeactivateSubscription() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);

        subscriptionDao.deactivate(subscription.getId());

        Subscription deactivatedSubscription = toSubscription(collection.findOne(new BasicDBObject("id", subscription.getId())));
        assertEquals(SubscriptionState.INACTIVE, deactivatedSubscription.getState());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnDeactivateSubscriptionIfMongoExceptionOccurs() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        doThrow(new MongoException("")).when(collection).findOne(any(DBObject.class));

        subscriptionDao.deactivate(subscription.getId());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfSubscriptionDoesNotExistOnDeactivateSubscription() throws Exception {
        subscriptionDao.deactivate("subscriptionId");
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Subscription not found .*")
    public void shouldThrowNotFoundExceptionOnRemoveSubscriptionsWithInvalidSubscriptionId() throws ServerException, NotFoundException {
        subscriptionDao.remove("test-id");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnRemoveSubscriptionsIfMongoExceptionOccurs() throws Exception {
        final String subscriptionId = "test_subscription_id";
        doThrow(new MongoException("")).when(collection).findOne(new BasicDBObject("id", subscriptionId));

        subscriptionDao.remove(subscriptionId);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnRemoveSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        doThrow(new MongoException("")).when(collection).remove(new BasicDBObject("id", subscription.getId()));

        subscriptionDao.remove(subscription.getId());
    }

    @Test
    public void shouldBeAbleToGetSubscriptionById() throws ServerException, NotFoundException, ConflictException {
        final Account account = createAccount();
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        when(accountDao.getById(account.getId())).thenReturn(account);
        insertSubscriptions(subscription);

        final Subscription actual = subscriptionDao.getById(subscription.getId());

        assertNotNull(actual);
        assertEquals(actual, subscription);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Subscription not found .*")
    public void shouldThrowNotFoundExceptionOnGetSubscriptionsByIdWithInvalidSubscriptionId() throws ServerException, NotFoundException {
        subscriptionDao.getById("test-id");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsByIdIfMongoExceptionOccurs() throws Exception {
        final String subscriptionId = "subscription-id";
        doThrow(new MongoException("")).when(collection).findOne(new BasicDBObject("id", subscriptionId));
        subscriptionDao.getById(subscriptionId);
    }

    private void insertSubscriptions(Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            collection.insert(subscriptionDao.toDBObject(subscription));
        }
    }

    private Subscription createSubscription() {
        final HashMap<String, String> properties = new HashMap<>(4);
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        return new Subscription().withId("test_subscription_id")
                                 .withAccountId("test_account_id")
                                 .withPlanId("test_plan_id")
                                 .withServiceId("test_service_id")
                                 .withProperties(properties)
                                 .withBillingCycleType(BillingCycleType.AutoRenew)
                                 .withBillingCycle(1)
                                 .withDescription("description")
                                 .withBillingContractTerm(1)
                                 .withStartDate(new Date())
                                 .withEndDate(new Date())
                                 .withBillingStartDate(new Date())
                                 .withBillingEndDate(new Date())
                                 .withNextBillingDate(new Date())
                                 .withState(SubscriptionState.ACTIVE)
                                 .withUsePaymentSystem(true);
    }

    private Account createAccount() {
        final Map<String, String> attributes = new HashMap<>(8);
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        return new Account().withId("test_account_id")
                            .withName("test_account_name")
                            .withAttributes(attributes);
    }
}