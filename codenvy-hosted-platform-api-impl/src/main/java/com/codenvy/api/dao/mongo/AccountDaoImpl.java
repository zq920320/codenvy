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
import com.codenvy.api.account.server.dao.SubscriptionHistoryEvent;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.google.gson.Gson;
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
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link com.codenvy.api.account.server.dao.AccountDao} based on MongoDB storage.
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class AccountDaoImpl implements AccountDao {

    private static final Logger LOG = LoggerFactory.getLogger(AccountDaoImpl.class);
    /*
    *  Members collection schema:
    *  ------------------------------------------------------------------------------------
    * | _ID (UserId)   |              List of account memberships                    |
    * ------------------------------------------------------------------------------------|
    * |     user1234   |  ["acc1", List<Roles>], ["acc2", List<Roles>], [...]             |
    *  -----------------------------------------------------------------------------------*/

    protected static final String ACCOUNT_COLLECTION              = "organization.storage.db.account.collection";
    protected static final String SUBSCRIPTION_COLLECTION         = "organization.storage.db.subscription.collection";
    protected static final String MEMBER_COLLECTION               = "organization.storage.db.acc.member.collection";
    protected static final String SUBSCRIPTION_HISTORY_COLLECTION =
            "organization.storage.db.subscription.history.collection";

    private final Gson         gson;
    private       DBCollection accountCollection;
    private       DBCollection subscriptionCollection;
    private       DBCollection memberCollection;
    private       DBCollection subscriptionHistoryCollection;

    private WorkspaceDao workspaceDao;

    @Inject
    public AccountDaoImpl(Gson gson, DB db,
                          WorkspaceDao workspaceDao,
                          @Named(ACCOUNT_COLLECTION) String accountCollectionName,
                          @Named(SUBSCRIPTION_COLLECTION) String subscriptionCollectionName,
                          @Named(MEMBER_COLLECTION) String memberCollectionName,
                          @Named(SUBSCRIPTION_HISTORY_COLLECTION) String subscriptionHistoryCollectionName) {
        accountCollection = db.getCollection(accountCollectionName);
        accountCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        accountCollection.ensureIndex(new BasicDBObject("name", 1));
        subscriptionCollection = db.getCollection(subscriptionCollectionName);
        subscriptionCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        subscriptionCollection.ensureIndex(new BasicDBObject("accountId", 1));
        memberCollection = db.getCollection(memberCollectionName);
        memberCollection.ensureIndex(new BasicDBObject("members.accountId", 1));
        subscriptionHistoryCollection = db.getCollection(subscriptionHistoryCollectionName);
        subscriptionHistoryCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        subscriptionHistoryCollection.ensureIndex(new BasicDBObject("userId", 1));
        subscriptionHistoryCollection.ensureIndex(new BasicDBObject("type", 1));
        subscriptionHistoryCollection.ensureIndex(new BasicDBObject("subscription.accountId", 1));
        subscriptionHistoryCollection.ensureIndex(new BasicDBObject("subscription.serviceId", 1));
        subscriptionHistoryCollection.ensureIndex(new BasicDBObject("subscription.properties.codenvy:trial", 1));
        this.workspaceDao = workspaceDao;
        this.gson = gson;
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
        DBObject res;
        try {
            res = accountCollection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null) {
            throw new NotFoundException("Account not found " + id);
        }
        return fromDBObject(res, Account.class);
    }

    @Override
    public Account getByName(String name) throws NotFoundException, ServerException {
        DBObject res;
        try {
            res = accountCollection.findOne(new BasicDBObject("name", name));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null) {
            throw new NotFoundException("Account not found " + name);
        }
        return fromDBObject(res, Account.class);
    }

    @Override
    public List<Account> getByOwner(String owner) throws ServerException, NotFoundException {
        final List<Account> accounts = new ArrayList<>();
        try {
            DBObject line = memberCollection.findOne(owner);
            if (line != null) {
                BasicDBList members = (BasicDBList)line.get("members");
                for (Object memberObj : members) {
                    Member member = fromDBObject( (DBObject)memberObj, Member.class);
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
        DBObject query = new BasicDBObject("id", account.getId());
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
                throw new NotFoundException(String.format("Account %s doesn't exist", id));
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
        List<Member> result = new ArrayList<>();
        try (DBCursor cursor = memberCollection.find(new BasicDBObject("members.accountId", accountId))) {
            for (DBObject one : cursor) {
                BasicDBList members = (BasicDBList)one.get("members");
                for (Object memberObj : members) {
                    Member member = fromDBObject( (DBObject)memberObj, Member.class);
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
        List<Member> result = new ArrayList<>();
        try {
            DBObject line = memberCollection.findOne(userId);
            if (line != null) {
                BasicDBList members = (BasicDBList)line.get("members");
                for (Object memberObj : members) {
                    Member member = fromDBObject( (DBObject)memberObj, Member.class);
                    result.add(member);
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
                Member one = fromDBObject((DBObject)member1, Member.class);
                if (one.getUserId().equals(member.getUserId()) && one.getAccountId().equals(member.getAccountId()))
                    throw new ConflictException(
                            String.format(
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
            BasicDBList members = (BasicDBList)old.get("members");
            //search for needed membership
            Iterator it = members.iterator();
            Member toRemove = null;
            while (it.hasNext() && toRemove == null) {
                toRemove = fromDBObject((DBObject)it.next(), Member.class);
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
        List<Subscription> result = new ArrayList<>();
        try {
            if (null == accountCollection.findOne(new BasicDBObject("id", accountId))) {
                throw new NotFoundException("Account not found " + accountId);
            }
            try (DBCursor subscriptions = subscriptionCollection.find(new BasicDBObject("accountId", accountId))) {
                for (DBObject currentSubscription : subscriptions) {
                    result.add(fromDBObject(currentSubscription, Subscription.class));
                }
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void updateSubscription(Subscription subscription) throws NotFoundException, ServerException {
        DBObject query = new BasicDBObject("id", subscription.getId());
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
            if (null == accountCollection.findOne(new BasicDBObject("id", subscription.getAccountId()))) {
                throw new NotFoundException("Account not found " + subscription.getAccountId());
            }
            ensureConsistency(subscription);
            subscriptionCollection.save(toDBObject(subscription));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void removeSubscription(String subscriptionId) throws NotFoundException, ServerException {
        try {
            if (null == subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId))) {
                LOG.warn(String.format("Subscription with id = %s, cant be removed cause it doesn't exist", subscriptionId));
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
            DBObject subscription = subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId));
            if (null == subscription) {
                throw new NotFoundException("Subscription not found " + subscriptionId);
            }
            return fromDBObject(subscription, Subscription.class);
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void addSubscriptionHistoryEvent(SubscriptionHistoryEvent historyEvent) throws ServerException, ConflictException {
        try {
            ensureConsistency(historyEvent);
            subscriptionHistoryCollection.save(toDBObject(historyEvent));
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public List<SubscriptionHistoryEvent> getSubscriptionHistoryEvents(SubscriptionHistoryEvent searchEvent) throws ServerException {
        DBObject query = getSearchQueryForHistoryEvent(searchEvent);
        List<SubscriptionHistoryEvent> result = new ArrayList<>();
        try (DBCursor events = subscriptionHistoryCollection.find(query)) {
            for (DBObject event : events) {
                result.add(fromDBObject(event, SubscriptionHistoryEvent.class));
            }
            return result;
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public List<Subscription> getSubscriptions() throws ServerException {
        try (DBCursor subscriptions = subscriptionCollection.find()) {
            ArrayList<Subscription> result = new ArrayList<>(subscriptions.size());
            for (DBObject currentSubscription : subscriptions) {
                result.add(fromDBObject(currentSubscription, Subscription.class));
            }
            return result;
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    private DBObject getSearchQueryForHistoryEvent(SubscriptionHistoryEvent searchEvent) {
        final BasicDBObject query = new BasicDBObject();
        if (searchEvent.getId() != null) {
            query.put("id", searchEvent.getId());
        }
        if (searchEvent.getUserId() != null) {
            query.put("userId", searchEvent.getUserId());
        }
        if (searchEvent.getType() != null) {
            query.put("type", searchEvent.getType().toString());
        }
        if (searchEvent.getTransactionId() != null) {
            query.put("transactionId", searchEvent.getTransactionId());
        }
        if (searchEvent.getSubscription() != null) {
            String subscriptionPrefix = "subscription.";
            Subscription subscription = searchEvent.getSubscription();
            if (subscription.getId() != null) {
                query.put(subscriptionPrefix + "id", subscription.getId());
            }
            if (subscription.getAccountId() != null) {
                query.put(subscriptionPrefix + "accountId", subscription.getAccountId());
            }
            if (subscription.getServiceId() != null) {
                query.put(subscriptionPrefix + "serviceId", subscription.getServiceId());
            }
            if (subscription.getState() != null) {
                query.put(subscriptionPrefix + "state", subscription.getState().toString());
            }
            final Map<String, String> properties = subscription.getProperties();
            if (properties != null && !properties.isEmpty()) {
                String propertiesPrefix = "properties.";
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    query.put(subscriptionPrefix + propertiesPrefix + entry.getKey(), entry.getValue());
                }
            }
        }

        return query;
    }

    /**
     * Check that subscription object has legal state
     *
     * @throws com.codenvy.api.core.ConflictException
     *         when end date goes before start date or subscription state is not set
     */
    private void ensureConsistency(Subscription subscription) throws ConflictException {
        if (subscription.getStartDate() >= subscription.getEndDate()) {
            throw new ConflictException("Subscription startDate should go before endDate");
        }
        if (null == subscription.getState()) {
            throw new ConflictException("Subscription state is missing");
        }
    }

    /**
     * Check that subscription history event object has legal state
     *
     * @throws com.codenvy.api.core.ConflictException
     *         when end date goes before start date
     */
    private void ensureConsistency(SubscriptionHistoryEvent historyEvent) throws ConflictException {
        if (null == historyEvent.getType()) {
            throw new ConflictException("SubscriptionHistoryEvent type is missing");
        }
        if (null == historyEvent.getId()) {
            throw new ConflictException("SubscriptionHistoryEvent id is missing");
        }
        if (historyEvent.getTime() == 0) {
            throw new ConflictException("SubscriptionHistoryEvent time can't be 0");
        }
        if (null == historyEvent.getUserId()) {
            throw new ConflictException("SubscriptionHistoryEvent userId is missing");
        }
    }

    /**
     * Convert account to Database ready-to-use object,
     *
     * @param obj
     *         account to convert
     * @return DBObject
     */
    private DBObject toDBObject(Account obj) {
        return (DBObject)JSON.parse(gson.toJson(obj));
    }

    /**
     * Convert member to Database ready-to-use object,
     *
     * @param obj
     *         member to convert
     * @return DBObject
     */
    private DBObject toDBObject(Object obj) {
        return (DBObject)JSON.parse(gson.toJson(obj));
    }

//    /**
//     * Convert subscription to Database ready-to-use object,
//     *
//     * @param obj
//     *         subscription to convert
//     * @return DBObject
//     */
//    private DBObject toDBObject(Subscription obj) {
//        return (DBObject)JSON.parse(gson.toJson(obj));
//    }
//
//    private DBObject toDBObject(SubscriptionHistoryEvent obj) {
//        return (DBObject)JSON.parse(gson.toJson(obj));
//    }

    //should be package-private (used in tests)
    protected <T> T fromDBObject(DBObject wsObj, Class<T> classOfT) {
        return gson.fromJson(wsObj.toString(), classOfT);
    }
}
