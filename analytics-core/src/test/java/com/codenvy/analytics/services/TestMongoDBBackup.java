/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.services;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.storage.MongoDataStorage;
import com.mongodb.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author Alexander Reshetnyak */
public class TestMongoDBBackup extends BaseTest {

    private String[] collectionNames;

    @BeforeClass
    public void prepare() throws Exception {
        collectionNames = new String[]{"test1_statistics", "test2_statistics"};

        MongoClient mongoClient = MongoDataStorage.getClient();
        DB db = MongoDataStorage.getUsedDB(mongoClient);

        for (String collectionName : collectionNames) {
            DBCollection dbCollection = db.getCollection(collectionName);

            for (int i = 0; i < 1000; i++) {
                DBObject dbObject = new BasicDBObject();
                dbObject.put("key_" + i, "object_" + i);
                dbCollection.insert(dbObject);
            }
        }
    }

    @AfterClass
    public void tearDown() throws Exception {
        MongoClient mongoClient = MongoDataStorage.getClient();
        DB db = MongoDataStorage.getUsedDB(mongoClient);

        for (String collectionName : collectionNames) {
            db.getCollection(collectionName).drop();
            db.getCollection(collectionName + MongoDBBackup.BACKUP_SUFFIX).drop();
        }
    }

    @Test
    public void shouldReturnCorrectData() throws Exception {
        MongoDBBackup job = new MongoDBBackup();
        job.forceExecute(null);

        MongoClient mongoClient = MongoDataStorage.getClient();
        DB db = MongoDataStorage.getUsedDB(mongoClient);

        // check
        for (String collectionName : collectionNames) {
            assertTrue(db.collectionExists(collectionName));
            assertTrue(db.collectionExists(collectionName + MongoDBBackup.BACKUP_SUFFIX));
            assertEquals(1000, db.getCollection(collectionName).count());
            assertEquals(1000, db.getCollection(collectionName + MongoDBBackup.BACKUP_SUFFIX).count());
        }

        // add data
        for (String collectionName : collectionNames) {
            DBCollection dbCollection = db.getCollection(collectionName);

            for (int i = 1000; i < 1050; i++) {
                DBObject dbObject = new BasicDBObject();
                dbObject.put("key_" + i, "object_" + i);
                dbCollection.insert(dbObject);
            }
        }

        // Second backup
        job.forceExecute(null);

        // check
        for (String collectionName : collectionNames) {
            assertTrue(db.collectionExists(collectionName));
            assertTrue(db.collectionExists(collectionName + MongoDBBackup.BACKUP_SUFFIX));
            assertEquals(1050, db.getCollection(collectionName).count());
            assertEquals(1050, db.getCollection(collectionName + MongoDBBackup.BACKUP_SUFFIX).count());
        }
    }
}
