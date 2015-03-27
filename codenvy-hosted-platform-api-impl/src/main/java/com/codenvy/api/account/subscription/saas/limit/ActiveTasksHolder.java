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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

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

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;

/**
 * Holder for active metered builds and runs.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ActiveTasksHolder {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveTasksHolder.class);

    private final Multimap<String, Interruptable> activeTasks;
    private final WorkspaceDao                    workspaceDao;
    private final EventService                    eventService;
    private final RunQueue                        runQueue;
    private final BuildQueue                      buildQueue;
    final         BuildEventSubscriber            buildEventSubscriber;
    final         RunEventSubscriber              runEventSubscriber;

    @Inject
    public ActiveTasksHolder(WorkspaceDao workspaceDao,
                             EventService eventService,
                             BuildQueue buildQueue,
                             RunQueue runQueue) {
        this.workspaceDao = workspaceDao;
        this.eventService = eventService;
        this.runQueue = runQueue;
        this.buildQueue = buildQueue;
        this.buildEventSubscriber = new BuildEventSubscriber(buildQueue);
        this.runEventSubscriber = new RunEventSubscriber();
        this.activeTasks = Multimaps.synchronizedMultimap(HashMultimap.<String, Interruptable>create());
    }

    public Set<String> getAccountsWithActiveTasks() {
        return activeTasks.keySet();
    }

    public Collection<Interruptable> getActiveTasks(String accountId) {
        return activeTasks.get(accountId);
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(buildEventSubscriber);
        eventService.subscribe(runEventSubscriber);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(buildEventSubscriber);
        eventService.unsubscribe(runEventSubscriber);
    }

    private void addInterruptableTask(String workspaceId, Interruptable interruptable) {
        String accountId = getAccountId(workspaceId);
        if (accountId != null) {
            activeTasks.put(accountId, interruptable);
        }
    }

    private void removeInterruptableTask(String workspaceId, Interruptable interruptable) {
        String accountId = getAccountId(workspaceId);
        if (accountId != null) {
            activeTasks.remove(accountId, interruptable);
        }
    }

    @Nullable
    private String getAccountId(String workspaceId) {
        try {
            return workspaceDao.getById(workspaceId).getAccountId();
        } catch (NotFoundException | ServerException e) {
            LOG.error(format("Error calculate accountId  in workspace %s .", workspaceId), e);
        }
        return null;
    }

    class BuildEventSubscriber extends MeteredBuildEventSubscriber {
        public BuildEventSubscriber(BuildQueue buildQueue) {
            super(buildQueue);
        }

        @Override
        public void onMeteredBuildEvent(BuilderEvent event) {
            switch (event.getType()) {
                case BEGIN:
                    addInterruptableTask(event.getWorkspace(), new InterruptableBuild(event));
                    break;
                case DONE:
                    removeInterruptableTask(event.getWorkspace(), new InterruptableBuild(event));
                    break;
            }
        }
    }

    class RunEventSubscriber implements EventSubscriber<RunnerEvent> {
        @Override
        public void onEvent(RunnerEvent event) {
            switch (event.getType()) {
                case STARTED:
                    addInterruptableTask(event.getWorkspace(), new InterruptableRun(event));
                    break;
                case STOPPED:
                    removeInterruptableTask(event.getWorkspace(), new InterruptableRun(event));
                    break;
            }
        }
    }

    private class InterruptableBuild implements Interruptable {
        private BuilderEvent builderEvent;

        public InterruptableBuild(BuilderEvent builderEvent) {
            this.builderEvent = builderEvent;
        }

        @Override
        public String getId() {
            return "build-" + builderEvent.getTaskId();
        }

        @Override
        public void interrupt() throws Exception {
            buildQueue.getTask(builderEvent.getTaskId()).cancel();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (int)(builderEvent.getTaskId() ^ (builderEvent.getTaskId() >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof InterruptableBuild)) {
                return false;
            }
            final InterruptableBuild other = (InterruptableBuild)obj;
            return Objects.equals(builderEvent.getTaskId(), other.builderEvent.getTaskId());
        }
    }

    private class InterruptableRun implements Interruptable {
        private RunnerEvent runnerEvent;

        public InterruptableRun(RunnerEvent runnerEvent) {
            this.runnerEvent = runnerEvent;
        }

        @Override
        public String getId() {
            return "run-" + runnerEvent.getProcessId();
        }

        @Override
        public void interrupt() throws Exception {
            runQueue.getTask(runnerEvent.getProcessId()).stop();
        }

        @Override
        public int hashCode() {
            int hash = 11;
            hash = 31 * hash + (int)(runnerEvent.getProcessId() ^ (runnerEvent.getProcessId() >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof InterruptableRun)) {
                return false;
            }
            final InterruptableRun other = (InterruptableRun)obj;
            return Objects.equals(runnerEvent.getProcessId(), other.runnerEvent.getProcessId());
        }
    }
}
