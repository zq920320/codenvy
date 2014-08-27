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
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Member;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.Billing;
import com.codenvy.api.account.shared.dto.SubscriptionAttributes;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link com.codenvy.api.account.server.dao.AccountDao} based on MongoDB storage.
 * <pre>
 *  Members collection schema:
 *  -----------------------------------------------------------------------------------
 * | _ID (UserId)   |              List of account memberships                         |
 * ------------------------------------------------------------------------------------|
 * |     user1234   |  ["acc1", List of roles], ["acc2", List of roles], [...]         |
 *  -----------------------------------------------------------------------------------
 * </pre>
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class AccountDaoImpl implements AccountDao {

    private static final Logger LOG                                = LoggerFactory.getLogger(AccountDaoImpl.class);
    private static final String ACCOUNT_COLLECTION                 = "organization.storage.db.account.collection";
    private static final String SUBSCRIPTION_COLLECTION            = "organization.storage.db.subscription.collection";
    private static final String SUBSCRIPTION_ATTRIBUTES_COLLECTION =
            "organization.storage.db.subscription.attributes.collection";
    private static final String MEMBER_COLLECTION                  = "organization.storage.db.acc.member.collection";

    private final DBCollection accountCollection;
    private final DBCollection subscriptionCollection;
    private final DBCollection memberCollection;
    private final DBCollection subscriptionAttributesCollection;
    private final WorkspaceDao workspaceDao;

    @Inject
    public AccountDaoImpl(DB db,
                          WorkspaceDao workspaceDao,
                          @Named(ACCOUNT_COLLECTION) String accountCollectionName,
                          @Named(SUBSCRIPTION_COLLECTION) String subscriptionCollectionName,
                          @Named(MEMBER_COLLECTION) String memberCollectionName,
                          @Named(SUBSCRIPTION_ATTRIBUTES_COLLECTION) String subscriptionAttributesCollectionName) {
        accountCollection = db.getCollection(accountCollectionName);
        accountCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        accountCollection.ensureIndex(new BasicDBObject("name", 1));
        subscriptionCollection = db.getCollection(subscriptionCollectionName);
        subscriptionCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        subscriptionCollection.ensureIndex(new BasicDBObject("accountId", 1));
        subscriptionAttributesCollection = db.getCollection(subscriptionAttributesCollectionName);
        memberCollection = db.getCollection(memberCollectionName);
        memberCollection.ensureIndex(new BasicDBObject("members.accountId", 1));
        this.workspaceDao = workspaceDao;
    }

    @Override
    public void create(Account account) throws ConflictException, ServerException {
        try {
            accountCollection.save(toDBObject(account));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public Account getById(String id) throws NotFoundException, ServerException {
        final DBObject res;
        try {
            res = accountCollection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null) {
            throw new NotFoundException("Account not found " + id);
        }
        return toAccount(res);
    }

    @Override
    public Account getByName(String name) throws NotFoundException, ServerException {
        final DBObject res;
        try {
            res = accountCollection.findOne(new BasicDBObject("name", name));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null) {
            throw new NotFoundException("Account not found " + name);
        }
        return toAccount(res);
    }

    @Override
    public List<Account> getByOwner(String owner) throws ServerException, NotFoundException {
        final List<Account> accounts = new LinkedList<>();
        try {
            final DBObject line = memberCollection.findOne(owner);
            if (line != null) {
                final BasicDBList members = (BasicDBList)line.get("members");
                for (Object memberObj : members) {
                    final Member member = toMember(memberObj);
                    if (member.getRoles().contains("account/owner")) {
                        accounts.add(getById(member.getAccountId()));
                    }
                }
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return accounts;
    }

    @Override
    public void update(Account account) throws NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", account.getId());
        try {
            if (accountCollection.findOne(query) == null) {
                throw new NotFoundException("Account not found " + account.getId());
            }
            accountCollection.update(query, toDBObject(account));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void remove(String id) throws ConflictException, NotFoundException, ServerException {
        try {
            if (accountCollection.findOne(new BasicDBObject("id", id)) == null) {
                throw new NotFoundException("Account not found " + id);
            }
            //check account hasn't associated workspaces
            if (workspaceDao.getByAccount(id).size() > 0) {
                throw new ConflictException("It is not possible to remove account that has associated workspaces");
            }
            // Removing subscriptions
            subscriptionCollection.remove(new BasicDBObject("accountId", id));
            //Removing members
            for (Member member : getMembers(id)) {
                removeMember(member);
            }
            // Removing account itself
            accountCollection.remove(new BasicDBObject("id", id));
        } catch (MongoException ex) {
            throw new ServerException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<Member> getMembers(String accountId) throws ServerException {
        final List<Member> result = new ArrayList<>();
        try (DBCursor cursor = memberCollection.find(new BasicDBObject("members.accountId", accountId))) {
            for (DBObject one : cursor) {
                final BasicDBList members = (BasicDBList)one.get("members");
                for (Object memberObj : members) {
                    final Member member = toMember(memberObj);
                    if (accountId.equals(member.getAccountId())) {
                        result.add(member);
                    }
                }
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public List<Member> getByMember(String userId) throws NotFoundException, ServerException {
        final List<Member> result = new ArrayList<>();
        try {
            final DBObject line = memberCollection.findOne(userId);
            if (line != null) {
                final BasicDBList members = (BasicDBList)line.get("members");
                for (Object memberObj : members) {
                    result.add(toMember(memberObj));
                }
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void addMember(Member member) throws NotFoundException, ConflictException, ServerException {
        try {
            if (accountCollection.findOne(new BasicDBObject("id", member.getAccountId())) == null) {
                throw new NotFoundException("Account not found " + member.getAccountId());
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
                final Member one = toMember(member1);
                if (one.getUserId().equals(member.getUserId()) && one.getAccountId().equals(member.getAccountId()))
                    throw new ConflictException(String.format(
                            "Membership of user %s in account %s already exists. Use update method instead.",
                            member.getUserId(), member.getAccountId())
                    );
            }
            // Adding new membership
            members.add(toDBObject(member));
            old.put("members", members);
            //Save
            memberCollection.save(old);
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void removeMember(Member member) throws NotFoundException, ServerException, ConflictException {
        //each account should have at least one owner
        DBObject query = new BasicDBObject("_id", member.getUserId());
        try {
            DBObject old = memberCollection.findOne(query);
            if (old == null) {
                throw new NotFoundException(String.format("User with id %s hasn't any account membership", member.getUserId()));
            }
            //check account exists
            if (accountCollection.findOne(new BasicDBObject("id", member.getAccountId())) == null) {
                throw new NotFoundException(String.format("Account with id %s doesn't exist", member.getAccountId()));
            }
            final BasicDBList members = (BasicDBList)old.get("members");
            //search for needed membership
            final Iterator it = members.iterator();
            Member toRemove = null;
            while (it.hasNext() && toRemove == null) {
                toRemove = toMember(it.next());
            }
            if (toRemove != null) {
                it.remove();
            } else {
                throw new NotFoundException(
                        String.format("Account %s doesn't have user %s as member", member.getAccountId(), member.getUserId()));
            }
            if (members.size() > 0) {
                old.put("members", members);
                memberCollection.update(query, old);
            } else {
                memberCollection.remove(query); // Removing user from table if no memberships anymore.
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public List<Subscription> getSubscriptions(String accountId) throws ServerException, NotFoundException {
        final List<Subscription> result;
        try {
            if (null == accountCollection.findOne(new BasicDBObject("id", accountId))) {
                throw new NotFoundException("Account not found " + accountId);
            }
            try (DBCursor subscriptions = subscriptionCollection.find(new BasicDBObject("accountId", accountId))) {
                result = new ArrayList<>(subscriptions.size());
                for (DBObject currentSubscription : subscriptions) {
                    result.add(toSubscription(currentSubscription));
                }
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void updateSubscription(Subscription subscription) throws NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", subscription.getId());
        try {
            if (null == subscriptionCollection.findOne(query)) {
                throw new NotFoundException("Subscription not found " + subscription.getId());
            }
            subscriptionCollection.update(query, toDBObject(subscription));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void addSubscription(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        try {
            ensureConsistency(subscription);
            if (null == accountCollection.findOne(new BasicDBObject("id", subscription.getAccountId()))) {
                throw new NotFoundException("Account not found " + subscription.getAccountId());
            }
            subscriptionCollection.save(toDBObject(subscription));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void removeSubscription(String subscriptionId) throws NotFoundException, ServerException {
        try {
            if (null == subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId))) {
                LOG.warn(String.format("Subscription with id = %s, cant be removed because it doesn't exist", subscriptionId));
                throw new NotFoundException("Subscription not found " + subscriptionId);
            }
            subscriptionCollection.remove(new BasicDBObject("id", subscriptionId));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public Subscription getSubscriptionById(String subscriptionId) throws NotFoundException, ServerException {
        try {
            final DBObject subscriptionObj = subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId));
            if (null == subscriptionObj) {
                throw new NotFoundException("Subscription not found " + subscriptionId);
            }
            return toSubscription(subscriptionObj);
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public List<Subscription> getSubscriptions() throws ServerException {
        try (DBCursor subscriptions = subscriptionCollection.find()) {
            final ArrayList<Subscription> result = new ArrayList<>(subscriptions.size());
            for (DBObject subscriptionObj : subscriptions) {
                result.add(toSubscription(subscriptionObj));
            }
            return result;
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void saveSubscriptionAttributes(String subscriptionId, SubscriptionAttributes subscriptionAttributes)
            throws ServerException, NotFoundException, ForbiddenException {
        if (null == subscriptionAttributes) {
            throw new ForbiddenException("Subscription attributes required");
        }
        try {
            if (null == subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId))) {
                throw new NotFoundException("Subscription not found " + subscriptionId);
            }
            subscriptionAttributesCollection.save(toDBObject(subscriptionId, subscriptionAttributes));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public SubscriptionAttributes getSubscriptionAttributes(String subscriptionId) throws ServerException, NotFoundException {
        try {
            final DBObject subscriptionAttributesObject =
                    subscriptionAttributesCollection.findOne(new BasicDBObject("_id", subscriptionId));
            if (null == subscriptionAttributesObject) {
                throw new NotFoundException("Attributes of subscription " + subscriptionId + " not found");
            }
            return toSubscriptionAttributes(subscriptionAttributesObject);
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }

    }

    @Override
    public void removeSubscriptionAttributes(String subscriptionId) throws ServerException, NotFoundException {
        try {
            if (null == subscriptionAttributesCollection.findOne(new BasicDBObject("_id", subscriptionId))) {
                throw new NotFoundException("Attributes of subscription " + subscriptionId + " not found");
            }
            subscriptionAttributesCollection.remove(new BasicDBObject("_id", subscriptionId));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    /**
     * Check that subscription object has legal state
     *
     * @throws com.codenvy.api.core.ConflictException
     *         when end date goes before start date or subscription state is not set
     */
    private void ensureConsistency(Subscription subscription) throws ConflictException {
        if (subscription.getPlanId() == null) {
            throw new ConflictException("Plan id is missing");
        }
        if (subscription.getServiceId() == null) {
            throw new ConflictException("Plan service id is missing");
        }
        if (subscription.getAccountId() == null) {
            throw new ConflictException("Plan account id is missing");
        }
        if (subscription.getId() == null) {
            throw new ConflictException("Subscription id is missing");
        }
    }

    /**
     * Converts member to database ready-to-use object
     */
    DBObject toDBObject(Member member) {
        final BasicDBList dbRoles = new BasicDBList();
        dbRoles.addAll(member.getRoles());
        return new BasicDBObject().append("userId", member.getUserId())
                                  .append("accountId", member.getAccountId())
                                  .append("roles", dbRoles);
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
                                  .append("properties", properties);
    }

    DBObject toDBObject(String subscriptionId, SubscriptionAttributes subscriptionAttributes) {
        final Billing billing = subscriptionAttributes.getBilling();
        final BasicDBObject billingObject = new BasicDBObject().append("contractTerm", billing.getContractTerm())
                                                               .append("startDate", billing.getStartDate())
                                                               .append("endDate", billing.getEndDate())
                                                               .append("cycle", billing.getCycle())
                                                               .append("cycleType", billing.getCycleType())
                                                               .append("paymentToken", billing.getPaymentToken())
                                                               .append("usePaymentSystem", billing.getUsePaymentSystem());
        return new BasicDBObject().append("_id", subscriptionId)
                                  .append("description", subscriptionAttributes.getDescription())
                                  .append("startDate", subscriptionAttributes.getStartDate())
                                  .append("endDate", subscriptionAttributes.getEndDate())
                                  .append("trialDuration", subscriptionAttributes.getTrialDuration())
                                  .append("custom", toDBList(subscriptionAttributes.getCustom()))
                                  .append("billing", billingObject);
    }

    SubscriptionAttributes toSubscriptionAttributes(DBObject dbObject) {
        final BasicDBObject attributes = (BasicDBObject)dbObject;
        final BasicDBObject billingAttributes = (BasicDBObject)attributes.get("billing");
        final Billing billing = DtoFactory.getInstance().createDto(Billing.class)
                                          .withContractTerm(billingAttributes.getInt("contractTerm"))
                                          .withCycle(billingAttributes.getInt("cycle"))
                                          .withCycleType(billingAttributes.getInt("cycleType"))
                                          .withStartDate(billingAttributes.getString("startDate"))
                                          .withEndDate(billingAttributes.getString("endDate"))
                                          .withPaymentToken(billingAttributes.getString("paymentToken"))
                                          .withUsePaymentSystem(billingAttributes.getString("usePaymentSystem"));

        return DtoFactory.getInstance().createDto(SubscriptionAttributes.class)
                         .withStartDate(attributes.getString("startDate"))
                         .withEndDate(attributes.getString("endDate"))
                         .withDescription(attributes.getString("description"))
                         .withTrialDuration(attributes.getInt("trialDuration"))
                         .withCustom(toMap((BasicDBList)attributes.get("custom")))
                         .withBilling(billing);
    }

    /**
     * Converts database object to account ready-to-use object
     */
    Account toAccount(Object dbObject) {
        final BasicDBObject accountObject = (BasicDBObject)dbObject;
        return new Account().withId(accountObject.getString("id"))
                            .withName(accountObject.getString("name"))
                            .withAttributes(toMap((BasicDBList)accountObject.get("attributes")));
    }

    /**
     * Converts database object to subscription ready-to-use object
     */
    Subscription toSubscription(Object dbObject) {
        final BasicDBObject basicSubscriptionObj = (BasicDBObject)dbObject;
        @SuppressWarnings("unchecked") //properties is always Map of Strings
        final Map<String, String> properties = (Map<String, String>)basicSubscriptionObj.get("properties");
        return new Subscription().withId(basicSubscriptionObj.getString("id"))
                                 .withAccountId(basicSubscriptionObj.getString("accountId"))
                                 .withServiceId(basicSubscriptionObj.getString("serviceId"))
                                 .withPlanId(basicSubscriptionObj.getString("planId"))
                                 .withProperties(properties);
    }

    /**
     * Converts database object to member read-to-use object
     */
    Member toMember(Object object) {
        final BasicDBObject basicMemberObj = (BasicDBObject)object;
        final BasicDBList basicRoles = (BasicDBList)basicMemberObj.get("roles");
        final List<String> roles = new ArrayList<>(basicRoles.size());
        for (Object role : basicRoles) {
            roles.add(role.toString());
        }
        return new Member().withAccountId(basicMemberObj.getString("accountId"))
                           .withUserId(basicMemberObj.getString("userId"))
                           .withRoles(roles);
    }

    /**
     * Converts account to database ready-to-use object
     */
    private DBObject toDBObject(Account account) {
        return new BasicDBObject().append("id", account.getId())
                                  .append("name", account.getName())
                                  .append("attributes", toDBList(account.getAttributes()));
    }

    /**
     * Converts database list to Map
     */
    private Map<String, String> toMap(BasicDBList list) {
        final Map<String, String> attributes = new HashMap<>();
        if (list != null) {
            for (Object obj : list) {
                final BasicDBObject attribute = (BasicDBObject)obj;
                attributes.put(attribute.getString("name"), attribute.getString("value"));
            }
        }
        return attributes;
    }

    /**
     * Converts Map to Database list
     */
    private BasicDBList toDBList(Map<String, String> attributes) {
        if (null == attributes) {
            attributes = Collections.emptyMap();
        }
        final BasicDBList list = new BasicDBList();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            list.add(new BasicDBObject().append("name", entry.getKey())
                                        .append("value", entry.getValue()));
        }
        return list;
    }
}
