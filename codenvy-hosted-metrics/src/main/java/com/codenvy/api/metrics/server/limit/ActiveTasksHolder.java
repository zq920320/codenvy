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
package com.codenvy.api.metrics.server.limit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Holder for active metered builds and runs.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ActiveTasksHolder {
    private final Set<ResourcesWatchdogProvider>    watchdogProviders;
    private final ListMultimap<String, MeteredTask> watchdogId2Task;
    private final Map<String, ResourcesWatchdog>    activeWatchdogs;
    private final ReadWriteLock                     lock;

    @Inject
    public ActiveTasksHolder(Set<ResourcesWatchdogProvider> watchdogProviders) {
        this.watchdogProviders = watchdogProviders;
        this.watchdogId2Task = ArrayListMultimap.create();
        this.activeWatchdogs = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * @return watchdogs which have active tasks
     */
    public List<ResourcesWatchdog> getActiveWatchdogs() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(activeWatchdogs.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param watchdogId
     *         given watchdog
     * @return active tasks by given watchdog
     */
    public List<MeteredTask> getActiveTasks(String watchdogId) {
        lock.readLock().lock();
        try {
            return new ArrayList<>(watchdogId2Task.get(watchdogId));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addMeteredTask(MeteredTask meteredTask) {
        lock.writeLock().lock();
        try {
            for (ResourcesWatchdogProvider watchdogProvider : watchdogProviders) {
                final String watchdogId = watchdogProvider.getId(meteredTask);
                if (watchdogId != null) {
                    if (!activeWatchdogs.containsKey(watchdogId)) {
                        activeWatchdogs.put(watchdogId, watchdogProvider.createWatchdog(meteredTask));
                    }
                    watchdogId2Task.put(watchdogId, meteredTask);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeMeteredTask(MeteredTask meteredTask) {
        lock.writeLock().lock();
        try {
            for (ResourcesWatchdogProvider watchdogProvider : watchdogProviders) {
                final String watchdogId = watchdogProvider.getId(meteredTask);

                if (watchdogId != null) {
                    watchdogId2Task.remove(watchdogId, meteredTask);
                    if (!watchdogId2Task.containsKey(watchdogId)) {
                        activeWatchdogs.remove(watchdogId);
                    }
                } else {
                    //Should not happen. But next code block provides assurance that the inactive tasks will not be kept
                    Iterator<Map.Entry<String, MeteredTask>> iterator = watchdogId2Task.entries().iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getValue().equals(meteredTask)) {
                            iterator.remove();
                        }
                    }

                    for (String watchdog : activeWatchdogs.keySet()) {
                        if (!watchdogId2Task.containsKey(watchdog)) {
                            activeWatchdogs.remove(watchdog);
                        }
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ResourcesWatchdog getWatchdog(String watchdogId) {
        lock.readLock().lock();
        try {
            return activeWatchdogs.get(watchdogId);
        } finally {
            lock.readLock().unlock();
        }
    }
}
