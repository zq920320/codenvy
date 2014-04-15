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
package com.codenvy.migration.rename;

import com.codenvy.api.account.shared.dto.Member;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import org.slf4j.Logger;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Tool(script) for renaming organization to account
 * <p/>
 * What is gonna be renamed:
 * <ul>
 * <li> organization(collection name) -> account (collection name)
 * </li>
 * <li>
 * workspace.organizationId -> workspace.accountId
 * </li>
 * <li>
 * member.organizationId -> member.accountId, member(collection name[orgmember]) -> member(collection name[accmember])
 * </li>
 * <li> subscription.organizationId -> subscription.accountId
 * </li>
 * </ul>
 *
 * @author Eugene Voevodin
 */
public final class RenameScript {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RenameScript.class);

    public static final String DB_URL                       = "organization.storage.db.url";
    public static final String DB_NAME                      = "organization.storage.db.name";
    public static final String DB_USERNAME                  = "organization.storage.db.username";
    public static final String DB_PASSWORD                  = "organization.storage.db.password";
    public static final String ORGANIZATION_COLLECTION_NAME = "organization.storage.db.organization.collection";
    public static final String ACCOUNT_COLLECTION_NAME      = "organization.storage.db.account.collection";
    public static final String WORKSPACE_COLLECTION_NAME    = "organization.storage.db.workspace.collection";
    public static final String ORG_MEMBER_COLLECTION_NAME   = "organization.storage.db.org.member.collection";
    public static final String ACC_MEMBER_COLLECTION_NAME   = "organization.storage.db.acc.member.collection";
    public static final String SUBSCRIPTION_COLLECTION_NAME = "organization.storage.db.subscription.collection";

    public static void execute(Properties mongoProperties) {
        verifyProperties(mongoProperties);
        DB db;
        try {
            MongoClient mongoClient = new MongoClient(mongoProperties.getProperty(DB_URL));
            db = mongoClient.getDB(mongoProperties.getProperty(DB_NAME));
            if (!db.authenticate(mongoProperties.getProperty(DB_USERNAME), mongoProperties.getProperty(DB_PASSWORD).toCharArray()))
                throw new RuntimeException("Incorrect MongoDB credentials: authentication failed.");
        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't connect to MongoDB.");
        }
        //rename organization -> account
        LOG.debug("Renaming: Organization[collection name -> account]");
        db.getCollection(mongoProperties.getProperty(ORGANIZATION_COLLECTION_NAME))
          .rename(mongoProperties.getProperty(ACCOUNT_COLLECTION_NAME));

        //rename workspace attrs
        LOG.debug("Renaming: Workspace[organization -> accountId]");
        DBCollection workspaces = db.getCollection(mongoProperties.getProperty(WORKSPACE_COLLECTION_NAME));
        renameAllAttributes(workspaces, "organizationId", "accountId");

        //rename members attrs, and collection name
        LOG.debug("Renaming: Member[organizationId -> accountId, roles: {organization/* -> account/*}]");
        DBCollection members = db.getCollection(mongoProperties.getProperty(ORG_MEMBER_COLLECTION_NAME));
        DBCursor cursor = members.find();
        for (DBObject current : cursor) {
            final BasicDBList currentMembers = (BasicDBList)current.get("members");
            final BasicDBList newMembers = new BasicDBList();
            for (Object object : currentMembers) {
                BasicDBObject dbObject = (BasicDBObject)object;
                BasicDBList roles = (BasicDBList)dbObject.get("roles");
                List<String> actualRoles = new ArrayList<>(roles.size());
                for (Object role : roles) {
                    actualRoles.add(role.toString().replace("organization", "account"));
                }
                Member member = DtoFactory.getInstance().createDto(Member.class)
                                          .withAccountId((String)dbObject.get("organizationId"))
                                          .withUserId((String)dbObject.get("userId"))
                                          .withRoles(actualRoles);
                newMembers.add(JSON.parse(member.toString()));
            }
            members.update(new BasicDBObject("_id", current.get("_id")), new BasicDBObject().append("members", newMembers));
        }
        //rename members collection with new name
        members.rename(mongoProperties.getProperty(ACC_MEMBER_COLLECTION_NAME));
        //rename subscriptions attrs
        LOG.debug("Renaming: Subscriptions[organizationId -> accountId]");
        DBCollection subscriptions = db.getCollection(mongoProperties.getProperty(SUBSCRIPTION_COLLECTION_NAME));
        renameAllAttributes(subscriptions, "organizationId", "accountId");
    }

    private static void renameAllAttributes(DBCollection collection, String oldAttributeName, String newAttributeName) {
        collection.updateMulti(new BasicDBObject(),
                               new BasicDBObject("$rename", new BasicDBObject(oldAttributeName, newAttributeName)));
    }

    private static void verifyProperties(Properties properties) {
        Objects.requireNonNull(properties);
        Set<String> needed = new HashSet<>();
        needed.add(DB_URL);
        needed.add(DB_NAME);
        needed.add(DB_USERNAME);
        needed.add(DB_PASSWORD);
        needed.add(ACC_MEMBER_COLLECTION_NAME);
        needed.add(ORG_MEMBER_COLLECTION_NAME);
        needed.add(SUBSCRIPTION_COLLECTION_NAME);
        needed.add(WORKSPACE_COLLECTION_NAME);
        needed.add(ACCOUNT_COLLECTION_NAME);
        needed.add(ORGANIZATION_COLLECTION_NAME);
        for (String expectedProperty : needed) {
            Objects.requireNonNull(properties.getProperty(expectedProperty), String.format("Property %s required", expectedProperty));
        }
    }
}
