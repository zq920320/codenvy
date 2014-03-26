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

import com.codenvy.api.organization.server.exception.OrganizationException;
import com.codenvy.api.organization.shared.dto.Organization;
import com.codenvy.api.organization.shared.dto.Attribute;
import com.codenvy.api.organization.shared.dto.Member;
import com.codenvy.api.organization.shared.dto.Subscription;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link com.codenvy.api.dao.mongo.OrganizationDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
public class OrganizationDaoTest extends BaseDaoTest {

    private static final String USER_ID = "user12837asjhda823981h";

    private static final String ORGANIZATION_ID    = "org123abc456def";
    private static final String ORGANIZATION_NAME  = "organization1";
    private static final String ORGANIZATION_OWNER = "user123@codenvy.com";

    private static final String ORG_COLL_NAME          = "organizations";
    private static final String SUBSCRIPTION_COLL_NAME = "subscriptions";
    private static final String MEMBER_COLL_NAME       = "members";

    private static final String SUBSCRIPTION_ID = "Subscription0xfffffff";
    private static final String SERVICE_NAME    = "builder";
    private static final long   START_DATE      = System.currentTimeMillis();
    private static final long   END_DATE        = START_DATE + /* 1 day ms */ 86_400_000;
    private static final Map<String, String> PROPS;


    OrganizationDaoImpl organizationDao;
    DBCollection        subscriptionCollection;
    DBCollection        membersCollection;

    static {
        PROPS = new HashMap<>();
        PROPS.put("key1", "value1");
        PROPS.put("key2", "value2");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(ORG_COLL_NAME);
        organizationDao = new OrganizationDaoImpl(db, ORG_COLL_NAME, SUBSCRIPTION_COLL_NAME, MEMBER_COLL_NAME);
        subscriptionCollection = db.getCollection(SUBSCRIPTION_COLL_NAME);
        membersCollection = db.getCollection(MEMBER_COLL_NAME);
    }

    @Test
    public void shouldCreateOrganization() throws Exception {
        Organization organization =
                DtoFactory.getInstance().createDto(Organization.class)
                          .withId(ORGANIZATION_ID)
                          .withName(ORGANIZATION_NAME)
                          .withOwner(ORGANIZATION_OWNER)
                          .withAttributes(getAttributes());

        organizationDao.create(organization);

        DBObject res = collection.findOne(new BasicDBObject("id", ORGANIZATION_ID));
        assertNotNull(res, "Specified user organization does not exists.");

        Organization result =
                DtoFactory.getInstance().createDtoFromJson(res.toString(), Organization.class);
        assertEquals(organization.getLinks(), result.getLinks());
        assertEquals(organization, result);
    }

