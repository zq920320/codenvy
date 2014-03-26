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

import com.codenvy.api.organization.server.dao.OrganizationDao;
import com.codenvy.api.organization.server.exception.OrganizationException;
import com.codenvy.api.organization.server.exception.OrganizationNotFoundException;
import com.codenvy.api.organization.shared.dto.Attribute;
import com.codenvy.api.organization.shared.dto.Member;
import com.codenvy.api.organization.shared.dto.Organization;
import com.codenvy.api.organization.shared.dto.Subscription;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of OrganizationDAO based on MongoDB storage.
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
public class OrganizationDaoImpl implements OrganizationDao {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationDaoImpl.class);
    /*
    *  Members collection schema:
    *  ------------------------------------------------------------------------------------
    * | _ID (UserId)   |              List of organization memberships                    |
    * ------------------------------------------------------------------------------------|
    * |     user1234   |  ["org1", List<Roles>], ["org2", List<Roles>], [...]             |
    *  -----------------------------------------------------------------------------------*/

    protected static final String ORGANIZATION_COLLECTION = "organization.storage.db.organization.collection";
    protected static final String SUBSCRIPTION_COLLECTION = "organization.storage.db.subscription.collection";
    protected static final String MEMBER_COLLECTION       = "organization.storage.db.org.member.collection";

    DBCollection organizationCollection;
    DBCollection subscriptionCollection;
    DBCollection memberCollection;

    @Inject
    public OrganizationDaoImpl(DB db, @Named(ORGANIZATION_COLLECTION) String organizationCollectionName,
                               @Named(SUBSCRIPTION_COLLECTION) String subscriptionCollectionName,
                               @Named(MEMBER_COLLECTION) String memberCollectionName) {
        organizationCollection = db.getCollection(organizationCollectionName);
        organizationCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        subscriptionCollection = db.getCollection(subscriptionCollectionName);
        subscriptionCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        memberCollection = db.getCollection(memberCollectionName);
    }

    @Override
    public void create(Organization organization) throws OrganizationException {
        try {
            organizationCollection.save(toDBObject(organization));
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
    }

    @Override
    public Organization getById(String id) throws OrganizationException {
        DBObject res;
        try {
            res = organizationCollection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
        return res != null ? DtoFactory.getInstance().createDtoFromJson(res.toString(), Organization.class) : null;
    }

    @Override
    public Organization getByName(String name) throws OrganizationException {
        DBObject res;
        try {
            res = organizationCollection.findOne(new BasicDBObject("name", name));
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
        return res != null ? DtoFactory.getInstance().createDtoFromJson(res.toString(), Organization.class) : null;
    }

    @Override
    public Organization getByOwner(String owner) throws OrganizationException {
        DBObject res;
        try {
            res = organizationCollection.findOne(new BasicDBObject("owner", owner));
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
        return res != null ? DtoFactory.getInstance().createDtoFromJson(res.toString(), Organization.class) : null;
    }

    @Override
    public void update(Organization organization) throws OrganizationException {
        DBObject query = new BasicDBObject("id", organization.getId());
        try {
            if (organizationCollection.findOne(query) == null) {
                throw OrganizationNotFoundException.doesNotExistWithId(organization.getId());
            }
            organizationCollection.update(query, toDBObject(organization));
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
    }

    @Override
    public void remove(String id) throws OrganizationException {
        try {
            // Removing subscriptions
            subscriptionCollection.remove(new BasicDBObject("organizationId", id));

            // Removing members
            try (DBCursor cursor = memberCollection.find()) {
                for (DBObject one : cursor) {
                    String userId = (String)one.get("_id");
                    removeMember(id, userId);
                }
            }
            // Removing organization itself
            organizationCollection.remove(new BasicDBObject("id", id));
        } catch (MongoException ae) {
            throw new OrganizationException(ae.getMessage(), ae);
        }
    }

    @Override
    public List<Member> getMembers(String orgId) throws OrganizationException {
        List<Member> result = new ArrayList<>();
        try (DBCursor cursor = memberCollection.find()) {
            for (DBObject one : cursor) {
                BasicDBList members = (BasicDBList)one.get("members");
                for (Object memberObj : members) {
                    Member member = DtoFactory.getInstance().createDtoFromJson(memberObj.toString(), Member.class);
                    if (orgId.equals(member.getOrganizationId())) {
                        result.add(member);
                    }
                }
            }
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public List<Organization> getByMember(String userId) throws OrganizationException {
        List<Organization> result = new ArrayList<>();
        try {
            DBObject line = memberCollection.findOne(userId);
            if (line != null) {
                BasicDBList members = (BasicDBList)line.get("members");
                for (Object memberObj : members) {
                    Member member = DtoFactory.getInstance().createDtoFromJson(memberObj.toString(), Member.class);
                    result.add(getById(member.getOrganizationId()));
                }
            }
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void addMember(Member member) throws OrganizationException {
        try {
            if (organizationCollection.findOne(new BasicDBObject("id", member.getOrganizationId())) == null) {
                throw OrganizationNotFoundException.doesNotExistWithId(member.getOrganizationId());
            }

            // Retrieving his membership list, or creating new one
            DBObject old = memberCollection.findOne(member.getUserId());
            if (old == null) {
                old = new BasicDBObject("_id", member.getUserId());
            }
            BasicDBList members = (BasicDBList)old.get("members");
            if (members == null)
                members = new BasicDBList();

            // Ensure such member not exists yet
            for (Object member1 : members) {
                Member one = DtoFactory.getInstance().createDtoFromJson(member1.toString(), Member.class);
                if (one.getUserId().equals(member.getUserId()) && one.getOrganizationId().equals(member.getOrganizationId()))
                    throw new OrganizationException(
                            String.format(
                                    "Membership of user %s in organization %s already exists. Use update method instead.",
                                    member.getUserId(), member.getOrganizationId()));
            }

            // Adding new membership
            members.add(toDBObject(member));
            old.put("members", members);

            //Save
            memberCollection.save(old);
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
    }

    @Override
    public void removeMember(String organizationId, String userId) throws OrganizationException {
        DBObject query = new BasicDBObject("_id", userId);
        try {
            DBObject old = memberCollection.findOne(query);
            if (old == null) {
                return;
            }
            BasicDBList members = (BasicDBList)old.get("members");
            Iterator it = members.iterator();
            while (it.hasNext()) {
                if (organizationId.equals(DtoFactory.getInstance().createDtoFromJson(it.next().toString(), Member.class)
                                                    .getOrganizationId()))
                    it.remove();
            }
            if (members.size() > 0) {
                old.put("members", members);
                memberCollection.update(query, old);
            } else {
                memberCollection.remove(query); // Removing user from table if no memberships anymore.
            }
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
    }

    @Override
    public List<Subscription> getSubscriptions(String organizationId) throws OrganizationException {
        List<Subscription> result = new ArrayList<>();
        try {
            DBObject old = organizationCollection.findOne(new BasicDBObject("id", organizationId));
            if (old != null) {
                DBCursor subscriptions = subscriptionCollection.find(new BasicDBObject("organizationId", organizationId));
                for (DBObject currentSubscription : subscriptions) {
                    result.add(DtoFactory.getInstance().createDtoFromJson(currentSubscription.toString(), Subscription.class));
                }
            }
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void addSubscription(Subscription subscription) throws OrganizationException {
        try {
            DBObject org = organizationCollection.findOne(new BasicDBObject("id", subscription.getOrganizationId()));
            if (org != null) {
                ensureDateConsistency(subscription);
                subscriptionCollection.save(toDBObject(subscription));
            } else {
                throw OrganizationNotFoundException.doesNotExistWithId(subscription.getOrganizationId());
            }
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
    }

    @Override
    public void removeSubscription(String subscriptionId) throws OrganizationException {
        DBObject toRemove = subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId));
        if (toRemove != null) {
            subscriptionCollection.remove(new BasicDBObject("id", subscriptionId));
        } else {
            LOG.warn(String.format("Subscription with id = %s, cant be removed cause it doesn't exist", subscriptionId));
        }
    }

    @Override
    public Subscription getSubscriptionById(String subscriptionId) throws OrganizationException {
        DBObject subscription;
        try {
            subscription = subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId));
        } catch (MongoException me) {
            throw new OrganizationException(me.getMessage(), me);
        }
        return subscription == null ? null : DtoFactory.getInstance().createDtoFromJson(subscription.toString(), Subscription.class);
    }

    /**
     * Convert organization to Database ready-to-use object,
     *
     * @param obj
     *         organization to convert
     * @return DBObject
     */
    private DBObject toDBObject(Organization obj) {
        List<Attribute> attributes = new ArrayList<>();
        if (obj.getAttributes() != null) {
            for (Attribute one : obj.getAttributes()) {
                attributes.add(DtoFactory.getInstance().createDto(Attribute.class)
                                         .withName(one.getName())
                                         .withValue(one.getValue())
                                         .withDescription(one.getDescription()));
            }
        }
        Organization organization = DtoFactory.getInstance().createDto(Organization.class)
                                              .withId(obj.getId())
                                              .withName(obj.getName())
                                              .withOwner(obj.getOwner())
                                              .withAttributes(attributes);
        return (DBObject)JSON.parse(organization.toString());
    }

    /**
     * Check that start date goes before end date
     *
     * @throws com.codenvy.api.organization.server.exception.OrganizationException
     *         when end date goes before start date
     */
    private void ensureDateConsistency(Subscription subscription) throws OrganizationException {
        if (subscription.getStartDate() >= subscription.getEndDate()) {
            throw new OrganizationException("Subscription startDate should be less than endDate");
        }
    }

    /**
     * Convert member to Database ready-to-use object,
     *
     * @param obj
     *         member to convert
     * @return DBObject
     */
    private DBObject toDBObject(Member obj) {
        Member member = DtoFactory.getInstance().createDto(Member.class)
                                  .withUserId(obj.getUserId())
                                  .withOrganizationId(obj.getOrganizationId())
                                  .withRoles(obj.getRoles());
        return (DBObject)JSON.parse(member.toString());
    }

    /**
     * Convert subscription to Database ready-to-use object,
     *
     * @param obj
     *         subscription to convert
     * @return DBObject
     */
    private DBObject toDBObject(Subscription obj) {
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withId(obj.getId())
                                              .withOrganizationId(obj.getOrganizationId())
                                              .withServiceId(obj.getServiceId())
                                              .withStartDate(obj.getStartDate())
                                              .withEndDate(obj.getEndDate())
                                              .withProperties(obj.getProperties());
        return (DBObject)JSON.parse(subscription.toString());
    }
}
