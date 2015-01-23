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
package com.codenvy.api.metrics;

import com.codenvy.api.account.MemoryUsedMetric;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.RunQueueTask;
import com.codenvy.api.runner.dto.RunRequest;
import com.codenvy.api.runner.internal.RunnerEvent;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Registers start and end of resources usage
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RunStatusSubscriber implements EventSubscriber<RunnerEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(RunStatusSubscriber.class);

    private final Integer               schedulingPeriod;
    private final EventService          eventService;
    private final WorkspaceDao          workspaceDao;
    private final UserDao               userDao;
    private final RunQueue              runQueue;
    private final ResourcesUsageTracker resourcesUsageTracker;

    @Inject
    public RunStatusSubscriber(@Named(TasksActivityChecker.RUN_ACTIVITY_CHECKING_PERIOD) Integer schedulingPeriod,
                               EventService eventService,
                               WorkspaceDao workspaceDao,
                               UserDao userDao,
                               RunQueue runQueue,
                               ResourcesUsageTracker resourcesUsageTracker) {
        this.schedulingPeriod = schedulingPeriod;
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
        this.userDao = userDao;
        this.runQueue = runQueue;
        this.resourcesUsageTracker = resourcesUsageTracker;
    }

    @PostConstruct
    private void startScheduling() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(RunnerEvent event) {
        switch (event.getType()) {
            case STARTED:
                registerMemoryUsage(event);
                break;
            case STOPPED:
                resourcesUsageTracker.resourceUsageStopped(event.getProcessId());
                break;
        }
    }

    private void registerMemoryUsage(RunnerEvent event) {
        try {
            final Workspace workspace = workspaceDao.getById(event.getWorkspace());
            final RunQueueTask task = runQueue.getTask(event.getProcessId());
            final RunRequest request = task.getRequest();
            final MemoryUsedMetric memoryUsedMetric = new MemoryUsedMetric(request.getMemorySize(),
                                                                           task.getCreationTime(),
                                                                           task.getCreationTime() + schedulingPeriod,
                                                                           request.getUserId(),
                                                                           workspace.getAccountId(),
                                                                           workspace.getId(),
                                                                           String.valueOf(event.getProcessId()));

            resourcesUsageTracker.resourceUsageStarted(memoryUsedMetric);
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error registration usage of resources by process {} in workspace {} in project {}", event.getProcessId(),
                      event.getWorkspace(), event.getProject());
        }
    }
}
