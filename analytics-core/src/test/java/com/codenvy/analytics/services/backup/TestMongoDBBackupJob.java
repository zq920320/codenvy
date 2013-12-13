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
package com.codenvy.analytics.services.backup;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.storage.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * TestMongoDBBackupJob will be tested MongoDBBackupJobs.
 *
 * @author Alexander Reshetnyak
 */
public class TestMongoDBBackupJob extends BaseTest {
    
    private String[] collectionNames;
    
    @BeforeClass
    public void prepare() throws Exception {
        collectionNames = Configurator.getArray(MongoDBBackupJob.BACKUP_RESOURCES);
        
        MongoClient mongoClient = new MongoClient(MongoDataStorage.getMongoClientURI());
        DB db = mongoClient.getDB(MongoDataStorage.getMongoClientURI().getDatabase());
        
        for (String collectionName : collectionNames) {
            DBCollection dbCollection = db.getCollection(collectionName);
            
            for (int i = 0; i < 1000; i++) {
                DBObject dbObject = new BasicDBObject();
                dbObject.put("key_" + i, "object_" + i);
                dbCollection.insert(dbObject);
            }
        }
        
        mongoClient.close();
    }
    
    @AfterClass
    public void teardown() throws Exception {
        MongoClient mongoClient = new MongoClient(MongoDataStorage.getMongoClientURI());
        DB db = mongoClient.getDB(MongoDataStorage.getMongoClientURI().getDatabase());
        
        for (String collectionName : collectionNames) {
            db.getCollection(collectionName).drop();
            db.getCollection(collectionName + MongoDBBackupJob.BACKUP_SUFFIX).drop();
        }
        
        mongoClient.close();
    }

    @Test
    public void shouldReturnCorrectData() throws Exception {
        MongoDBBackupJob job = new MongoDBBackupJob();
        // First backup
        job.forceExecute(null);
        
        MongoClient mongoClient = new MongoClient(MongoDataStorage.getMongoClientURI());
        DB db = mongoClient.getDB(MongoDataStorage.getMongoClientURI().getDatabase());
        
        // check
        for (String collectionName : collectionNames) {
            assertTrue(db.collectionExists(collectionName));
            assertTrue(db.collectionExists(collectionName + MongoDBBackupJob.BACKUP_SUFFIX));
            assertEquals(1000, db.getCollection(collectionName).count());
            assertEquals(1000, db.getCollection(collectionName + MongoDBBackupJob.BACKUP_SUFFIX).count());
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
            assertTrue(db.collectionExists(collectionName + MongoDBBackupJob.BACKUP_SUFFIX));
            assertEquals(1050, db.getCollection(collectionName).count());
            assertEquals(1050, db.getCollection(collectionName + MongoDBBackupJob.BACKUP_SUFFIX).count());
        }
        
        mongoClient.close();
    }
}
