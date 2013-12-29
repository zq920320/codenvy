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
import com.codenvy.analytics.persistent.CollectionsManagement;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/** @author Alexander Reshetnyak */
public class MongoDBBackup implements Feature {

    private static final Logger LOG         = LoggerFactory.getLogger(MongoDBBackup.class);
    private static final String COLLECTIONS = "analytics.backup.collections";

    private final CollectionsManagement collectionsManagement;

    public MongoDBBackup() {
        collectionsManagement = new CollectionsManagement();
    }

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
        LOG.info("MongoDBBackup is started");
        long start = System.currentTimeMillis();

        try {
            if (Configurator.exists(COLLECTIONS)) {
                for (String name : Configurator.getArray(COLLECTIONS)) {
                    if (!name.isEmpty()) {
                        collectionsManagement.backup(name);
                    }
                }
            }
        } finally {
            LOG.info("MongoDBBackup is finished  in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
}
