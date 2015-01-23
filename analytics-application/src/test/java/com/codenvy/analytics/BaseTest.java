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
package com.codenvy.analytics;

import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.util.UserPrincipalCache;
import com.mongodb.DB;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

/** @author Dmytro Nochevnov */
public class BaseTest {
    public static final String BASE_DIR = "target";

    protected final Configurator                     configurator;
    protected final PigServer                        pigServer;
    protected final MongoDataStorage                 mongoDataStorage;
    protected final DB                               mongoDb;
    protected final com.codenvy.analytics.util.Utils utils;
    protected final UserPrincipalCache               cache;

    @BeforeClass
    public void clearDatabase() {
        for (String collectionName : mongoDb.getCollectionNames()) {
            if (collectionName.startsWith("system.")) {           // don't drop system collections
                continue;
            }

            mongoDb.getCollection(collectionName).drop();
        }
    }

    public BaseTest() {
        this.configurator = Injector.getInstance(Configurator.class);
        this.pigServer = Injector.getInstance(PigServer.class);
        this.mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
        this.mongoDb = mongoDataStorage.getDb();
        this.utils = Injector.getInstance(com.codenvy.analytics.util.Utils.class);
        this.cache = Injector.getInstance(UserPrincipalCache.class);
    }

    @BeforeSuite
    public void initCache() {
        this.cache.init();
    }
}
