/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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

import com.codenvy.api.event.user.RemoveAccountEvent;
import com.google.common.annotations.VisibleForTesting;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.asMap;
import static java.lang.String.format;

/**
 * Implementation of {@link AccountDao} based on MongoDB storage.
 * <pre>
 *  Account members collection document schema:
 *
 * {
 *      "_id" : ObjectId("...")
 *      "userId" : "userId...",
 *      "accountId" : "accountId...",
 *      "roles" : [ "account/owner" ]
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

    private static final Logger LOG                = LoggerFactory.getLogger(AccountDaoImpl.class);
    private static final String ACCOUNT_COLLECTION = "organization.storage.db.account.collection";
    private static final String MEMBER_COLLECTION  = "organization.storage.db.acc.member.collection";

    private final DBCollection accountCollection;
    private final DBCollection memberCollection;
    private final WorkspaceDao workspaceDao;
    private final EventService eventService;

    @Inject
    public AccountDaoImpl(DB db,
                          WorkspaceDao workspaceDao,
                          @Named(ACCOUNT_COLLECTION) String accountCollectionName,
                          @Named(MEMBER_COLLECTION) String memberCollectionName,
                          EventService eventService) {
        this.eventService = eventService;
        accountCollection = db.getCollection(accountCollectionName);
        accountCollection.createIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        accountCollection.createIndex(new BasicDBObject("name", 1));
        accountCollection.createIndex(new BasicDBObject("attributes.name", 1).append("attributes.value", 1));
        memberCollection = db.getCollection(memberCollectionName);
        memberCollection.createIndex(new BasicDBObject("accountId", 1).append("userId", 1), new BasicDBObject("unique", true));
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
        final List<Account> accounts = new ArrayList<>();
        for (Member member : getByMember(owner)) {
            if (member.getRoles().contains("account/owner")) {
                accounts.add(getById(member.getAccountId()));
            }
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
            //Removing members
            for (Member member : getMembers(id)) {
                removeMember(member);
            }
            // Removing account itself
            accountCollection.remove(new BasicDBObject("id", id));
            eventService.publish(new RemoveAccountEvent(id));
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to remove account");
        }
    }

    @Override
    public List<Member> getMembers(String accountId) throws ServerException {
        return doGetMembers(new BasicDBObject("accountId", accountId));
    }

    @Override
    public List<Member> getByMember(String userId) throws NotFoundException, ServerException {
        return doGetMembers(new BasicDBObject("userId", userId));
    }

    @Override
    public void addMember(Member member) throws NotFoundException, ConflictException, ServerException {
        checkAccountExists(member.getAccountId());
        try {
            memberCollection.insert(toDBObject(member));
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("User '%s' is already has membership in account '%s'",
                                               member.getUserId(),
                                               member.getAccountId()));
        } catch (MongoException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void removeMember(Member member) throws NotFoundException, ServerException, ConflictException {
        try {
            if (memberCollection.remove(query(member.getUserId(), member.getAccountId())).getN() == 0) {
                throw new NotFoundException(
                        format("Membership between '%s' and '%s' not found", member.getUserId(), member.getAccountId()));
            }
        } catch (MongoException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @VisibleForTesting
    BasicDBObject query(String userId, String accountId) {
        return new BasicDBObject("userId", userId).append("accountId", accountId);
    }


    private List<Member> doGetMembers(BasicDBObject query) throws ServerException {
        final List<Member> members;
        try (DBCursor cursor = memberCollection.find(query)) {
            members = new ArrayList<>(cursor.count());
            for (DBObject memberDoc : cursor) {
                members.add(toMember(memberDoc));
            }
        }
        return members;
    }

    private void checkAccountExists(String id) throws NotFoundException {
        if (accountCollection.findOne(new BasicDBObject("id", id)) == null) {
            throw new NotFoundException(format("Account with id %s was not found", id));
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
     * Converts database object to account ready-to-use object
     */
    Account toAccount(Object dbObject) {
        final BasicDBObject accountObject = (BasicDBObject)dbObject;
        return new Account().withId(accountObject.getString("id"))
                            .withName(accountObject.getString("name"))
                            .withAttributes(asMap(accountObject.get("attributes")));
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
                                  .append("attributes", asDBList(account.getAttributes()));
    }
}
