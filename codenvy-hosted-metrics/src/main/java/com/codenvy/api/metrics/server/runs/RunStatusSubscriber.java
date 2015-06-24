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
package com.codenvy.api.metrics.server.runs;

import com.codenvy.api.metrics.server.MemoryUsedMetric;
import com.codenvy.api.metrics.server.ResourcesUsageTracker;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.RunQueueTask;
import org.eclipse.che.api.runner.dto.RunRequest;
import org.eclipse.che.api.runner.internal.RunnerEvent;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Registers start and end of runner resources usage
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RunStatusSubscriber implements EventSubscriber<RunnerEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(RunStatusSubscriber.class);

    private final Integer               schedulingPeriod;
    private final EventService          eventService;
    private final WorkspaceDao          workspaceDao;
    private final RunQueue              runQueue;
    private final ResourcesUsageTracker resourcesUsageTracker;

    @Inject
    public RunStatusSubscriber(@Named(RunTasksActivityChecker.RUN_ACTIVITY_CHECKING_PERIOD) Integer schedulingPeriod,
                               EventService eventService,
                               WorkspaceDao workspaceDao,
                               RunQueue runQueue,
                               ResourcesUsageTracker resourcesUsageTracker) {
        this.schedulingPeriod = schedulingPeriod;
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
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
                resourcesUsageTracker.resourceUsageStopped(RunTasksActivityChecker.PFX + String.valueOf(event.getProcessId()));
                break;
            default:
        }
    }

    private void registerMemoryUsage(RunnerEvent event) {
        try {
            final Workspace workspace = workspaceDao.getById(event.getWorkspace());
            final RunQueueTask task;
            try {
                task = runQueue.getTask(event.getProcessId());
            } catch (NotFoundException nfe) {
                //task already is interrupted
                return;
            }
            final RunRequest request = task.getRequest();
            final MemoryUsedMetric memoryUsedMetric = new MemoryUsedMetric(request.getMemorySize(),
                                                                           task.getCreationTime(),
                                                                           task.getCreationTime() + schedulingPeriod,
                                                                           request.getUserId(),
                                                                           workspace.getAccountId(),
                                                                           workspace.getId(),
                                                                           RunTasksActivityChecker.PFX + String.valueOf(
                                                                                   event.getProcessId()));

            resourcesUsageTracker.resourceUsageStarted(memoryUsedMetric);
        } catch (NotFoundException | ServerException e) {
            LOG.error(String.format("Error registration usage of resources by run process %s in workspace %s in project %s",
                                    event.getProcessId(), event.getWorkspace(), event.getProject()), e);
        }
    }
}
