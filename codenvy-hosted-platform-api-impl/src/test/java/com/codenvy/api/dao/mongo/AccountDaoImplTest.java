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
import com.codenvy.api.account.server.dao.Billing;
import com.codenvy.api.account.server.dao.SubscriptionAttributes;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
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
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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
    private static final String SUBSCRIPTION_ATTRIBUTES_COLL_NAME = "subscriptionAttributes";

    @Mock
    private WorkspaceDao   workspaceDao;
    private AccountDaoImpl accountDao;
    private DBCollection   subscriptionCollection;
    private DBCollection   membersCollection;
    private DBCollection   subscriptionAttributesCollection;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(ACC_COLL_NAME);
        db = spy(db);
        collection = spy(db.getCollection(ACC_COLL_NAME));
        subscriptionCollection = spy(db.getCollection(SUBSCRIPTION_COLL_NAME));
        subscriptionAttributesCollection = spy(db.getCollection(SUBSCRIPTION_ATTRIBUTES_COLL_NAME));
        membersCollection = spy(db.getCollection(MEMBER_COLL_NAME));
        when(db.getCollection(ACC_COLL_NAME)).thenReturn(collection);
        when(db.getCollection(SUBSCRIPTION_COLL_NAME)).thenReturn(subscriptionCollection);
        when(db.getCollection(SUBSCRIPTION_ATTRIBUTES_COLL_NAME)).thenReturn(subscriptionAttributesCollection);
        when(db.getCollection(MEMBER_COLL_NAME)).thenReturn(membersCollection);
        accountDao = new AccountDaoImpl(db,
                                        workspaceDao,
                                        ACC_COLL_NAME,
                                        SUBSCRIPTION_COLL_NAME,
                                        MEMBER_COLL_NAME,
                                        SUBSCRIPTION_ATTRIBUTES_COLL_NAME);
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
        subscriptionAttributesCollection.insert(new BasicDBObject("_id", subscription.getId()));

        accountDao.remove(account.getId());

        assertNull(collection.findOne(new BasicDBObject("id", account.getId())));
        assertNull(subscriptionCollection.findOne(new BasicDBObject("accountId", account.getId())));
        assertNull(subscriptionAttributesCollection.findOne(new BasicDBObject("_id", subscription.getId())));
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
    public void shouldBeAbleToAddSubscription() throws Exception {
        final Account account = createAccount();
        final Subscription subscription = createSubscription().withAccountId(account.getId());
        insertAccounts(account);

        accountDao.addSubscription(subscription);

        final DBObject subscriptionDocument = subscriptionCollection.findOne(new BasicDBObject("accountId", account.getId()));
        assertNotNull(subscriptionDocument);
        assertEquals(accountDao.toSubscription(subscriptionDocument), subscription);
    }

    @Test
    public void shouldBeAbleToUpdateSubscription() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        //prepare update
        subscription.setPlanId(subscription.getPlanId() + "suffix");
        subscription.setServiceId(subscription.getServiceId() + "suffix");

        accountDao.updateSubscription(subscription);

        final DBObject subscriptionDocument = subscriptionCollection.findOne(new BasicDBObject("id", subscription.getId()));
        assertEquals(accountDao.toSubscription(subscriptionDocument), subscription);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfSubscriptionDoesNotExist() throws Exception {
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

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowAnExceptionWhileAddingSubscriptionToNotExistedAccount() throws Exception {
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

    @Test
    public void shouldBeAbleToGetSubscriptionsByAccount() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId()).withServiceId("Saas");
        final Subscription subscription2 = createSubscription().withAccountId(account.getId()).withId(subscription1.getId() + "other");
        insertSubscriptions(subscription1, subscription2);
        insertAccounts(account);

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), null);

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

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), subscription2.getServiceId());

        assertEquals(found, asList(subscription2));
    }

    @Test
    public void shouldBeAbleToGetSubscriptionsByAccountAndService() throws Exception {
        final Account account = createAccount();
        final Subscription subscription1 = createSubscription().withAccountId(account.getId());
        final Subscription subscription2 = createSubscription().withAccountId(account.getId())
                                                               .withId(subscription1.getId() + "other");
        final Subscription subscription3 = createSubscription().withAccountId(account.getId())
                                                               .withId(subscription1.getId() + "other2")
                                                               .withServiceId(subscription1.getServiceId() + "other");
        insertAccounts(account);
        insertSubscriptions(subscription1, subscription2, subscription3);

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), subscription1.getServiceId());

        assertEquals(new HashSet<>(found), new HashSet<>(asList(subscription1, subscription2)));
    }

    @Test
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetSubscriptionsWithNotSaasService() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), "Factory");

        assertTrue(found.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetSubscriptionsAndServiceIsSaasAndAccountDoesNotContainWs()
            throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        when(workspaceDao.getByAccount(account.getId())).thenReturn(Collections.<Workspace>emptyList());

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), "Saas");

        assertTrue(found.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetSubscriptionsAndServiceIsNotProvidedAndAccountDoesNotContainWs()
            throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        when(workspaceDao.getByAccount(account.getId())).thenReturn(Collections.<Workspace>emptyList());

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), null);

        assertTrue(found.isEmpty());
    }

    @Test
    public void shouldReturnDefaultSubscriptionIfThereIsNoSubscriptionsOnGetSubscriptionsAndServiceIsSaas() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        when(workspaceDao.getByAccount(account.getId())).thenReturn(singletonList(new Workspace()));

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), "Saas");

        assertEquals(found, singletonList(new Subscription().withId("community" + account.getId())
                                                            .withPlanId("sas-community")
                                                            .withAccountId(account.getId())
                                                            .withServiceId("Saas")
                                                            .withProperties(singletonMap("Package", "Community"))));
    }

    @Test
    public void shouldReturnDefaultSubscriptionIfThereIsNoSubscriptionsOnGetSubscriptionsWithoutService() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        when(workspaceDao.getByAccount(account.getId())).thenReturn(singletonList(new Workspace()));

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), null);

        assertEquals(found, singletonList(new Subscription()
                                                  .withId("community" + account.getId())
                                                  .withPlanId("sas-community")
                                                  .withAccountId(account.getId())
                                                  .withServiceId("Saas")
                                                  .withProperties(singletonMap("Package", "Community"))));
    }

    @Test
    public void shouldReturnDefaultSubscriptionIfThereIsNonSaasSubscriptionsOnGetSubscriptionsWithoutService() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        final Subscription subscription = createSubscription().withAccountId(account.getId()).withServiceId("NonSaas");
        insertSubscriptions(subscription);
        Subscription defaultSaasSubscription = new Subscription()
                .withId("community" + account.getId())
                .withPlanId("sas-community")
                .withAccountId(account.getId())
                .withServiceId("Saas")
                .withProperties(singletonMap("Package", "Community"));
        when(workspaceDao.getByAccount(account.getId())).thenReturn(singletonList(new Workspace()));

        final List<Subscription> found = accountDao.getSubscriptions(account.getId(), null);

        assertEquals(new HashSet<>(found), new HashSet<>(asList(subscription, defaultSaasSubscription)));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionOnGetSubscriptionsWithInvalidAccountId() throws Exception {
        accountDao.getSubscriptions("invalid_account_id", null);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsIfMongoExceptionOccurs() throws Exception {
        final Account account = createAccount();
        insertAccounts(account);
        doThrow(new MongoException("")).when(subscriptionCollection).find(new BasicDBObject("accountId", account.getId()));

        accountDao.getSubscriptions(account.getId(), null);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsIfMongoExceptionOccurs2() throws Exception {
        final String accountId = "test_account_id";
        doThrow(new MongoException("")).when(collection).findOne(new BasicDBObject("id", accountId));

        accountDao.getSubscriptions(accountId, null);
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

    @Test
    public void shouldBeAbleToGetAllSubscriptions() throws ServerException {
        final Subscription subscription1 = createSubscription();
        final Subscription subscription2 = createSubscription().withId(subscription1.getId() + "suffix");
        insertSubscriptions(subscription1, subscription2);

        final List<Subscription> actual = accountDao.getSubscriptions();

        assertEquals(new HashSet<>(actual), new HashSet<>(asList(subscription1, subscription2)));
    }

    @Test
    public void shouldReturnEmptyCollectionIfThereIsNoSubscriptionsOnGetAllSubscriptions() throws ServerException {
        assertTrue(accountDao.getSubscriptions().isEmpty());
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "It is not possible to retrieve subscriptions")
    public void shouldThrowServerExceptionIfMongoExceptionIsThrownOnGetAllSubscriptions() throws ServerException {
        when(subscriptionCollection.find()).thenThrow(new MongoException("mongo exception"));

        accountDao.getSubscriptions();
    }

    @Test(expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "Subscription attributes required")
    public void shouldThrowForbiddenExceptionIfSubscriptionAttributesIsNullOnSaveSubscriptionAttributes() throws Exception {
        accountDao.saveSubscriptionAttributes("test-id", null);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Subscription not found .*")
    public void shouldThrowNotFoundExceptionIfSubscriptionIsMissingOnSaveSubscriptionAttributes() throws Exception {
        accountDao.saveSubscriptionAttributes("subscription-id", new SubscriptionAttributes());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "It is not possible to persist subscription attributes")
    public void shouldThrowServerExceptionIfMongoExceptionOccursOnGetSubscriptionInSaveSubscriptionAttributes() throws Exception {
        final String subscriptionId = "subscription-id";
        when(subscriptionCollection.findOne(eq(new BasicDBObject("id", subscriptionId))))
                .thenThrow(new MongoException("Mongo exception message"));

        accountDao.saveSubscriptionAttributes(subscriptionId, new SubscriptionAttributes());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "It is not possible to persist subscription attributes")
    public void shouldThrowServerExceptionIfMongoExceptionOccursOnSaveSubscriptionAttributes() throws Exception {
        final Subscription subscription = createSubscription();
        insertSubscriptions(subscription);
        doThrow(new MongoException("Mongo exception message")).when(subscriptionAttributesCollection).save(any(DBObject.class));

        accountDao.saveSubscriptionAttributes(subscription.getId(), createSubscriptionAttributes());
    }

    @Test
    public void shouldBeAbleToSaveSubscriptionAttributes() throws Exception {
        final Subscription subscription = createSubscription();
        final SubscriptionAttributes attributes = createSubscriptionAttributes();
        insertSubscriptions(subscription);

        accountDao.saveSubscriptionAttributes(subscription.getId(), createSubscriptionAttributes());

        final DBObject attributesDocument = subscriptionAttributesCollection.findOne(new BasicDBObject("_id", subscription.getId()));
        assertNotNull(attributesDocument);
        assertEquals(accountDao.toSubscriptionAttributes(attributesDocument), attributes);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Attributes of subscription .* not found")
    public void shouldThrowNotFoundExceptionIfSubscriptionAttributesAreMissingOnGetSubscriptionAttributes() throws Exception {
        accountDao.getSubscriptionAttributes("subscription-id");
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "It is not possible to retrieve subscription attributes")
    public void shouldThrowServerExceptionIfMongoExceptionOccursOnGetSubscriptionAttributes() throws ServerException, NotFoundException {
        final String subscriptionId = "subscription-id";
        when(subscriptionAttributesCollection.findOne(eq(new BasicDBObject("_id", subscriptionId))))
                .thenThrow(new MongoException("Mongo exception message"));

        accountDao.getSubscriptionAttributes(subscriptionId);
    }

    @Test
    public void shouldBeAbleToGetSubscriptionAttributes() throws ServerException, NotFoundException {
        final String subscriptionId = "subscription-id";
        final SubscriptionAttributes attributes = createSubscriptionAttributes();
        subscriptionAttributesCollection.save(accountDao.toDBObject(subscriptionId, attributes));

        final SubscriptionAttributes actual = accountDao.getSubscriptionAttributes(subscriptionId);

        assertEquals(actual, attributes);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Attributes of subscription .* not found")
    public void shouldThrowNotFoundExceptionIfSubscriptionAttributesDoNotExist() throws ServerException, NotFoundException {
        accountDao.removeSubscriptionAttributes("subscription-id");
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "It is not possible to remove subscription attributes")
    public void shouldThrowServerExceptionIfMongoExceptionOccursOnRetrievingSubscriptionAttributesInRemoveSubscriptionAttributes()
            throws Exception {
        when(subscriptionAttributesCollection.findOne(any(DBObject.class))).thenThrow(new MongoException("Mongo exception message"));

        accountDao.removeSubscriptionAttributes("subscription-id");
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "It is not possible to remove subscription attributes")
    public void shouldThrowServerExceptionIfMongoExceptionOccursOnRemoveSubscriptionAttributes() throws ServerException, NotFoundException {
        final String subscriptionId = "subscription-id";
        final SubscriptionAttributes attributes = createSubscriptionAttributes();
        subscriptionAttributesCollection.save(accountDao.toDBObject(subscriptionId, attributes));

        doThrow(new MongoException("Mongo exception message")).when(subscriptionAttributesCollection).remove(any(DBObject.class));

        accountDao.removeSubscriptionAttributes(subscriptionId);
    }

    @Test
    public void shouldBeAbleToRemoveSubscriptionAttributes() throws ServerException, NotFoundException {
        final String subscriptionId = "subscription-id";
        final SubscriptionAttributes attributes = createSubscriptionAttributes();
        subscriptionAttributesCollection.save(accountDao.toDBObject(subscriptionId, attributes));
        assertNotNull(subscriptionAttributesCollection.findOne(new BasicDBObject("_id", subscriptionId)));

        accountDao.removeSubscriptionAttributes(subscriptionId);

        assertNull(subscriptionAttributesCollection.findOne(new BasicDBObject("_id", subscriptionId)));
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

    private SubscriptionAttributes createSubscriptionAttributes() {
        return new SubscriptionAttributes().withTrialDuration(7)
                                           .withStartDate("11/12/2014")
                                           .withEndDate("11/12/2015")
                                           .withDescription("description")
                                           .withCustom(singletonMap("key", "value"))
                                           .withBilling(new Billing().withStartDate("11/12/2014")
                                                                     .withEndDate("11/12/2015")
                                                                     .withUsePaymentSystem("true")
                                                                     .withCycleType(1)
                                                                     .withCycle(1)
                                                                     .withContractTerm(1));
    }

    private Subscription createSubscription() {
        final HashMap<String, String> properties = new HashMap<>(4);
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        return new Subscription().withId("test_subscription_id")
                                 .withAccountId("test_account_id")
                                 .withPlanId("test_plan_id")
                                 .withServiceId("test_service_id")
                                 .withProperties(properties);
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
