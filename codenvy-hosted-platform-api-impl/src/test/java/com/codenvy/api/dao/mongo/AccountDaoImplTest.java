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

import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.Member;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionQueryBuilder;
import com.codenvy.api.account.shared.dto.BillingCycleType;
import com.codenvy.api.account.shared.dto.SubscriptionState;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

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

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for {@link AccountDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class AccountDaoImplTest extends BaseDaoTest {

    private static final String ACC_COLL_NAME                     = "accounts";
    private static final String SUBSCRIPTION_COLL_NAME            = "subscriptions";
    private static final String MEMBER_COLL_NAME                  = "members";

    @Mock
    private SubscriptionQueryBuilder subscriptionQueryBuilder;
    @Mock
    private WorkspaceDao   workspaceDao;

    private AccountDaoImpl accountDao;
    private DBCollection   subscriptionCollection;
    private DBCollection   membersCollection;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(ACC_COLL_NAME);
        db = spy(db);
        collection = spy(db.getCollection(ACC_COLL_NAME));
        subscriptionCollection = spy(db.getCollection(SUBSCRIPTION_COLL_NAME));
        membersCollection = spy(db.getCollection(MEMBER_COLL_NAME));
        when(db.getCollection(ACC_COLL_NAME)).thenReturn(collection);
        when(db.getCollection(SUBSCRIPTION_COLL_NAME)).thenReturn(subscriptionCollection);
        when(db.getCollection(MEMBER_COLL_NAME)).thenReturn(membersCollection);
        accountDao = new AccountDaoImpl(db,
                                        workspaceDao,
                                        ACC_COLL_NAME,
                                        SUBSCRIPTION_COLL_NAME,
                                        MEMBER_COLL_NAME,
                                        subscriptionQueryBuilder);
    }

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void shouldBeAbleToCreateAccount() throws Exception {
        final Account account = createAccount();

        accountDao.create(account);

        final DBObject accountDocument = collection.findOne(new BasicDBObject("id", account.getId()));
        assertNotNull(accountDocument);
        assertEquals(accountDao.toAccount(accountDocument), account);
    }

    @Test
    public void shouldBeAbleToGetAccountById() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);

        final Account actual = accountDao.getById(account.getId());

        assertEquals(actual, account);
    }

    @Test
    public void shouldBeAbleToGetAccountByName() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);

        final Account actual = accountDao.getByName(account.getName());

        assertEquals(actual, account);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfAccountWithGivenNameDoesNotExist() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);

        assertNull(accountDao.getByName(account.getName() + "suffix"));
    }

    @Test
    public void shouldBeAbleToGetAccountByOwner() throws Exception {
        final Account account = createAccount();
        final Member member = new Member().withAccountId(account.getId())
                                          .withRoles(asList("account/owner"))
                                          .withUserId("test_user_id");
        insertAccounts(account);
        insertMembers(member);

        final List<Account> result = accountDao.getByOwner(member.getUserId());

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), account);
    }

    @Test
    public void shouldBeAbleToUpdateAccount() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        //prepare update
        account.setName(account.getName() + "new_name_suffix");

        accountDao.update(account);

        final DBObject accountDocument = collection.findOne(new BasicDBObject("id", account.getId()));
        assertNotNull(accountDocument);
        assertEquals(accountDao.toAccount(accountDocument), account);
    }

    @Test
    public void shouldBeAbleToRemoveAccount() throws Exception {
        final Account account = createAccount();
        when(workspaceDao.getByAccount(account.getId())).thenReturn(Collections.<Workspace>emptyList());
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        final Member member1 = new Member().withUserId("test_user_1")
                                           .withAccountId(account.getId())
                                           .withRoles(asList("account/owner"));
        final Member member2 = new Member().withUserId("test_user_2")
                                           .withAccountId(account.getId())
                                           .withRoles(asList("account/member"));
        insertMembers(member1, member2);
        insertAccounts(account);
        insertSubscriptions(subscription);

        accountDao.remove(account.getId());

        assertNull(collection.findOne(new BasicDBObject("id", account.getId())));
        assertNull(subscriptionCollection.findOne(new BasicDBObject("accountId", account.getId())));
        assertFalse(membersCollection.find(new BasicDBObject("members.accountId", account.getId())).hasNext());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "It is not possible to remove account having associated workspaces")
    public void shouldNotBeAbleToRemoveAccountWithAssociatedWorkspace() throws Exception {
        final Account account = createAccount();
        when(workspaceDao.getByAccount(account.getId())).thenReturn(asList(mock(Workspace.class)));
        insertAccounts(account);

        accountDao.remove(account.getId());
    }

    @Test
    public void shouldBeAbleToAddMember() throws Exception {
        final Account account = createAccount();
        final Member member = new Member().withUserId("test_user_id")
                                          .withAccountId(account.getId())
                                          .withRoles(asList("account/admin", "account/manager"));
        insertAccounts(account);

        accountDao.addMember(member);

        final DBObject membersDocument = membersCollection.findOne(new BasicDBObject("_id", member.getUserId()));
        assertNotNull(membersDocument);
        final BasicDBList actualMembers = (BasicDBList)membersDocument.get("members");
        assertEquals(actualMembers.size(), 1);
        assertEquals(accountDao.toMember(actualMembers.get(0)), member);
    }

    @Test
    public void shouldBeAbleToGetMembers() throws Exception {
        final Account account = createAccount();
        final Member member1 = new Member().withUserId("test_user_id1")
                                           .withAccountId(account.getId())
                                           .withRoles(asList("account/owner"));
        final Member member2 = new Member().withUserId("test_user_id2")
                                           .withAccountId(account.getId())
                                           .withRoles(asList("account/member"));
        insertMembers(member1, member2);

        final List<Member> actualMembers = accountDao.getMembers(account.getId());

        assertEquals(new HashSet<>(actualMembers), new HashSet<>(asList(member1, member2)));
    }

    @Test
    public void shouldBeAbleToRemoveMember() throws Exception {
        final Account account = createAccount();
        final Member member1 = new Member().withUserId("test_user_1")
                                           .withAccountId(account.getId())
                                           .withRoles(asList("account/owner"));
        final Member member2 = new Member().withUserId("test_user_2")
                                           .withAccountId(account.getId())
                                           .withRoles(asList("account/member"));
        insertMembers(member1, member2);

        accountDao.removeMember(member2);

        assertNull(membersCollection.findOne(new BasicDBObject("_id", member2.getUserId())));
        assertNotNull(membersCollection.findOne(new BasicDBObject("_id", member1.getUserId())));
    }

    @Test
    public void shouldBeAbleToGetAccountMembershipsByMember() throws NotFoundException, ServerException {
        final Account account = createAccount();
        final Member member = new Member().withAccountId(account.getId())
                                          .withUserId("test_user_id")
                                          .withRoles(asList("account/owner"));
        insertMembers(member);

        final List<Member> actualMembers = accountDao.getByMember(member.getUserId());

        assertEquals(actualMembers.size(), 1);
        assertEquals(actualMembers.get(0), member);
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
                    .withPaymentToken("updatedPaymentToken")
                    .withStartDate(updatedDate)
                    .withEndDate(updatedDate)
                    .withTrialStartDate(updatedDate)
                    .withTrialEndDate(updatedDate)
                    .withBillingStartDate(updatedDate)
                    .withBillingEndDate(updatedDate)
                    .withNextBillingDate(updatedDate)
                    .withBillingCycle(0)
                    .withBillingCycleType(BillingCycleType.NoRenewal)
                    .withBillingContractTerm(0);

        accountDao.updateSubscription(subscription);

        final DBObject subscriptionDocument = subscriptionCollection.findOne(new BasicDBObject("id", subscription.getId()));
        assertEquals(accountDao.toSubscription(subscriptionDocument), subscription);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfSubscriptionDoesNotExistOnUpdateSubscription() throws Exception {
        accountDao.updateSubscription(createSubscription());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnUpdateSubscriptionsIfMongoExceptionOccurs()
            throws ServerException, NotFoundException, ConflictException {
        final Subscription subscription = createSubscription();
        doThrow(new MongoException("")).when(subscriptionCollection).findOne(new BasicDBObject("id", subscription.getId()));
        accountDao.updateSubscription(subscription);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnUpdateSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        doThrow(new MongoException("")).when(subscriptionCollection).update(any(DBObject.class), any(DBObject.class));

        accountDao.updateSubscription(subscription);
    }

    @Test
    public void shouldBeAbleToAddSubscription() throws Exception {
        final Account account = createAccount();
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        insertAccounts(account);

        accountDao.addSubscription(subscription);

        final DBObject subscriptionDocument = subscriptionCollection.findOne(new BasicDBObject("accountId", account.getId()));
        assertNotNull(subscriptionDocument);
        assertEquals(accountDao.toSubscription(subscriptionDocument), subscription);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowExceptionOnAddSubscriptionToNotExistedAccount() throws Exception {
        accountDao.addSubscription(createSubscription());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnAddSubscriptionsIfMongoExceptionOccurs() throws Exception {
        doThrow(new MongoException("")).when(collection).findOne(any(DBObject.class));

        accountDao.addSubscription(createSubscription());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnAddSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final Account account = createAccount();
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        insertAccounts(account);
        doThrow(new MongoException("")).when(subscriptionCollection).save(accountDao.toDBObject(subscription));

        accountDao.addSubscription(subscription);
    }

    @Test(dataProvider = "nullFieldProvider")
    public void shouldThrowConflictExceptionOnAddSubscriptionIfMandatoryFiledIsNull(Subscription subscription, String message) throws Exception {
        final Account account = createAccount();
        insertAccounts(account.withId("account ID"));

        try {
            accountDao.addSubscription(subscription);
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
    public void shouldBeAbleToGetActiveSubscriptionsByAccount() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId()).withServiceId("Saas");
        final Subscription subscription2 = createSubscription().withAccountId(account.getId()).withId(subscription1.getId() + "other");
        insertSubscriptions(subscription1, subscription2);
        insertAccounts(account);

        final List<Subscription> found = accountDao.getActiveSubscriptions(account.getId(), null);

        assertEquals(new HashSet<>(found), new HashSet<>(asList(subscription1, subscription2)));
    }

    @Test
    public void shouldNotReturnSubscriptionIfServiceDoesNotMatch() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId());
        final Subscription subscription2 = createSubscription().withAccountId(account.getId())
                                                               .withId(subscription1.getId() + "other")
                                                               .withServiceId(subscription1.getServiceId() + "other");
        insertAccounts(account);
        insertSubscriptions(subscription1, subscription2);

        final List<Subscription> found = accountDao.getActiveSubscriptions(account.getId(), subscription2.getServiceId());

        assertEquals(found, asList(subscription2));
    }

    @Test
    public void shouldNotReturnInactiveSubscriptionOnGetActive() throws Exception {
        final Account account = createAccount();
        final Subscription subscription =
                createSubscription().withAccountId(account.getId()).withServiceId("Saas").withState(SubscriptionState.INACTIVE);
        insertAccounts(account);
        insertSubscriptions(subscription);

        final List<Subscription> found = accountDao.getActiveSubscriptions(account.getId(), null);

        assertTrue(found.isEmpty());
    }

    @Test
    public void shouldBeAbleToGetActiveSubscriptionsByAccountAndService() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId());
        final Subscription subscription2 = createSubscription().withAccountId(account.getId())
                                                               .withId(subscription1.getId() + "other");
        final Subscription subscription3 = createSubscription().withAccountId(account.getId())
                                                               .withId(subscription1.getId() + "other2")
                                                               .withServiceId(subscription1.getServiceId() + "other");
        insertAccounts(account);
        insertSubscriptions(subscription1, subscription2, subscription3);

        final List<Subscription> found = accountDao.getActiveSubscriptions(account.getId(), subscription1.getServiceId());

        assertEquals(new HashSet<>(found), new HashSet<>(asList(subscription1, subscription2)));
    }

    @Test(dataProvider = "notSaasServiceIdProvider")
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetActiveSubscriptionsWithNotSaasService(String serviceId) throws Exception {
        final Account account = createAccount();
        insertAccounts(account);

        final List<Subscription> found = accountDao.getActiveSubscriptions(account.getId(), serviceId);

        assertTrue(found.isEmpty());
    }

    @DataProvider(name = "notSaasServiceIdProvider")
    public String [][] notSaasServiceIdProvider() {
        return new String[][] {
                {"Factory"},
                {"OnPremises"},
                {"NotSaas"}
        };
    }

    @Test
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetActiveSubscriptionsAndServiceIsSaasAndAccountDoesNotContainWs()
            throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        when(workspaceDao.getByAccount(account.getId())).thenReturn(Collections.<Workspace>emptyList());

        final List<Subscription> found = accountDao.getActiveSubscriptions(account.getId(), "Saas");

        assertTrue(found.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetActiveSubscriptionsAndServiceIsNotProvidedAndAccountDoesNotContainWs()
            throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        when(workspaceDao.getByAccount(account.getId())).thenReturn(Collections.<Workspace>emptyList());

        final List<Subscription> found = accountDao.getActiveSubscriptions(account.getId(), null);

        assertTrue(found.isEmpty());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionOnGetActiveSubscriptionsWithInvalidAccountId() throws Exception {
        accountDao.getActiveSubscriptions("invalid_account_id", null);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetActiveSubscriptionsIfMongoExceptionOccurs() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        doThrow(new MongoException("")).when(subscriptionCollection).find(any(DBObject.class));

        accountDao.getActiveSubscriptions(account.getId(), null);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetActiveSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        doThrow(new MongoException("")).when(collection).findOne(any(DBObject.class));

        accountDao.getActiveSubscriptions(account.getId(), null);
    }

    @Test
    public void shouldBeAbleToRemoveSubscription() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);

        accountDao.removeSubscription(subscription.getId());

        assertNull(subscriptionCollection.findOne(new BasicDBObject("id", subscription.getId())));
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Subscription not found .*")
    public void shouldThrowNotFoundExceptionOnRemoveSubscriptionsWithInvalidSubscriptionId() throws ServerException, NotFoundException {
        accountDao.removeSubscription("test-id");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnRemoveSubscriptionsIfMongoExceptionOccurs() throws Exception {
        final String subscriptionId = "test_subscription_id";
        doThrow(new MongoException("")).when(subscriptionCollection).findOne(new BasicDBObject("id", subscriptionId));

        accountDao.removeSubscription(subscriptionId);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnRemoveSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        doThrow(new MongoException("")).when(subscriptionCollection).remove(new BasicDBObject("id", subscription.getId()));

        accountDao.removeSubscription(subscription.getId());
    }

    @Test
    public void shouldBeAbleToGetSubscriptionById() throws ServerException, NotFoundException, ConflictException {
        final Account account = createAccount();
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        insertAccounts(account);
        insertSubscriptions(subscription);

        final Subscription actual = accountDao.getSubscriptionById(subscription.getId());

        assertNotNull(actual);
        assertEquals(actual, subscription);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Subscription not found .*")
    public void shouldThrowNotFoundExceptionOnGetSubscriptionsByIdWithInvalidSubscriptionId() throws ServerException, NotFoundException {
        accountDao.getSubscriptionById("test-id");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsByIdIfMongoExceptionOccurs() throws Exception {
        final String subscriptionId = "subscription-id";
        doThrow(new MongoException("")).when(subscriptionCollection).findOne(new BasicDBObject("id", subscriptionId));
        accountDao.getSubscriptionById(subscriptionId);
    }

    private void insertAccounts(Account... accounts) {
        for (Account account : accounts) {
            collection.insert(accountDao.toDBObject(account));
        }
    }

    private void insertSubscriptions(Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            subscriptionCollection.insert(accountDao.toDBObject(subscription));
        }
    }

    private void insertMembers(Member... members) {
        final Map<String, BasicDBList> membersMap = new HashMap<>();
        for (Member member : members) {
            if (!membersMap.containsKey(member.getUserId())) {
                membersMap.put(member.getUserId(), new BasicDBList());
            }
            membersMap.get(member.getUserId()).add(accountDao.toDBObject(member));
        }
        for (Map.Entry<String, BasicDBList> entry : membersMap.entrySet()) {
            membersCollection.insert(new BasicDBObject("_id", entry.getKey()).append("members", entry.getValue()));
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
                                 .withTrialStartDate(new Date())
                                 .withTrialEndDate(new Date())
                                 .withPaymentToken("token")
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
