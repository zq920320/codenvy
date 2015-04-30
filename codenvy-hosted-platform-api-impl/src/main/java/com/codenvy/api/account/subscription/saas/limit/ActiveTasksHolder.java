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
package com.codenvy.api.account.subscription.saas.limit;

import com.codenvy.api.account.metrics.MeteredBuildEventSubscriber;
import com.codenvy.api.account.server.WorkspaceResourcesUsageLimitChangedEvent;
import com.codenvy.api.account.subscription.ServiceId;
import com.codenvy.api.account.subscription.SubscriptionEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.internal.RunnerEvent;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Holder for active metered builds and runs.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ActiveTasksHolder {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveTasksHolder.class);

    private final Multimap<String, MeteredTask>  activeTasks;
    private final Map<String, ResourcesWatchdog> activeWatchdogs;
    final         LoadingCache<String, String>   accountIdsCache;
    private final ReadWriteLock                  lock;
    private final EventService                   eventService;
    private final RunQueue                       runQueue;
    private final BuildQueue                     buildQueue;
    private final ResourcesWatchdogFactory       resourcesWatchdogFactory;

    final BuildEventSubscriber               buildEventSubscriber;
    final RunEventSubscriber                 runEventSubscriber;
    final ChangeSubscriptionSubscriber       changeSubscriptionSubscriber;
    final ChangeResourceUsageLimitSubscriber changeResourceUsageLimitSubscriber;

    @Inject
    public ActiveTasksHolder(final WorkspaceDao workspaceDao,
                             EventService eventService,
                             BuildQueue buildQueue,
                             RunQueue runQueue,
                             ResourcesWatchdogFactory resourcesWatchdogFactory) {
        this.eventService = eventService;
        this.runQueue = runQueue;
        this.buildQueue = buildQueue;
        this.resourcesWatchdogFactory = resourcesWatchdogFactory;

        this.buildEventSubscriber = new BuildEventSubscriber(buildQueue);
        this.runEventSubscriber = new RunEventSubscriber();
        this.changeSubscriptionSubscriber = new ChangeSubscriptionSubscriber();
        this.changeResourceUsageLimitSubscriber = new ChangeResourceUsageLimitSubscriber();

        this.activeTasks = ArrayListMultimap.create();
        this.activeWatchdogs = new HashMap<>();
        this.accountIdsCache = CacheBuilder.newBuilder()
                                           .build(
                                                   new CacheLoader<String, String>() {
                                                       public String load(String key) throws NotFoundException, ServerException {
                                                           return workspaceDao.getById(key).getAccountId();
                                                       }
                                                   });
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * @return watchdogs which have active tasks
     */
    public Collection<ResourcesWatchdog> getActiveWatchdogs() {
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
    public Collection<MeteredTask> getActiveTasks(String watchdogId) {
        lock.readLock().lock();
        try {
            return new ArrayList<>(activeTasks.get(watchdogId));
        } finally {
            lock.readLock().unlock();
        }
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(buildEventSubscriber);
        eventService.subscribe(runEventSubscriber);
        eventService.subscribe(changeSubscriptionSubscriber);
        eventService.subscribe(changeResourceUsageLimitSubscriber);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(buildEventSubscriber);
        eventService.unsubscribe(runEventSubscriber);
        eventService.unsubscribe(changeSubscriptionSubscriber);
        eventService.unsubscribe(changeResourceUsageLimitSubscriber);
    }

    private void addMeteredTask(String workspaceId, MeteredTask meteredTask) {
        String accountId = null;
        try {
            accountId = accountIdsCache.get(workspaceId);
        } catch (ExecutionException e) {
            LOG.error("Error calculate accountId  in workspace " + workspaceId, e);
        }

        lock.writeLock().lock();
        try {
            if (accountId != null) {
                if (!activeWatchdogs.containsKey(accountId)) {
                    activeWatchdogs.put(accountId, resourcesWatchdogFactory.createAccountWatchdog(accountId));
                }
                activeTasks.put(accountId, meteredTask);
            } else {
                LOG.error("Error tracking of metered task " + meteredTask.getId() + ". Can't calculate account id.");
            }

            if (!activeWatchdogs.containsKey(workspaceId)) {
                activeWatchdogs.put(workspaceId, resourcesWatchdogFactory.createWorkspaceWatchdog(workspaceId));
            }
            activeTasks.put(workspaceId, meteredTask);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeMeteredTask(String workspaceId, MeteredTask meteredTask) {
        String accountId = null;
        try {
            accountId = accountIdsCache.get(workspaceId);
        } catch (ExecutionException e) {
            LOG.error("Error calculate accountId  in workspace " + workspaceId, e);
        }

        lock.writeLock().lock();
        try {
            activeTasks.remove(workspaceId, meteredTask);
            if (!activeTasks.containsKey(workspaceId)) {
                activeWatchdogs.remove(workspaceId);
            }

            if (accountId != null) {
                activeTasks.remove(accountId, meteredTask);
                if (!activeTasks.containsKey(accountId)) {
                    activeWatchdogs.remove(accountId);
                    accountIdsCache.invalidate(workspaceId);
                }
            } else {
                //Should not happen. But next code block provides assurance that the inactive tasks will not be kept
                Iterator<Map.Entry<String, MeteredTask>> iterator = activeTasks.entries().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getValue().equals(meteredTask)) {
                        iterator.remove();
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    class ChangeSubscriptionSubscriber implements EventSubscriber<SubscriptionEvent> {
        @Override
        public void onEvent(SubscriptionEvent event) {
            Subscription subscription = event.getSubscription();
            if (!ServiceId.SAAS.equals(subscription.getServiceId())) {
                return;
            }

            switch (event.getType()) {
                case ADDED:
                case REMOVED:
                    recheckWatchdog(subscription.getAccountId());
                    break;
                case RENEWED:
                    //do nothing
                    break;
            }
        }
    }

    class ChangeResourceUsageLimitSubscriber implements EventSubscriber<WorkspaceResourcesUsageLimitChangedEvent> {
        @Override
        public void onEvent(WorkspaceResourcesUsageLimitChangedEvent event) {
            recheckWatchdog(event.getWorkspaceId());
        }
    }

    private void recheckWatchdog(String watchdogId) {
        ResourcesWatchdog resourcesWatchdog = null;
        lock.readLock().lock();
        try {
            resourcesWatchdog = activeWatchdogs.get(watchdogId);
        } finally {
            lock.readLock().unlock();
        }

        if (resourcesWatchdog != null) {
            resourcesWatchdog.checkLimit();
        }
    }

    class BuildEventSubscriber extends MeteredBuildEventSubscriber {
        public BuildEventSubscriber(BuildQueue buildQueue) {
            super(buildQueue);
        }

        @Override
        public void onMeteredBuildEvent(BuilderEvent event) {
            switch (event.getType()) {
                case BUILD_TASK_ADDED_IN_QUEUE:
                    addMeteredTask(event.getWorkspace(), new MeteredTaskBuild(event));
                    break;
                case DONE:
                case CANCELED:
                    removeMeteredTask(event.getWorkspace(), new MeteredTaskBuild(event));
                    break;
            }
        }
    }

    class RunEventSubscriber implements EventSubscriber<RunnerEvent> {
        @Override
        public void onEvent(RunnerEvent event) {
            switch (event.getType()) {
                case RUN_TASK_ADDED_IN_QUEUE:
                    addMeteredTask(event.getWorkspace(), new MeteredTaskRun(event));
                    break;
                case ERROR:
                case STOPPED:
                case CANCELED:
                    removeMeteredTask(event.getWorkspace(), new MeteredTaskRun(event));
                    break;
            }
        }
    }

    private class MeteredTaskBuild implements MeteredTask {
        private BuilderEvent builderEvent;

        public MeteredTaskBuild(BuilderEvent builderEvent) {
            this.builderEvent = builderEvent;
        }

        @Override
        public String getId() {
            return "build-" + builderEvent.getTaskId();
        }

        @Override
        public void interrupt() throws Exception {
            try {
                buildQueue.getTask(builderEvent.getTaskId()).cancel();
            } catch (NotFoundException nfe) {
                LOG.warn("Can't interrupt build. " + nfe.getLocalizedMessage());
                removeMeteredTask(builderEvent.getWorkspace(), this);
            }
        }

        @Override
        public int hashCode() {
            return 7 + (int)(builderEvent.getTaskId() ^ (builderEvent.getTaskId() >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MeteredTaskBuild)) {
                return false;
            }
            final MeteredTaskBuild other = (MeteredTaskBuild)obj;
            return Objects.equals(builderEvent.getTaskId(), other.builderEvent.getTaskId());
        }
    }

    private class MeteredTaskRun implements MeteredTask {
        private RunnerEvent runnerEvent;

        public MeteredTaskRun(RunnerEvent runnerEvent) {
            this.runnerEvent = runnerEvent;
        }

        @Override
        public String getId() {
            return "run-" + runnerEvent.getProcessId();
        }

        @Override
        public void interrupt() throws Exception {
            try {
                runQueue.getTask(runnerEvent.getProcessId()).stop();
            } catch (NotFoundException nfe) {
                LOG.warn("Can't interrupt build. " + nfe.getLocalizedMessage());
                removeMeteredTask(runnerEvent.getWorkspace(), this);
            }
        }

        @Override
        public int hashCode() {
            return 11 + (int)(runnerEvent.getProcessId() ^ (runnerEvent.getProcessId() >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MeteredTaskRun)) {
                return false;
            }
            final MeteredTaskRun other = (MeteredTaskRun)obj;
            return Objects.equals(runnerEvent.getProcessId(), other.runnerEvent.getProcessId());
        }
    }
}
