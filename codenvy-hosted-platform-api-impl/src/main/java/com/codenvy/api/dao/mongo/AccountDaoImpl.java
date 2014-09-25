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
import com.codenvy.api.account.server.dao.Billing;
import com.codenvy.api.account.server.dao.SubscriptionAttributes;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
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

import static java.lang.String.format;

/**
 * Implementation of {@link AccountDao} based on MongoDB storage.
 * <pre>
 *  Account members collection document schema:
 *
 * {
 *     "_id" : "userId...",
 *     "members" : [
 *          ...
 *          {
 *              "userId" : "userId...",
 *              "accountId" : "accountId...",
 *              "roles" : [
 *                  "role1...",
 *                  "role2..."
 *              ]
 *          }
 *          ...
 *     ]
 * }
 *
 * Account collection document schema:
 *
 * {
 *      "id" : "accountId...",
 *      "name" : "name...",
 *      "attributes" : [
 *          ...
 *          {
 *              "name" : "key...",
 *              "value" : "value..."
 *          }
 *          ...
 *      ]
 * }
 *
 *
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
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to create account");
        }
    }

    @Override
    public Account getById(String id) throws NotFoundException, ServerException {
        final DBObject accountDocument;
        try {
            accountDocument = accountCollection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve account");
        }
        if (accountDocument == null) {
            throw new NotFoundException(format("Account with id %s was not found", id));
        }
        return toAccount(accountDocument);
    }

    @Override
    public Account getByName(String name) throws NotFoundException, ServerException {
        final DBObject accountDocument;
        try {
            accountDocument = accountCollection.findOne(new BasicDBObject("name", name));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve account");
        }
        if (accountDocument == null) {
            throw new NotFoundException(format("Account with name %s was not found", name));
        }
        return toAccount(accountDocument);
    }

    @Override
    public List<Account> getByOwner(String owner) throws ServerException, NotFoundException {
        final List<Account> accounts = new LinkedList<>();
        try {
            final DBObject membersDocument = memberCollection.findOne(owner);
            if (membersDocument != null) {
                final BasicDBList members = (BasicDBList)membersDocument.get("members");
                for (Object memberObj : members) {
                    final Member member = toMember(memberObj);
                    if (member.getRoles().contains("account/owner")) {
                        accounts.add(getById(member.getAccountId()));
                    }
                }
            }
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve accounts");
        }
        return accounts;
    }

    @Override
    public void update(Account account) throws NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", account.getId());
        try {
            checkAccountExists(account.getId());
            accountCollection.update(query, toDBObject(account));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to update account");
        }
    }

    @Override
    public void remove(String id) throws ConflictException, NotFoundException, ServerException {
        //check account doesn't have associated workspaces
        if (!workspaceDao.getByAccount(id).isEmpty()) {
            throw new ConflictException("It is not possible to remove account having associated workspaces");
        }
        try {
            // Removing subscriptions
            try (DBCursor cursor = subscriptionCollection.find(new BasicDBObject("accountId", id))) {
                for (DBObject subscriptionDocument : cursor) {
                    final Subscription current = toSubscription(subscriptionDocument);
                    subscriptionCollection.remove(new BasicDBObject("id", current.getId()));
                    subscriptionAttributesCollection.remove(new BasicDBObject("_id", current.getId()));
                }
            }
            //Removing members
            for (Member member : getMembers(id)) {
                removeMember(member);
            }
            // Removing account itself
            accountCollection.remove(new BasicDBObject("id", id));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to remove account");
        }
    }

    @Override
    public List<Member> getMembers(String accountId) throws ServerException {
        final List<Member> result = new ArrayList<>();
        try (DBCursor membersCursor = memberCollection.find(new BasicDBObject("members.accountId", accountId))) {
            for (DBObject memberDocument : membersCursor) {
                final BasicDBList members = (BasicDBList)memberDocument.get("members");
                result.add(retrieveMember(accountId, members));
            }
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve account members");
        }
        return result;
    }

    @Override
    public List<Member> getByMember(String userId) throws NotFoundException, ServerException {
        final List<Member> result = new ArrayList<>();
        try {
            final DBObject membersDocument = memberCollection.findOne(userId);
            if (membersDocument != null) {
                final BasicDBList members = (BasicDBList)membersDocument.get("members");
                for (Object memberObj : members) {
                    result.add(toMember(memberObj));
                }
            }
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve members");
        }
        return result;
    }

    @Override
    public void addMember(Member member) throws NotFoundException, ConflictException, ServerException {
        try {
            checkAccountExists(member.getAccountId());
            final DBObject membersDocument = documentFor(member);
            final BasicDBList members = (BasicDBList)membersDocument.get("members");
            checkMemberIsAbsent(member, members);
            //member doesn't exist so we can create and save it
            members.add(toDBObject(member));
            memberCollection.save(membersDocument);
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to persist member");
        }
    }

    @Override
    public void removeMember(Member member) throws NotFoundException, ServerException, ConflictException {
        final DBObject query = new BasicDBObject("_id", member.getUserId());
        try {
            final DBObject membersDocument = memberCollection.findOne(query);
            if (membersDocument == null) {
                throw new NotFoundException(format("User %s doesn't have account memberships", member.getUserId()));
            }
            final BasicDBList members = (BasicDBList)membersDocument.get("members");
            //remove member from members list
            if (!remove(member.getAccountId(), members)) {
                throw new NotFoundException(format("Membership between %s and %s not found", member.getUserId(), member.getAccountId()));
            }
            //if user doesn't have memberships then remove document
            if (!members.isEmpty()) {
                memberCollection.update(query, membersDocument);
            } else {
                memberCollection.remove(query);
            }
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to remove member");
        }
    }

    @Override
    public List<Subscription> getSubscriptions(String accountId, String serviceId) throws ServerException, NotFoundException {
        final List<Subscription> result;
        try {
            if (null == accountCollection.findOne(new BasicDBObject("id", accountId))) {
                throw new NotFoundException("Account not found " + accountId);
            }
            final BasicDBObject query = new BasicDBObject("accountId", accountId);
            if (null != serviceId) {
                query.append("serviceId", serviceId);
            }
            try (DBCursor subscriptions = subscriptionCollection.find(query)) {
                if (subscriptions.size() == 0 && (null == serviceId || "Saas".equals(serviceId))) {
                    return getDefaultSubscriptions(accountId);
                } else {
                    result = new ArrayList<>(subscriptions.size());
                    for (DBObject currentSubscription : subscriptions) {
                        result.add(toSubscription(currentSubscription));
                    }
                }
            }
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve subscriptions");
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
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to update subscription");
        }
    }

    @Override
    public void addSubscription(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        try {
            ensureConsistency(subscription);
            checkAccountExists(subscription.getAccountId());
            subscriptionCollection.save(toDBObject(subscription));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to persist subscription");
        }
    }

    @Override
    public void removeSubscription(String subscriptionId) throws NotFoundException, ServerException {
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
    public Subscription getSubscriptionById(String subscriptionId) throws NotFoundException, ServerException {
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
    public List<Subscription> getSubscriptions() throws ServerException {
        try (DBCursor subscriptions = subscriptionCollection.find()) {
            final ArrayList<Subscription> result = new ArrayList<>(subscriptions.size());
            for (DBObject subscriptionObj : subscriptions) {
                result.add(toSubscription(subscriptionObj));
            }
            return result;
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve subscriptions");
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
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to persist subscription attributes");
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
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve subscription attributes");
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
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to remove subscription attributes");
        }
    }

    private boolean remove(String accountId, BasicDBList src) {
        boolean found = false;
        final Iterator it = src.iterator();
        while (!found && it.hasNext()) {
            final Member member = toMember(it.next());
            if (member.getAccountId().equals(accountId)) {
                found = true;
                it.remove();
            }
        }
        return found;
    }

    private Member retrieveMember(String accountId, BasicDBList src) {
        for (Object dbMember : src) {
            final Member member = toMember(dbMember);
            if (accountId.equals(member.getAccountId())) {
                return member;
            }
        }
        return null;
    }

    private DBObject documentFor(Member member) {
        DBObject membersDocument = memberCollection.findOne(new BasicDBObject("_id", member.getUserId()));
        if (membersDocument == null) {
            membersDocument = new BasicDBObject("_id", member.getUserId());
            membersDocument.put("members", new BasicDBList());
        }
        return membersDocument;
    }

    private void checkAccountExists(String id) throws NotFoundException {
        if (accountCollection.findOne(new BasicDBObject("id", id)) == null) {
            throw new NotFoundException(format("Account with id %s was not found", id));
        }
    }

    private void checkMemberIsAbsent(Member target, BasicDBList members) throws ConflictException {
        for (Object dbMember : members) {
            final Member member = toMember(dbMember);
            if (target.getAccountId().equals(member.getAccountId())) {
                throw new ConflictException(format("Account %s already contains member %s", target.getAccountId(), target.getUserId()));
            }
        }
    }

    private List<Subscription> getDefaultSubscriptions(String accountId) {
        try {
            if (!workspaceDao.getByAccount(accountId).isEmpty()) {
                return Collections.singletonList(new Subscription()
                                                         .withId("community" + accountId)
                                                         .withAccountId(accountId)
                                                         .withPlanId("sas-community")
                                                         .withServiceId("Saas")
                                                         .withProperties(Collections.singletonMap("Package", "Community"))
                                                );
            }
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
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
        final Billing billing = new Billing().withContractTerm(billingAttributes.getInt("contractTerm"))
                                             .withCycle(billingAttributes.getInt("cycle"))
                                             .withCycleType(billingAttributes.getInt("cycleType"))
                                             .withStartDate(billingAttributes.getString("startDate"))
                                             .withEndDate(billingAttributes.getString("endDate"))
                                             .withUsePaymentSystem(billingAttributes.getString("usePaymentSystem"));

        return new SubscriptionAttributes().withStartDate(attributes.getString("startDate"))
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
    DBObject toDBObject(Account account) {
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
