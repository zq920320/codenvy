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
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
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

    private static final String USER_ID                        = "user12837asjhda823981h";
    private static final String ACCOUNT_ID                     = "org123abc456def";
    private static final String ACCOUNT_NAME                   = "account";
    private static final String ACCOUNT_OWNER                  = "user123@codenvy.com";
    private static final String ACC_COLL_NAME                  = "accounts";
    private static final String SUBSCRIPTION_COLL_NAME         = "subscriptions";
    private static final String MEMBER_COLL_NAME               = "members";
    private static final String SUBSCRIPTION_HISTORY_COLL_NAME = "history";
    private static final String SUBSCRIPTION_ID                = "Subscription0xfffffff";
    private static final String SERVICE_NAME                   = "builder";
    private static final String PLAN_ID                        = "plan_id";
    private static final Subscription        defaultSubscription;
    private static final Map<String, String> PROPS;

    @Mock
    private WorkspaceDao   workspaceDao;
    private AccountDaoImpl accountDao;
    private DBCollection   subscriptionCollection;
    private DBCollection   membersCollection;
    private DBCollection   subscriptionHistoryCollection;

    static {
        PROPS = new HashMap<>(2);
        PROPS.put("key1", "value1");
        PROPS.put("key2", "value2");
        defaultSubscription = new Subscription().withId(SUBSCRIPTION_ID)
                                                .withAccountId(ACCOUNT_ID)
                                                .withPlanId(PLAN_ID)
                                                .withServiceId(SERVICE_NAME)
                                                .withProperties(PROPS);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(ACC_COLL_NAME);
        db = spy(db);
        collection = spy(db.getCollection(ACC_COLL_NAME));
        subscriptionCollection = spy(db.getCollection(SUBSCRIPTION_COLL_NAME));
        membersCollection = spy(db.getCollection(MEMBER_COLL_NAME));
        subscriptionHistoryCollection = spy(db.getCollection(SUBSCRIPTION_HISTORY_COLL_NAME));
        when(db.getCollection(ACC_COLL_NAME)).thenReturn(collection);
        when(db.getCollection(SUBSCRIPTION_COLL_NAME)).thenReturn(subscriptionCollection);
        when(db.getCollection(MEMBER_COLL_NAME)).thenReturn(membersCollection);
        when(db.getCollection(SUBSCRIPTION_HISTORY_COLL_NAME)).thenReturn(subscriptionHistoryCollection);
        accountDao = new AccountDaoImpl(db,
                                        workspaceDao,
                                        ACC_COLL_NAME,
                                        SUBSCRIPTION_COLL_NAME,
                                        MEMBER_COLL_NAME,
                                        SUBSCRIPTION_HISTORY_COLL_NAME);
    }

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void shouldCreateAccount() throws Exception {
        Account account = new Account().withId(ACCOUNT_ID)
                                       .withName(ACCOUNT_NAME)
                                       .withAttributes(getAttributes());

        accountDao.create(account);

        DBObject res = collection.findOne(new BasicDBObject("id", ACCOUNT_ID));
        assertNotNull(res, "Specified user account does not exists.");

        Account result = accountDao.toAccount(res);
        assertEquals(result, account);
    }

    @Test
    public void shouldFindAccountById() throws Exception {
        collection.insert(new BasicDBObject().append("id", ACCOUNT_ID)
                                             .append("name", ACCOUNT_NAME)
                                             .append("owner", ACCOUNT_OWNER)
                                             .append("attributes", new BasicDBList()));
        Account result = accountDao.getById(ACCOUNT_ID);
        assertNotNull(result);
        assertEquals(result.getName(), ACCOUNT_NAME);
    }

    @Test
    public void shouldFindAccountByName() throws Exception {
        collection.insert(new BasicDBObject().append("id", ACCOUNT_ID)
                                             .append("name", ACCOUNT_NAME)
                                             .append("owner", ACCOUNT_OWNER)
                                             .append("attributes", new BasicDBList()));
        Account result = accountDao.getByName(ACCOUNT_NAME);
        assertNotNull(result);
        assertEquals(result.getId(), ACCOUNT_ID);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldNotFindUnExistingAccountByName() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));
        Account result = accountDao.getByName("randomName");
        assertNull(result);
    }

    @Test
    public void shouldFindAccountByOwner() throws Exception {
        collection.insert(new BasicDBObject().append("id", ACCOUNT_ID)
                                             .append("name", ACCOUNT_NAME)
                                             .append("owner", ACCOUNT_OWNER)
                                             .append("attributes", new BasicDBList()));
        collection.insert(new BasicDBObject().append("id", "fake")
                                             .append("name", "fake")
                                             .append("owner", "fake")
                                             .append("attributes", new BasicDBList()));
        BasicDBList members = new BasicDBList();
        members.add(accountDao.toDBObject(new Member().withAccountId(ACCOUNT_ID)
                                                      .withRoles(Arrays.asList("account/owner"))
                                                      .withUserId(USER_ID)));
        members.add(accountDao.toDBObject(new Member().withAccountId("fake")
                                                      .withRoles(Arrays.asList("account/member"))
                                                      .withUserId(USER_ID)));
        membersCollection.insert(new BasicDBObject().append("_id", USER_ID)
                                                    .append("members", members));
        List<Account> result = accountDao.getByOwner(USER_ID);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId(), ACCOUNT_ID);
        assertEquals(result.get(0).getName(), ACCOUNT_NAME);
    }

    @Test
    public void shouldUpdateAccount() throws Exception {
        Account account = new Account().withId(ACCOUNT_ID)
                                       .withName(ACCOUNT_NAME)
                                       .withAttributes(getAttributes());
        // Put first object
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));
        // main invoke
        accountDao.update(account);

        DBObject res = collection.findOne(new BasicDBObject("id", ACCOUNT_ID));
        assertNotNull(res, "Specified user profile does not exists.");

        Account result = accountDao.toAccount(res);

        assertEquals(account, result);
    }

    @Test
    public void shouldRemoveAccount() throws Exception {
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.<Workspace>emptyList());
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        List<String> roles = Arrays.asList("account/admin", "account/member");
        Member member1 = new Member().withUserId(USER_ID)
                                     .withAccountId(ACCOUNT_ID)
                                     .withRoles(roles.subList(0, 1));
        subscriptionCollection.insert(new BasicDBObject("accountId", ACCOUNT_ID));
        accountDao.addMember(member1);

        accountDao.remove(ACCOUNT_ID);
        assertNull(collection.findOne(new BasicDBObject("id", ACCOUNT_ID)));
        assertNull(membersCollection.findOne(new BasicDBObject("_id", USER_ID)));
        assertNull(subscriptionCollection.findOne(new BasicDBObject("accountId", ACCOUNT_ID)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "It is not possible to remove account that has associated workspaces")
    public void shouldNotBeAbleToRemoveAccountWithAssociatedWorkspace() throws Exception {
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(new Workspace()));
        collection.insert(
                new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        List<String> roles = Arrays.asList("account/admin", "account/manager");
        Member member1 = new Member().withUserId(USER_ID)
                                     .withAccountId(ACCOUNT_ID)
                                     .withRoles(roles.subList(0, 1));
        accountDao.addMember(member1);

        accountDao.remove(ACCOUNT_ID);
    }

    @Test
    public void shouldAddMember() throws Exception {
        List<String> roles = Arrays.asList("account/admin", "account/manager");
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));
        Member member = new Member().withUserId(USER_ID)
                                    .withAccountId(ACCOUNT_ID)
                                    .withRoles(roles);
        accountDao.addMember(member);

        DBObject res = membersCollection.findOne(new BasicDBObject("_id", USER_ID));
        assertNotNull(res, "Specified user membership does not exists.");

        for (Object dbMembership : (BasicDBList)res.get("members")) {
            Member membership = accountDao.toMember(dbMembership);
            assertEquals(membership.getAccountId(), ACCOUNT_ID);
            assertEquals(roles, membership.getRoles());
        }
    }

    @Test
    public void shouldFindMembers() throws Exception {
        List<String> roles = Arrays.asList("account/admin", "account/manager");
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));
        Member member1 = new Member().withUserId(USER_ID)
                                     .withAccountId(ACCOUNT_ID)
                                     .withRoles(roles.subList(0, 1));
        Member member2 = new Member().withUserId("anotherUserId")
                                     .withAccountId(ACCOUNT_ID)
                                     .withRoles(roles);

        accountDao.addMember(member1);
        accountDao.addMember(member2);

        List<Member> found = accountDao.getMembers(ACCOUNT_ID);
        assertEquals(found.size(), 2);
    }

    @Test
    public void shouldRemoveMembers() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME));
        Member accountOwner = new Member().withUserId(USER_ID)
                                          .withAccountId(ACCOUNT_ID)
                                          .withRoles(Arrays.asList("account/owner"));
        Member accountMember = new Member().withUserId("user2")
                                           .withAccountId(ACCOUNT_ID)
                                           .withRoles(Arrays.asList("account/member"));

        accountDao.addMember(accountOwner);
        accountDao.addMember(accountMember);

        accountDao.removeMember(accountMember);

        assertNull(membersCollection.findOne(new BasicDBObject("_id", accountMember.getUserId())));
        assertNotNull(membersCollection.findOne(new BasicDBObject("_id", accountOwner.getUserId())));
    }

    @Test
    public void shouldBeAbleToRemoveAccountOwnerIfOtherOneExists() throws ConflictException, NotFoundException, ServerException {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME));
        Member accountOwner = new Member().withUserId(USER_ID)
                                          .withAccountId(ACCOUNT_ID)
                                          .withRoles(Arrays.asList("account/owner"));
        Member accountOwner2 = new Member().withUserId("user2")
                                           .withAccountId(ACCOUNT_ID)
                                           .withRoles(Arrays.asList("account/owner"));

        accountDao.addMember(accountOwner);
        accountDao.addMember(accountOwner2);

        accountDao.removeMember(accountOwner);

        assertNull(membersCollection.findOne(new BasicDBObject("_id", accountOwner.getUserId())));
        assertNotNull(membersCollection.findOne(new BasicDBObject("_id", accountOwner2.getUserId())));
    }

    @Test
    public void shouldBeAbleToGetAccountMembershipsByMember() throws NotFoundException, ServerException {
        BasicDBList members = new BasicDBList();
        members.add(accountDao.toDBObject(new Member().withAccountId(ACCOUNT_ID)
                                                      .withUserId(USER_ID)
                                                      .withRoles(Arrays.asList("account/owner"))));
        membersCollection.insert(new BasicDBObject("_id", USER_ID).append("members", members));
        collection.insert(new BasicDBObject().append("id", ACCOUNT_ID)
                                             .append("name", ACCOUNT_NAME));
        List<Member> memberships = accountDao.getByMember(USER_ID);
        assertEquals(memberships.size(), 1);
        assertEquals(memberships.get(0).getRoles(), Arrays.asList("account/owner"));
    }

    @Test
    public void shouldBeAbleToAddSubscription() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        Subscription ss = new Subscription().withId(SUBSCRIPTION_ID)
                                            .withAccountId(ACCOUNT_ID)
                                            .withPlanId(PLAN_ID)
                                            .withServiceId(SERVICE_NAME)
                                            .withProperties(PROPS);

        accountDao.addSubscription(ss);

        DBObject res = subscriptionCollection.findOne(new BasicDBObject("accountId", ACCOUNT_ID));
        assertNotNull(res, "Specified subscription does not exists.");

        DBCursor dbSubscriptions = subscriptionCollection.find(new BasicDBObject("id", SUBSCRIPTION_ID));
        for (DBObject currentSubscription : dbSubscriptions) {
            Subscription subscription = accountDao.toSubscription(currentSubscription);
            assertEquals(subscription.getServiceId(), SERVICE_NAME);
            assertEquals(subscription.getAccountId(), ACCOUNT_ID);
            assertEquals(subscription.getPlanId(), PLAN_ID);
            assertEquals(subscription.getProperties(), PROPS);
        }
    }

    @Test
    public void shouldBeAbleToUpdateSubscription() throws Exception {
        collection.insert(new BasicDBObject().append("id", ACCOUNT_ID)
                                             .append("name", ACCOUNT_NAME)
                                             .append("owner", ACCOUNT_OWNER));
        Subscription ss = new Subscription()
                .withId(SUBSCRIPTION_ID)
                .withAccountId(ACCOUNT_ID)
                .withServiceId(SERVICE_NAME)
                .withPlanId(PLAN_ID)
                .withProperties(new HashMap<String, String>());

        subscriptionCollection.insert(new BasicDBObject().append("id", SUBSCRIPTION_ID)
                                                         .append("accountId", ACCOUNT_ID)
                                                         .append("planId", PLAN_ID)
                                                         .append("serviceId", SERVICE_NAME)
                                                         .append("properties", new BasicDBObject()));

        accountDao.updateSubscription(ss);

        DBCursor newDbSubscription = subscriptionCollection.find(new BasicDBObject("id", SUBSCRIPTION_ID));
        assertEquals(accountDao.toSubscription(newDbSubscription.next()), ss);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfSubscriptionDoesNotExist() throws Exception {
        Subscription ss = new Subscription().withId(SUBSCRIPTION_ID)
                                            .withAccountId(ACCOUNT_ID)
                                            .withPlanId(PLAN_ID)
                                            .withServiceId(SERVICE_NAME);

        accountDao.updateSubscription(ss);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnUpdateSubscriptionsIfMongoExceptionOccurs()
            throws ServerException, NotFoundException, ConflictException {
        Subscription ss = new Subscription().withId(SUBSCRIPTION_ID)
                                            .withAccountId(ACCOUNT_ID)
                                            .withPlanId(PLAN_ID)
                                            .withServiceId(SERVICE_NAME);

        doThrow(new MongoException("")).when(subscriptionCollection).findOne(new BasicDBObject("id", SUBSCRIPTION_ID));
        accountDao.updateSubscription(ss);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnUpdateSubscriptionsIfMongoExceptionOccurs2()
            throws ServerException, NotFoundException, ConflictException {
        subscriptionCollection.insert(new BasicDBObject().append("id", SUBSCRIPTION_ID)
                                                         .append("accountId", ACCOUNT_ID)
                                                         .append("serviceId", SERVICE_NAME)
                                                         .append("planId", PLAN_ID)
                                                         .append("properties", new BasicDBObject()));
        Subscription ss = new Subscription().withId(SUBSCRIPTION_ID)
                                            .withAccountId(ACCOUNT_ID)
                                            .withServiceId(SERVICE_NAME)
                                            .withPlanId(PLAN_ID)
                                            .withProperties(new HashMap<String, String>());
        doThrow(new MongoException("")).when(subscriptionCollection).update(any(DBObject.class), any(DBObject.class));
        accountDao.updateSubscription(ss);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowAnExceptionWhileAddingSubscriptionToNotExistedAccount() throws ServerException,
                                                                                          ConflictException, NotFoundException {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        Subscription subscription = new Subscription().withId(SUBSCRIPTION_ID)
                                                      .withAccountId("DO_NOT_EXIST")
                                                      .withServiceId(SERVICE_NAME)
                                                      .withPlanId(PLAN_ID)
                                                      .withProperties(PROPS);

        accountDao.addSubscription(subscription);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnAddSubscriptionsIfMongoExceptionOccurs()
            throws ServerException, NotFoundException, ConflictException {
        doThrow(new MongoException("")).when(collection).findOne(new BasicDBObject("id", ACCOUNT_ID));
        Subscription subscription = new Subscription().withId(SUBSCRIPTION_ID)
                                                      .withAccountId(ACCOUNT_ID)
                                                      .withServiceId(SERVICE_NAME)
                                                      .withPlanId(PLAN_ID)
                                                      .withProperties(PROPS);
        accountDao.addSubscription(subscription);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnAddSubscriptionsIfMongoExceptionOccurs2()
            throws ServerException, NotFoundException, ConflictException {
        Subscription subscription = new Subscription().withId(SUBSCRIPTION_ID)
                                                      .withAccountId(ACCOUNT_ID)
                                                      .withServiceId(SERVICE_NAME)
                                                      .withPlanId(PLAN_ID)
                                                      .withProperties(PROPS);
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));

        doThrow(new MongoException("")).when(subscriptionCollection).save(accountDao.toDBObject(subscription));
        accountDao.addSubscription(subscription);
    }

    @Test
    public void shouldBeAbleToGetSubscriptions() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));

        Subscription ss1 = new Subscription().withAccountId(ACCOUNT_ID)
                                             .withServiceId(SERVICE_NAME)
                                             .withProperties(PROPS)
                                             .withPlanId(PLAN_ID);
        Subscription ss2 = new Subscription().withAccountId(ACCOUNT_ID)
                                             .withServiceId(SERVICE_NAME)
                                             .withProperties(PROPS)
                                             .withPlanId(PLAN_ID);

        subscriptionCollection.save(accountDao.toDBObject(ss1));
        subscriptionCollection.save(accountDao.toDBObject(ss2));

        List<Subscription> found = accountDao.getSubscriptions(ACCOUNT_ID);
        assertEquals(found.size(), 2);
    }

    @Test
    public void shouldReturnEmptyListIfThereIsNoSubscriptionsOnGetSubscriptions() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));

        List<Subscription> found = accountDao.getSubscriptions(ACCOUNT_ID);
        assertEquals(found.size(), 0);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionOnGetSubscriptionsWithInvalidAccountId() throws ServerException, NotFoundException {
        accountDao.getSubscriptions(ACCOUNT_ID);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsIfMongoExceptionOccurs()
            throws ServerException, NotFoundException, ConflictException {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));

        doThrow(new MongoException("")).when(subscriptionCollection).find(new BasicDBObject("accountId", ACCOUNT_ID));
        accountDao.getSubscriptions(ACCOUNT_ID);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsIfMongoExceptionOccurs2()
            throws ServerException, NotFoundException, ConflictException {
        doThrow(new MongoException("")).when(collection).findOne(new BasicDBObject("id", ACCOUNT_ID));
        accountDao.getSubscriptions(ACCOUNT_ID);
    }

    @Test
    public void shouldBeAbleToRemoveSubscription() throws Exception {
        Subscription ss = new Subscription().withId(SUBSCRIPTION_ID)
                                            .withAccountId(ACCOUNT_ID)
                                            .withServiceId(SERVICE_NAME)
                                            .withProperties(PROPS)
                                            .withPlanId(PLAN_ID);

        subscriptionCollection.save(accountDao.toDBObject(ss));

        final String anotherSubscriptionId = "Subscription0x00000000f";
        ss.setId(anotherSubscriptionId);
        ss.setAccountId("another_account");

        subscriptionCollection.save(accountDao.toDBObject(ss));

        accountDao.removeSubscription(SUBSCRIPTION_ID);

        assertNull(subscriptionCollection.findOne(new BasicDBObject("id", SUBSCRIPTION_ID)));
        assertNotNull(subscriptionCollection.findOne(new BasicDBObject("id", anotherSubscriptionId)));
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Subscription not found " + SUBSCRIPTION_ID)
    public void shouldThrowNotFoundExceptionOnRemoveSubscriptionsWithInvalidSubscriptionId() throws ServerException, NotFoundException {
        accountDao.removeSubscription(SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnRemoveSubscriptionsIfMongoExceptionOccurs()
            throws ServerException, NotFoundException, ConflictException {
        doThrow(new MongoException("")).when(subscriptionCollection).findOne(new BasicDBObject("id", SUBSCRIPTION_ID));
        accountDao.removeSubscription(SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnRemoveSubscriptionsIfMongoExceptionOccurs2()
            throws ServerException, NotFoundException, ConflictException {
        Subscription ss = new Subscription().withId(SUBSCRIPTION_ID)
                                            .withAccountId(ACCOUNT_ID)
                                            .withServiceId(SERVICE_NAME)
                                            .withProperties(PROPS)
                                            .withPlanId(PLAN_ID);

        subscriptionCollection.save(accountDao.toDBObject(ss));

        doThrow(new MongoException("")).when(subscriptionCollection).remove(new BasicDBObject("id", SUBSCRIPTION_ID));

        accountDao.removeSubscription(SUBSCRIPTION_ID);
    }

    @Test
    public void shouldBeAbleToGetSubscriptionById() throws ServerException, NotFoundException, ConflictException {
        collection.insert(new BasicDBObject().append("id", ACCOUNT_ID)
                                             .append("name", ACCOUNT_NAME)
                                             .append("owner", ACCOUNT_OWNER));
        Subscription subscription = new Subscription().withId(SUBSCRIPTION_ID)
                                                      .withAccountId(ACCOUNT_ID)
                                                      .withServiceId(SERVICE_NAME)
                                                      .withProperties(PROPS)
                                                      .withPlanId(PLAN_ID);

        subscriptionCollection.save(accountDao.toDBObject(subscription));

        Subscription actual = accountDao.getSubscriptionById(SUBSCRIPTION_ID);

        assertNotNull(actual);
        assertEquals(actual, subscription);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Subscription not found " + SUBSCRIPTION_ID)
    public void shouldThrowNotFoundExceptionOnGetSubscriptionsByIdWithInvalidSubscriptionId() throws ServerException, NotFoundException {
        accountDao.getSubscriptionById(SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetSubscriptionsByIdIfMongoExceptionOccurs()
            throws ServerException, NotFoundException, ConflictException {
        doThrow(new MongoException("")).when(subscriptionCollection).findOne(new BasicDBObject("id", SUBSCRIPTION_ID));
        accountDao.getSubscriptionById(SUBSCRIPTION_ID);
    }

    @Test
    public void shouldBeAbleToGetAllSubscriptions() throws ServerException {
        Subscription ss1 = new Subscription().withAccountId(ACCOUNT_ID)
                                             .withServiceId(SERVICE_NAME)
                                             .withProperties(PROPS)
                                             .withPlanId(PLAN_ID);
        Subscription ss2 = new Subscription().withAccountId("ANOTHER" + ACCOUNT_ID)
                                             .withServiceId("ANOTHER" + SERVICE_NAME)
                                             .withProperties(PROPS)
                                             .withPlanId(PLAN_ID);

        subscriptionCollection.save(accountDao.toDBObject(ss1));
        subscriptionCollection.save(accountDao.toDBObject(ss2));

        List<Subscription> actual = accountDao.getSubscriptions();

        assertEquals(actual, Arrays.asList(ss1, ss2));
    }

    @Test
    public void shouldReturnEmptyCollectionIfThereIsNoSubscriptionsOnGetAllSubscriptions() throws ServerException {
        assertTrue(accountDao.getSubscriptions().isEmpty());
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "mongo exception")
    public void shouldThrowServerExceptionIfMongoExceptionIsThrownOnGetAllSubscriptions() throws ServerException {
        when(subscriptionCollection.find()).thenThrow(new MongoException("mongo exception"));

        accountDao.getSubscriptions();
    }

    private Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        return attributes;
    }
}
