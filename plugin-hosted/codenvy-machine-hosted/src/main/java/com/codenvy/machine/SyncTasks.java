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
package com.codenvy.machine;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Machine projects sync tasks manager
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class SyncTasks {
    private static final Logger LOG = LoggerFactory.getLogger(SyncTasks.class);

    private final ExecutorService                                     executor;
    private final ConcurrentHashMap<String, SyncthingSynchronizeTask> projectsSyncTasks;

    @Inject
    public SyncTasks() {
        this.projectsSyncTasks = new ConcurrentHashMap<>();
        this.executor = Executors
                .newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("RemoteMachineSlaveImpl-%d").setDaemon(true).build());
    }

    public void startTask(String key, SyncthingSynchronizeTask syncTask) {
        projectsSyncTasks.put(key, syncTask);
        executor.submit(syncTask);
    }

    public SyncthingSynchronizeTask stopTask(String key) {
        final SyncthingSynchronizeTask syncTask = projectsSyncTasks.remove(key);
        try {
            syncTask.cancel();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return syncTask;
    }
}
