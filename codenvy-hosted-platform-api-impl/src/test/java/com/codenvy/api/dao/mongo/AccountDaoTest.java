/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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

import com.codenvy.api.account.shared.dto.Account;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.account.shared.dto.Attribute;
import com.codenvy.api.account.shared.dto.Member;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link AccountDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class AccountDaoTest extends BaseDaoTest {

    private static final String USER_ID = "user12837asjhda823981h";

    private static final String ACCOUNT_ID    = "org123abc456def";
    private static final String ACCOUNT_NAME  = "account";
    private static final String ACCOUNT_OWNER = "user123@codenvy.com";

    private static final String ACC_COLL_NAME          = "accounts";
    private static final String SUBSCRIPTION_COLL_NAME = "subscriptions";
    private static final String MEMBER_COLL_NAME       = "members";

    private static final String SUBSCRIPTION_ID = "Subscription0xfffffff";
    private static final String SERVICE_NAME    = "builder";
    private static final long   START_DATE      = System.currentTimeMillis();
    private static final long   END_DATE        = START_DATE + /* 1 day ms */ 86_400_000;
    private static final Map<String, String> PROPS;


    AccountDaoImpl accountDao;
    DBCollection   subscriptionCollection;
    DBCollection   membersCollection;

    @Mock
    WorkspaceDao workspaceDao;

    static {
        PROPS = new HashMap<>();
        PROPS.put("key1", "value1");
        PROPS.put("key2", "value2");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(ACC_COLL_NAME);
        accountDao = new AccountDaoImpl(db, workspaceDao, ACC_COLL_NAME, SUBSCRIPTION_COLL_NAME, MEMBER_COLL_NAME);
        subscriptionCollection = db.getCollection(SUBSCRIPTION_COLL_NAME);
        membersCollection = db.getCollection(MEMBER_COLL_NAME);
    }

    @Test
    public void shouldCreateAccount() throws Exception {
        Account account =
                DtoFactory.getInstance().createDto(Account.class)
                          .withId(ACCOUNT_ID)
                          .withName(ACCOUNT_NAME)
                          .withAttributes(getAttributes());

        accountDao.create(account);

        DBObject res = collection.findOne(new BasicDBObject("id", ACCOUNT_ID));
        assertNotNull(res, "Specified user account does not exists.");

        Account result =
                DtoFactory.getInstance().createDtoFromJson(res.toString(), Account.class);
        assertEquals(account.getLinks(), result.getLinks());
        assertEquals(result, account);
    }

    @Test
    public void shouldFindAccountById() throws Exception {
        collection.insert(
                new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));
        Account result = accountDao.getById(ACCOUNT_ID);
        assertNotNull(result);
        assertEquals(result.getName(), ACCOUNT_NAME);
    }

    @Test
    public void shouldFindAccountByName() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));
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
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME));
        collection.insert(new BasicDBObject("id", "fake").append("name", "fake"));
        BasicDBList members = new BasicDBList();
        members.add(JSON.parse(DtoFactory.getInstance().createDto(Member.class).withAccountId(ACCOUNT_ID)
                                         .withRoles(Arrays.asList("account/owner")).withUserId(USER_ID).toString()));
        members.add(JSON.parse(DtoFactory.getInstance().createDto(Member.class).withAccountId("fake")
                                         .withRoles(Arrays.asList("account/member")).withUserId(USER_ID).toString()));
        membersCollection.insert(new BasicDBObject("_id", USER_ID).append("members", members));
        List<Account> result = accountDao.getByOwner(USER_ID);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId(), ACCOUNT_ID);
        assertEquals(result.get(0).getName(), ACCOUNT_NAME);
    }

    @Test
    public void shouldUpdateAccount() throws Exception {
        Account account = DtoFactory.getInstance().createDto(Account.class)
                                    .withId(ACCOUNT_ID)
                                    .withName(ACCOUNT_NAME)
                                    .withAttributes(getAttributes());
        // Put first object
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));
        // main invoke
        accountDao.update(account);

        DBObject res = collection.findOne(new BasicDBObject("id", ACCOUNT_ID));
        assertNotNull(res, "Specified user profile does not exists.");

        Account result = DtoFactory.getInstance().createDtoFromJson(res.toString(), Account.class);

        assertEquals(account.getLinks(), result.getLinks());
        assertEquals(account, result);
    }

    @Test
    public void shouldRemoveAccount() throws Exception {
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.<Workspace>emptyList());
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        List<String> roles = Arrays.asList("account/admin", "account/member");
        Member member1 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId(USER_ID)
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
        when(workspaceDao.getByAccount(ACCOUNT_ID))
                .thenReturn(Arrays.asList(DtoFactory.getInstance().createDto(Workspace.class)));
        collection.insert(
                new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        List<String> roles = Arrays.asList("account/admin", "account/manager");
        Member member1 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId(USER_ID)
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
        Member member = DtoFactory.getInstance().createDto(Member.class)
                                  .withUserId(USER_ID)
                                  .withAccountId(ACCOUNT_ID)
                                  .withRoles(roles);
        accountDao.addMember(member);

        DBObject res = membersCollection.findOne(new BasicDBObject("_id", USER_ID));
        assertNotNull(res, "Specified user membership does not exists.");

        for (Object dbMembership : (BasicDBList)res.get("members")) {
            Member membership = DtoFactory.getInstance().createDtoFromJson(dbMembership.toString(), Member.class);
            assertEquals(membership.getAccountId(), ACCOUNT_ID);
            assertEquals(roles, membership.getRoles());
        }
    }

    @Test
    public void shouldFindMembers() throws Exception {
        List<String> roles = Arrays.asList("account/admin", "account/manager");
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));
        Member member1 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId(USER_ID)
                                   .withAccountId(ACCOUNT_ID)
                                   .withRoles(roles.subList(0, 1));
        Member member2 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId("anotherUserId")
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
        Member accountOwner = DtoFactory.getInstance().createDto(Member.class)
                                        .withUserId(USER_ID)
                                        .withAccountId(ACCOUNT_ID)
                                        .withRoles(Arrays.asList("account/owner"));
        Member accountMember = DtoFactory.getInstance().createDto(Member.class)
                                         .withUserId("user2")
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
        Member accountOwner = DtoFactory.getInstance().createDto(Member.class)
                                        .withUserId(USER_ID)
                                        .withAccountId(ACCOUNT_ID)
                                        .withRoles(Arrays.asList("account/owner"));
        Member accountOwner2 = DtoFactory.getInstance().createDto(Member.class)
                                         .withUserId("user2")
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
        members.add(DtoFactory.getInstance().createDto(Member.class)
                              .withAccountId(ACCOUNT_ID)
                              .withUserId(USER_ID)
                              .withRoles(Arrays.asList("account/owner")).toString());
        membersCollection.insert(new BasicDBObject("_id", USER_ID).append("members", members));
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME));
        List<AccountMembership> memberships = accountDao.getByMember(USER_ID);
        assertEquals(memberships.size(), 1);
        assertEquals(memberships.get(0).getRoles(), Arrays.asList("account/owner"));
    }

    @Test
    public void shouldAddSubscription() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        Subscription ss = DtoFactory.getInstance().createDto(Subscription.class)
                                    .withId(SUBSCRIPTION_ID)
                                    .withAccountId(ACCOUNT_ID)
                                    .withServiceId(SERVICE_NAME)
                                    .withStartDate(START_DATE)
                                    .withEndDate(END_DATE)
                                    .withProperties(PROPS);

        accountDao.addSubscription(ss);

        DBObject res = subscriptionCollection.findOne(new BasicDBObject("accountId", ACCOUNT_ID));
        assertNotNull(res, "Specified subscription does not exists.");

        DBCursor dbSubscriptions = subscriptionCollection.find(new BasicDBObject("id", SUBSCRIPTION_ID));
        for (DBObject currentSubscription : dbSubscriptions) {
            Subscription subscription = DtoFactory.getInstance().createDtoFromJson(currentSubscription.toString(), Subscription.class);
            assertEquals(subscription.getServiceId(), SERVICE_NAME);
            assertEquals(subscription.getAccountId(), ACCOUNT_ID);
            assertEquals(subscription.getStartDate(), START_DATE);
            assertEquals(subscription.getEndDate(), END_DATE);
            assertEquals(subscription.getProperties(), PROPS);
        }
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowAnExceptionWhileAddingSubscriptionToNotExistedAccount() throws ServerException,
                                                                                          ConflictException, NotFoundException {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));

        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withId(SUBSCRIPTION_ID)
                                              .withAccountId("DO_NOT_EXIST")
                                              .withServiceId(SERVICE_NAME)
                                              .withStartDate(START_DATE)
                                              .withEndDate(END_DATE)
                                              .withProperties(PROPS);

        accountDao.addSubscription(subscription);
    }

    @Test
    public void shouldFindSubscriptions() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME)
                                                             .append("owner", ACCOUNT_OWNER));

        Subscription ss1 = DtoFactory.getInstance().createDto(Subscription.class)
                                     .withAccountId(ACCOUNT_ID)
                                     .withServiceId(SERVICE_NAME)
                                     .withStartDate(START_DATE)
                                     .withEndDate(END_DATE)
                                     .withProperties(PROPS);
        Subscription ss2 = DtoFactory.getInstance().createDto(Subscription.class)
                                     .withAccountId(ACCOUNT_ID)
                                     .withServiceId(SERVICE_NAME)
                                     .withStartDate(START_DATE)
                                     .withEndDate(END_DATE)
                                     .withProperties(PROPS);

        accountDao.addSubscription(ss1);
        accountDao.addSubscription(ss2);

        List<Subscription> found = accountDao.getSubscriptions(ACCOUNT_ID);
        assertEquals(found.size(), 2);
    }

    @Test
    public void shouldRemoveSubscription() throws Exception {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));
        collection.insert(new BasicDBObject("id", "another_account").append("name", ACCOUNT_NAME).append("owner",
                                                                                                         ACCOUNT_OWNER));
        Subscription ss = DtoFactory.getInstance().createDto(Subscription.class)
                                    .withId(SUBSCRIPTION_ID)
                                    .withAccountId(ACCOUNT_ID)
                                    .withServiceId(SERVICE_NAME)
                                    .withStartDate(START_DATE)
                                    .withEndDate(END_DATE)
                                    .withProperties(PROPS);

        accountDao.addSubscription(ss);

        final String anotherSubscriptionId = "Subscription0x00000000f";
        ss.setId(anotherSubscriptionId);
        ss.setAccountId("another_account");

        accountDao.addSubscription(ss);

        accountDao.removeSubscription(SUBSCRIPTION_ID);

        assertNull(subscriptionCollection.findOne(new BasicDBObject("id", SUBSCRIPTION_ID)));
        assertNotNull(subscriptionCollection.findOne(new BasicDBObject("id", anotherSubscriptionId)));
    }

    @Test
    public void shouldGetSubscriptionById() throws ServerException, NotFoundException, ConflictException {
        collection.insert(new BasicDBObject("id", ACCOUNT_ID).append("name", ACCOUNT_NAME).append("owner", ACCOUNT_OWNER));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withId(SUBSCRIPTION_ID)
                                              .withAccountId(ACCOUNT_ID)
                                              .withServiceId(SERVICE_NAME)
                                              .withStartDate(START_DATE)
                                              .withEndDate(END_DATE)
                                              .withProperties(PROPS);

        accountDao.addSubscription(subscription);

        Subscription actual = accountDao.getSubscriptionById(SUBSCRIPTION_ID);

        assertNotNull(actual);
        assertEquals(actual, subscription);
    }

    private List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(DtoFactory.getInstance().createDto(Attribute.class).withName("attr1").withValue("value1")
                                 .withDescription("description1"));
        attributes.add(DtoFactory.getInstance().createDto(Attribute.class).withName("attr2").withValue("value2")
                                 .withDescription("description2"));
        attributes.add(DtoFactory.getInstance().createDto(Attribute.class).withName("attr3").withValue("value3")
                                 .withDescription("description3"));
        return attributes;
    }
}
