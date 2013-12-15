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

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.storage.MongoDataStorage;
import com.mongodb.*;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/** @author Alexander Reshetnyak */
public class MongoDBBackup implements Feature {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBBackup.class);

    public static final  String   BACKUP_SUFFIX = "_backup";
    private static final String[] COLLECTIONS   = Configurator.getArray("analytics.backup.collections");

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            doExecute();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAvailable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void forceExecute(Map<String, String> context) throws JobExecutionException {
        try {
            doExecute();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private void doExecute() throws IOException {
        LOG.info("MongoDBBackup is started ");
        long start = System.currentTimeMillis();

        MongoClient mongoClient = MongoDataStorage.openConnection();
        try {
            DB db = MongoDataStorage.getUsedDB(mongoClient);

            for (String name : COLLECTIONS) {
                DBCollection src = db.getCollection(name);
                DBCollection dst = db.getCollection(name + BACKUP_SUFFIX);

                backup(src, dst);
            }
        } finally {
            mongoClient.close();
            LOG.info("MongoDBBackup was finished  in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void backup(DBCollection src, DBCollection dst) throws IOException {
        try {
            dst.drop();
        } catch (MongoException e) {
            throw new IOException("Backup failed. Can't drop " + dst.getName(), e);
        }

        try {
            Iterator<DBObject> it = src.find().iterator();
            while (it.hasNext()) {
                dst.insert(it.next());
            }
        } catch (MongoException e) {
            throw new IOException("Backup failed. Can't copy data from " + src.getName() + " to " + dst.getName(), e);
        }

        if (src.count() != dst.count()) {
            throw new IOException(
                    "Backup failed. Wrong records count between " + src.getName() + " and " + dst.getName());
        }
    }
}
