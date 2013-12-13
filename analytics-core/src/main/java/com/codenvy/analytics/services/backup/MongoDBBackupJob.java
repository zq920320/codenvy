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

import java.util.Iterator;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.storage.MongoDataStorage;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * The backup job will be backup ... every the Friday of week.
 *  
 * @author Alexander Reshetnyak
 * */
public class MongoDBBackupJob implements Feature {
    
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBBackupJob.class);
    public static final String BACKUP_RESOURCES = "backup.resources.list";
    public static final String BACKUP_SUFFIX = "_BACKUP";
    private final String[] collectionNames;
    private final MongoClientURI mongoClientURI;

    public MongoDBBackupJob() {
        if (Configurator.exists(BACKUP_RESOURCES)) {
            collectionNames = Configurator.getArray(BACKUP_RESOURCES);
            mongoClientURI = MongoDataStorage.getMongoClientURI();
        } else {
            collectionNames = null;
            mongoClientURI = null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (collectionNames != null && context.getFireTime().getDay() == 5) {//Friday
            execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forceExecute(Map<String, String> context) throws JobExecutionException {
        if (collectionNames != null) {
            execute();
        }
    }
    
    /**
     * Execute backup the collection which was configured in analitics.conf
     * 
     * For example:
     * backup.resources.list=users_statistics,workspaces_statistics
     * 
     * @throws JobExecutionException
     */
    private void execute() throws JobExecutionException {
        LOG.info("MongoDBBackupJob is execute ...");
        long start = System.currentTimeMillis();
        
        try {
            MongoClient mongoClient = new MongoClient(mongoClientURI);
            try {
                DB db = mongoClient.getDB(mongoClientURI.getDatabase());
                
                for (String name : collectionNames) {
                    long startCol = System.currentTimeMillis();
                    DBCollection dbCollectionSource = db.getCollection(name);

                    String collectionDestName = name + BACKUP_SUFFIX;
                    String collectionDestNameRemoved = collectionDestName + "_REMOVED";
                    
                    if (db.collectionExists(collectionDestName)) {
                        db.getCollection(collectionDestName).rename(collectionDestNameRemoved);
                    }

                    DBCollection dbCollectionDest = db.getCollection(collectionDestName);
                    
                    try
                    {
                        Iterator<DBObject> it = dbCollectionSource.find().iterator();
                        while (it.hasNext()) {
                            dbCollectionDest.insert(it.next());
                        }
                        
                        LOG.info("The collection \"" + dbCollectionSource.getName() + "\" was backup in " 
                                 + (System.currentTimeMillis() - startCol) / 1000 + " sec.");
                    } finally {
                        if (db.collectionExists(collectionDestNameRemoved)) {
                            if (dbCollectionDest.count() == dbCollectionSource.count()) {
                                db.getCollection(collectionDestNameRemoved).drop();
                            } else {
                                LOG.error("The problem have at backup the collection \"" + dbCollectionSource.getName() + "\"." +
                                         " So, the current broken backup will be removed and restoring previous backup.");
                                dbCollectionDest.drop();
                                db.getCollection(collectionDestNameRemoved).rename(collectionDestName);
                            }
                        }
                    }
                }
            } finally {
                mongoClient.close();
            }
        } catch (Throwable e) {
            throw new JobExecutionException("Can not execute backup on MongoDB storage." , e);
        } finally {
            LOG.info("MongoDBBackupJob was finished backup in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
    
}
