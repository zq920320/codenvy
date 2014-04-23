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

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.Account;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.account.shared.dto.Attribute;
import com.codenvy.api.account.shared.dto.Member;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.*;
import com.mongodb.util.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link com.codenvy.api.account.server.dao.AccountDao} based on MongoDB storage.
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
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

    protected static final String ACCOUNT_COLLECTION      = "organization.storage.db.account.collection";
    protected static final String SUBSCRIPTION_COLLECTION = "organization.storage.db.subscription.collection";
    protected static final String MEMBER_COLLECTION       = "organization.storage.db.acc.member.collection";

    DBCollection accountCollection;
    DBCollection subscriptionCollection;
    DBCollection memberCollection;

    WorkspaceDao workspaceDao;

    @Inject
    public AccountDaoImpl(DB db,
                          WorkspaceDao workspaceDao,
                          @Named(ACCOUNT_COLLECTION) String accountCollectionName,
                          @Named(SUBSCRIPTION_COLLECTION) String subscriptionCollectionName,
                          @Named(MEMBER_COLLECTION) String memberCollectionName) {
        accountCollection = db.getCollection(accountCollectionName);
        accountCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        subscriptionCollection = db.getCollection(subscriptionCollectionName);
        subscriptionCollection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        memberCollection = db.getCollection(memberCollectionName);
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
        DBObject res;
        try {
            res = accountCollection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (res == null) {
            throw new NotFoundException("Account not found " + id);
        }
        return DtoFactory.getInstance().createDtoFromJson(res.toString(), Account.class);
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
        return DtoFactory.getInstance().createDtoFromJson(res.toString(), Account.class);
    }

    @Override
    public List<Account> getByOwner(String owner) throws ServerException, NotFoundException {
        final List<Account> accounts = new ArrayList<>();
        try {
            DBObject line = memberCollection.findOne(owner);
            if (line != null) {
                BasicDBList members = (BasicDBList)line.get("members");
                for (Object memberObj : members) {
                    Member member = DtoFactory.getInstance().createDtoFromJson(memberObj.toString(), Member.class);
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
            //check account hasn't associated workspaces
            if (workspaceDao.getByAccount(id).size() > 0) {
                throw new ConflictException("It is not possible to remove account that has associated workspaces");
            }
            // Removing subscriptions
            subscriptionCollection.remove(new BasicDBObject("accountId", id));

            // Removing members
            try (DBCursor cursor = memberCollection.find()) {
                for (DBObject one : cursor) {
                    String userId = (String)one.get("_id");
                    removeMember(id, userId);
                }
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
        try (DBCursor cursor = memberCollection.find()) {
            for (DBObject one : cursor) {
                BasicDBList members = (BasicDBList)one.get("members");
                for (Object memberObj : members) {
                    Member member = DtoFactory.getInstance().createDtoFromJson(memberObj.toString(), Member.class);
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
    public List<AccountMembership> getByMember(String userId) throws NotFoundException, ServerException {
        List<AccountMembership> result = new ArrayList<>();
        try {
            DBObject line = memberCollection.findOne(userId);
            if (line != null) {
                BasicDBList members = (BasicDBList)line.get("members");
                for (Object memberObj : members) {
                    Member member = DtoFactory.getInstance().createDtoFromJson(memberObj.toString(), Member.class);
                    Account account = getById(member.getAccountId());
                    AccountMembership am = DtoFactory.getInstance().createDto(AccountMembership.class);
                    am.setId(account.getId());
                    am.setName(account.getName());
                    am.setAttributes(account.getAttributes());
                    am.setRoles(member.getRoles());
                    result.add(am);
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
                Member one = DtoFactory.getInstance().createDtoFromJson(member1.toString(), Member.class);
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
    public void removeMember(String accountId, String userId) throws NotFoundException, ServerException {
        DBObject query = new BasicDBObject("_id", userId);
        try {
            DBObject old = memberCollection.findOne(query);
            if (old == null) {
                throw new NotFoundException("Member not found " + userId);
                //return;
            }
            BasicDBList members = (BasicDBList)old.get("members");
            Iterator it = members.iterator();
            while (it.hasNext()) {
                if (accountId.equals(DtoFactory.getInstance().createDtoFromJson(it.next().toString(), Member.class)
                                               .getAccountId()))
                    it.remove();
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
    public List<Subscription> getSubscriptions(String accountId) throws ServerException {
        List<Subscription> result = new ArrayList<>();
        try {
            DBObject old = accountCollection.findOne(new BasicDBObject("id", accountId));
            if (old != null) {
                DBCursor subscriptions = subscriptionCollection.find(new BasicDBObject("accountId", accountId));
                for (DBObject currentSubscription : subscriptions) {
                    result.add(DtoFactory.getInstance().createDtoFromJson(currentSubscription.toString(), Subscription.class));
                }
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return result;
    }

    @Override
    public void addSubscription(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        try {
            DBObject org = accountCollection.findOne(new BasicDBObject("id", subscription.getAccountId()));
            if (org != null) {
                ensureDateConsistency(subscription);
                subscriptionCollection.save(toDBObject(subscription));
            } else {
                throw new NotFoundException("Account not found " + subscription.getAccountId());
            }
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void removeSubscription(String subscriptionId) throws NotFoundException, ServerException {
        DBObject toRemove = subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId));
        if (toRemove != null) {
            subscriptionCollection.remove(new BasicDBObject("id", subscriptionId));
        } else {
            LOG.warn(String.format("Subscription with id = %s, cant be removed cause it doesn't exist", subscriptionId));
            throw new NotFoundException("Subscription with id = %s, cant be removed cause it doesn't exist " + subscriptionId);
        }
    }

    @Override
    public Subscription getSubscriptionById(String subscriptionId) throws NotFoundException, ServerException {
        DBObject subscription;
        try {
            subscription = subscriptionCollection.findOne(new BasicDBObject("id", subscriptionId));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        if (subscription == null)
            throw new NotFoundException("Subscription not found " + subscriptionId);
        return DtoFactory.getInstance().createDtoFromJson(subscription.toString(), Subscription.class);
    }

    /**
     * Convert account to Database ready-to-use object,
     *
     * @param obj
     *         account to convert
     * @return DBObject
     */
    private DBObject toDBObject(Account obj) {
        List<Attribute> attributes = new ArrayList<>();
        if (obj.getAttributes() != null) {
            for (Attribute one : obj.getAttributes()) {
                attributes.add(DtoFactory.getInstance().createDto(Attribute.class)
                                         .withName(one.getName())
                                         .withValue(one.getValue())
                                         .withDescription(one.getDescription()));
            }
        }
        Account account = DtoFactory.getInstance().createDto(Account.class)
                                    .withId(obj.getId())
                                    .withName(obj.getName())
                                    .withAttributes(attributes);
        return (DBObject)JSON.parse(account.toString());
    }

    /**
     * Check that start date goes before end date
     *
     * @throws com.codenvy.api.core.ConflictException
     *         when end date goes before start date
     */
    private void ensureDateConsistency(Subscription subscription) throws ConflictException {
        if (subscription.getStartDate() >= subscription.getEndDate()) {
            throw new ConflictException("Subscription startDate should go before endDate");
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
                                  .withAccountId(obj.getAccountId())
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
                                              .withAccountId(obj.getAccountId())
                                              .withServiceId(obj.getServiceId())
                                              .withStartDate(obj.getStartDate())
                                              .withEndDate(obj.getEndDate())
                                              .withProperties(obj.getProperties());
        return (DBObject)JSON.parse(subscription.toString());
    }
}
