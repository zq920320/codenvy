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

import com.codenvy.api.organization.shared.dto.Organization;
import com.codenvy.api.organization.shared.dto.Attribute;
import com.codenvy.api.organization.shared.dto.Member;
import com.codenvy.api.organization.shared.dto.Subscription;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
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
 *
 * @author Max Shaposhnik
 */
public class OrganizationDaoTest extends BaseDaoTest {

    private static final String USER_ID = "user12837asjhda823981h";

    private static final String ORGANIZATION_ID    = "accc123abc456def";
    private static final String ORGANIZATION_NAME  = "acccount1";
    private static final String ORGANIZATION_OWNER = "user123@codenvy.com";

    private static final String ACC_COLL_NAME          = "organizations";
    private static final String SUBSCRIPTION_COLL_NAME = "subscriptions";
    private static final String MEMBER_COLL_NAME       = "members";

    private static final String SERVICE_NAME = "builder";
    private static final String START_DATE   = "2050-01-12";
    private static final String END_DATE     = "2080-05-17";


    OrganizationDaoImpl organizationDao;
    DBCollection        subscriptionCollection;
    DBCollection        membersCollection;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(ACC_COLL_NAME);
        organizationDao = new OrganizationDaoImpl(db, ACC_COLL_NAME, SUBSCRIPTION_COLL_NAME, MEMBER_COLL_NAME);
        subscriptionCollection = db.getCollection(SUBSCRIPTION_COLL_NAME);
        membersCollection = db.getCollection(MEMBER_COLL_NAME);
    }


    @Test
    public void shouldCreateOrganization() throws Exception {

        Organization organization = DtoFactory.getInstance().createDto(Organization.class).withId(ORGANIZATION_ID).withName(ORGANIZATION_NAME)
                                         .withOwner(ORGANIZATION_OWNER).withAttributes(getAttributes());

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
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                                  .append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getById(ORGANIZATION_ID);
        assertNotNull(result);
        assertEquals(result.getName(), ORGANIZATION_NAME);
        assertEquals(result.getOwner(), ORGANIZATION_OWNER);
    }

    @Test
    public void shouldFindOrganizationByName() throws Exception {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getByName(ORGANIZATION_NAME);
        assertNotNull(result);
        assertEquals(result.getId(), ORGANIZATION_ID);
        assertEquals(result.getOwner(), ORGANIZATION_OWNER);
    }

    @Test
    public void shouldNotFindUnexistingOrganizationByName() throws Exception {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getByName("randomName");
        assertNull(result);
    }

    @Test
    public void shouldFindOrganizationByOwner() throws Exception {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));
        Organization result = organizationDao.getByOwner(ORGANIZATION_OWNER);
        assertNotNull(result);
        assertEquals(result.getId(), ORGANIZATION_ID);
        assertEquals(result.getName(), ORGANIZATION_NAME);
    }


    @Test
    public void shouldUpdateOrganization() throws Exception {

        Organization organization = DtoFactory.getInstance().createDto(Organization.class).withId(ORGANIZATION_ID).withName(ORGANIZATION_NAME)
                                        .withAttributes(getAttributes()).withOwner(ORGANIZATION_OWNER);

        // Put first object
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));
        // main invoke
        organizationDao.update(organization);

        DBObject res = collection.findOne(new BasicDBObject("id", ORGANIZATION_ID));
        assertNotNull(res, "Specified user profile does not exists.");

        Organization result =
                DtoFactory.getInstance().createDtoFromJson(res.toString(), Organization.class);

        assertEquals(organization.getLinks(), result.getLinks());
        assertEquals(organization, result);
    }

    @Test
    public void shouldRemoveOrganization() throws Exception {
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));

        List<String> roles = Arrays.asList("organization/admin", "organization/developer");
        Member member1 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withOrganizationId(ORGANIZATION_ID)
                          .withRoles(roles.subList(0,1));
        organizationDao.addMember(member1);

        organizationDao.remove(ORGANIZATION_ID);
        assertNull(collection.findOne(new BasicDBObject("id", ORGANIZATION_ID)));
        assertNull(membersCollection.findOne(new BasicDBObject("_id", USER_ID)));
    }


    @Test
    public void  shouldAddMember() throws Exception {
        List<String> roles = Arrays.asList("organization/admin", "organization/developer");
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));
        Member member =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withOrganizationId(ORGANIZATION_ID)
                          .withRoles(roles);

        organizationDao.addMember(member);

        DBObject res = membersCollection.findOne( new BasicDBObject("_id", USER_ID));
        assertNotNull(res, "Specified user membership does not exists.");

        for (Object dbmembership : (BasicDBList)res.get("members")) {
            Member membership = DtoFactory.getInstance().createDtoFromJson(dbmembership.toString(), Member.class);
            assertEquals(membership.getOrganizationId(), ORGANIZATION_ID);
            assertEquals(roles, membership.getRoles());
        }

    }


    @Test
    public void shouldFindMembers() throws Exception {
        List<String> roles = Arrays.asList("organization/admin", "organization/developer");
        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));
        Member member1 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withOrganizationId(ORGANIZATION_ID)
                          .withRoles(roles.subList(0, 1));
        Member member2 =
                DtoFactory.getInstance().createDto(Member.class).withUserId("anotherUserId").withOrganizationId(ORGANIZATION_ID)
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
        Member member1 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withOrganizationId(ORGANIZATION_ID)
                          .withRoles(roles.subList(0,1));
        Member member2 =
                DtoFactory.getInstance().createDto(Member.class).withUserId("user2").withOrganizationId(ORGANIZATION_ID)
                          .withRoles(roles);

        organizationDao.addMember(member1);
        organizationDao.addMember(member2);

        organizationDao.removeMember(ORGANIZATION_ID, USER_ID);

        assertNull(membersCollection.findOne(new BasicDBObject("_id", USER_ID)));
        assertNotNull(membersCollection.findOne(new BasicDBObject("_id", "user2")));
    }



    @Test
    public void  shouldAddSubscription() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));

        Subscription ss =
                DtoFactory.getInstance().createDto(Subscription.class).withServiceId(SERVICE_NAME)
                .withStartDate(START_DATE).withEndDate(END_DATE).withProperties(properties);

        organizationDao.addSubscription(ss, ORGANIZATION_ID);

        DBObject res = subscriptionCollection.findOne( new BasicDBObject("_id", ORGANIZATION_ID));
        assertNotNull(res, "Specified subscription does not exists.");

        for (Object dbsubscriptions : (BasicDBList)res.get("subscriptions")) {
            Subscription subscription = DtoFactory.getInstance().createDtoFromJson(dbsubscriptions.toString(), Subscription.class);
            assertEquals(subscription.getServiceId(), SERVICE_NAME);
            assertEquals(subscription.getStartDate(), START_DATE);
            assertEquals(subscription.getEndDate(), END_DATE);
            assertEquals(subscription.getProperties(), properties);

        }
    }


    @Test
    public void shouldFindSubscriptions() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));

        Subscription ss1 =
                DtoFactory.getInstance().createDto(Subscription.class).withServiceId(SERVICE_NAME)
                          .withStartDate(START_DATE).withEndDate(END_DATE).withProperties(properties);
        Subscription ss2 =
                DtoFactory.getInstance().createDto(Subscription.class).withServiceId(SERVICE_NAME)
                          .withStartDate(START_DATE).withEndDate(END_DATE).withProperties(properties);

        organizationDao.addSubscription(ss1, ORGANIZATION_ID);
        organizationDao.addSubscription(ss2, ORGANIZATION_ID);

        List<Subscription> found = organizationDao.getSubscriptions(ORGANIZATION_ID);
        assertEquals(found.size(), 2);
    }

    @Test
    public void shouldRemoveSubscriptions() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        collection.insert(new BasicDBObject("id", ORGANIZATION_ID).append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));
        collection.insert(new BasicDBObject("id", "another_organization").append("name", ORGANIZATION_NAME)
                                                              .append("owner", ORGANIZATION_OWNER));

        Subscription ss =
                DtoFactory.getInstance().createDto(Subscription.class).withServiceId(SERVICE_NAME)
                          .withStartDate(START_DATE).withEndDate(END_DATE).withProperties(properties);

        organizationDao.addSubscription(ss, ORGANIZATION_ID);
        organizationDao.addSubscription(ss, "another_organization");

        organizationDao.removeSubscription(ORGANIZATION_ID, SERVICE_NAME);

        assertNull(subscriptionCollection.findOne(new BasicDBObject("_id", ORGANIZATION_ID)));
        assertNotNull(subscriptionCollection.findOne(new BasicDBObject("_id", "another_organization")));
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
