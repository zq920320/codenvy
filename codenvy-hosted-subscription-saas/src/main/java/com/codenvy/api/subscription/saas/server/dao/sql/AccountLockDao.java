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
package com.codenvy.api.subscription.saas.server.dao.sql;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class AccountLockDao {
    private static final Logger LOG = LoggerFactory.getLogger(AccountLockDao.class);

    private static final String ACCOUNT_COLLECTION   = "organization.storage.db.account.collection";

    private final DBCollection accountCollection;

    @Inject
    public AccountLockDao(DB db,
                          @Named(ACCOUNT_COLLECTION) String accountCollectionName) {
        accountCollection = db.getCollection(accountCollectionName);
    }

    /**
     * Get all accounts which are locked after RAM runner resources was exceeded.
     *
     * @return all locked accounts
     */
    public List<Account> getAccountsWithLockedResources() throws ServerException, ForbiddenException {
        DBObject query = QueryBuilder.start("attributes").elemMatch(new BasicDBObject("name", Constants.RESOURCES_LOCKED_PROPERTY)).get();

        try (DBCursor accounts = accountCollection.find(query)) {
            final ArrayList<Account> result = new ArrayList<>();
            for (DBObject accountObj : accounts) {
                result.add(toAccount(accountObj));
            }
            return result;
        } catch (MongoException me) {
            LOG.error(me.getMessage(), me);
            throw new ServerException("It is not possible to retrieve accounts");
        }
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
     * Converts database list to Map
     */
    public static Map<String, String> asMap(Object src) {
        if (!(src instanceof BasicDBList)) {
            throw new IllegalArgumentException("BasicDBList was expected");
        }
        final BasicDBList list = (BasicDBList)src;
        final Map<String, String> map = new HashMap<>();
        for (Object obj : list) {
            final BasicDBObject attribute = (BasicDBObject)obj;
            map.put(attribute.getString("name"), attribute.getString("value"));
        }
        return map;
    }
}