    @Test
    public void shouldFindOrganizationById() throws Exception {
        collection.insert(
                new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getById(ORGANIZATION_ID);
        assertNotNull(result);
        assertEquals(result.getName(), ORGANIZATION_NAME);
        assertEquals(result.getOwner(), ORGANIZATION_OWNER);
    }

    @Test
    public void shouldFindOrganizationByName() throws Exception {
        collection.insert(
                new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getByName(ORGANIZATION_NAME);
        assertNotNull(result);
        assertEquals(result.getId(), ORGANIZATION_ID);
        assertEquals(result.getOwner(), ORGANIZATION_OWNER);
    }

    @Test
    public void shouldNotFindUnExistingOrganizationByName() throws Exception {
        collection.insert(
                new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getByName("randomName");
        assertNull(result);
    }


    @Test
    public void shouldFindOrganizationByOwner() throws Exception {
        collection.insert(
                new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getByOwner(ORGANIZATION_OWNER);
        assertNotNull(result);
        assertEquals(result.getId(), ORGANIZATION_ID);
        assertEquals(result.getName(), ORGANIZATION_NAME);
    }

    @Test
    public void shouldUpdateOrganization() throws Exception {
        Organization organization = DtoFactory.getInstance().createDto(Organization.class)
                                              .withId(ORGANIZATION_ID)
                                              .withName(ORGANIZATION_NAME)
                                              .withOwner(ORGANIZATION_OWNER)
                                              .withAttributes(getAttributes());
        // Put first object
        collection.insert(
                new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));
        // main invoke
        organizationDao.update(organization);

        DBObject res = collection.findOne(new BasicDBObject("id", ORGANIZATION_ID));
        assertNotNull(res, "Specified user profile does not exists.");

        Organization result = DtoFactory.getInstance().createDtoFromJson(res.toString(), Organization.class);

        assertEquals(organization.getLinks(), result.getLinks());
        assertEquals(organization, result);
    }

    @Test
    public void shouldRemoveOrganization() throws Exception {
        collection.insert(
                new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));

        List<String> roles = Arrays.asList("organization/admin", "organization/developer");
        Member member1 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId(USER_ID)
                                   .withOrganizationId(ORGANIZATION_ID)
                                   .withRoles(roles.subList(0, 1));
        organizationDao.addMember(member1);

        organizationDao.remove(ORGANIZATION_ID);
        assertNull(collection.findOne(new BasicDBObject("id", ORGANIZATION_ID)));
        assertNull(membersCollection.findOne(new BasicDBObject("_id", USER_ID)));
    }

    @Test
    public void shouldAddMember() throws Exception {
        List<String> roles = Arrays.asList("organization/admin", "organization/developer");
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                                  .append("owner", ORGANIZATION_OWNER));
        Member member = DtoFactory.getInstance().createDto(Member.class)
                                  .withUserId(USER_ID)
                                  .withOrganizationId(ORGANIZATION_ID)
                                  .withRoles(roles);
        organizationDao.addMember(member);

        DBObject res = membersCollection.findOne(new BasicDBObject("_id", USER_ID));
        assertNotNull(res, "Specified user membership does not exists.");

        for (Object dbMembership : (BasicDBList)res.get("members")) {
            Member membership = DtoFactory.getInstance().createDtoFromJson(dbMembership.toString(), Member.class);
            assertEquals(membership.getOrganizationId(), ORGANIZATION_ID);
            assertEquals(roles, membership.getRoles());
        }
    }

    @Test
    public void shouldFindMembers() throws Exception {
        List<String> roles = Arrays.asList("organization/admin", "organization/developer");
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                                  .append("owner", ORGANIZATION_OWNER));
        Member member1 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId(USER_ID)
                                   .withOrganizationId(ORGANIZATION_ID)
                                   .withRoles(roles.subList(0, 1));
        Member member2 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId("anotherUserId")
                                   .withOrganizationId(ORGANIZATION_ID)
                                   .withRoles(roles);

        organizationDao.addMember(member1);
        organizationDao.addMember(member2);

        List<Member> found = organizationDao.getMembers(ORGANIZATION_ID);
        assertEquals(found.size(), 2);
    }


    @Test
    public void shouldRemoveMembers() throws Exception {
        List<String> roles = Arrays.asList("organization/admin", "organization/developer");
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                                  .append("owner", ORGANIZATION_OWNER));
        Member member1 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId(USER_ID)
                                   .withOrganizationId(ORGANIZATION_ID)
                                   .withRoles(roles.subList(0, 1));
        Member member2 = DtoFactory.getInstance().createDto(Member.class)
                                   .withUserId("user2")
                                   .withOrganizationId(ORGANIZATION_ID)
                                   .withRoles(roles);

        organizationDao.addMember(member1);
        organizationDao.addMember(member2);

        organizationDao.removeMember(ORGANIZATION_ID, USER_ID);

        assertNull(membersCollection.findOne(new BasicDBObject("_id", USER_ID)));
        assertNotNull(membersCollection.findOne(new BasicDBObject("_id", "user2")));
    }

    @Test
    public void shouldAddSubscription() throws Exception {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));

        Subscription ss = DtoFactory.getInstance().createDto(Subscription.class)
                                    .withId(SUBSCRIPTION_ID)
                                    .withOrganizationId(ORGANIZATION_ID)
                                    .withServiceId(SERVICE_NAME)
                                    .withStartDate(START_DATE)
                                    .withEndDate(END_DATE)
                                    .withProperties(PROPS);

        organizationDao.addSubscription(ss);

        DBObject res = subscriptionCollection.findOne(new BasicDBObject("organizationId", ORGANIZATION_ID));
        assertNotNull(res, "Specified subscription does not exists.");

        DBCursor dbSubscriptions = subscriptionCollection.find(new BasicDBObject("id", SUBSCRIPTION_ID));
        for (DBObject currentSubscription : dbSubscriptions) {
            Subscription subscription = DtoFactory.getInstance().createDtoFromJson(currentSubscription.toString(), Subscription.class);
            assertEquals(subscription.getServiceId(), SERVICE_NAME);
            assertEquals(subscription.getOrganizationId(), ORGANIZATION_ID);
            assertEquals(subscription.getStartDate(), START_DATE);
            assertEquals(subscription.getEndDate(), END_DATE);
            assertEquals(subscription.getProperties(), PROPS);
        }
    }

    @Test(expectedExceptions = OrganizationException.class)
    public void shouldThrowAnExceptionWhileAddingSubscriptionToNotExistedOrganization() throws OrganizationException {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));

        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withId(SUBSCRIPTION_ID)
                                              .withOrganizationId("DO_NOT_EXIST")
                                              .withServiceId(SERVICE_NAME)
                                              .withStartDate(START_DATE)
                                              .withEndDate(END_DATE)
                                              .withProperties(PROPS);

        organizationDao.addSubscription(subscription);
    }

    @Test
    public void shouldFindSubscriptions() throws Exception {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                                  .append("owner", ORGANIZATION_OWNER));

        Subscription ss1 = DtoFactory.getInstance().createDto(Subscription.class)
                                     .withOrganizationId(ORGANIZATION_ID)
                                     .withServiceId(SERVICE_NAME)
                                     .withStartDate(START_DATE)
                                     .withEndDate(END_DATE)
                                     .withProperties(PROPS);
        Subscription ss2 = DtoFactory.getInstance().createDto(Subscription.class)
                                     .withOrganizationId(ORGANIZATION_ID)
                                     .withServiceId(SERVICE_NAME)
                                     .withStartDate(START_DATE)
                                     .withEndDate(END_DATE)
                                     .withProperties(PROPS);

        organizationDao.addSubscription(ss1);
        organizationDao.addSubscription(ss2);

        List<Subscription> found = organizationDao.getSubscriptions(ORGANIZATION_ID);
        assertEquals(found.size(), 2);
    }

    @Test
    public void shouldRemoveSubscription() throws Exception {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));
        collection.insert(new BasicDBObject("id", "another_organization").append("name", ORGANIZATION_NAME).append("owner",
                                                                                                                   ORGANIZATION_OWNER));
        Subscription ss = DtoFactory.getInstance().createDto(Subscription.class)
                                    .withId(SUBSCRIPTION_ID)
                                    .withOrganizationId(ORGANIZATION_ID)
                                    .withServiceId(SERVICE_NAME)
                                    .withStartDate(START_DATE)
                                    .withEndDate(END_DATE)
                                    .withProperties(PROPS);

        organizationDao.addSubscription(ss);

        final String anotherSubscriptionId = "Subscription0x00000000f";
        ss.setId(anotherSubscriptionId);
        ss.setOrganizationId("another_organization");

        organizationDao.addSubscription(ss);

        organizationDao.removeSubscription(SUBSCRIPTION_ID);

        assertNull(subscriptionCollection.findOne(new BasicDBObject("id", SUBSCRIPTION_ID)));
        assertNotNull(subscriptionCollection.findOne(new BasicDBObject("id", anotherSubscriptionId)));
    }

    @Test
    public void shouldGetSubscriptionById() throws OrganizationException {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME).append("owner", ORGANIZATION_OWNER));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withId(SUBSCRIPTION_ID)
                                              .withOrganizationId(ORGANIZATION_ID)
                                              .withServiceId(SERVICE_NAME)
                                              .withStartDate(START_DATE)
                                              .withEndDate(END_DATE)
                                              .withProperties(PROPS);

        organizationDao.addSubscription(subscription);

        Subscription actual = organizationDao.getSubscriptionById(SUBSCRIPTION_ID);

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
