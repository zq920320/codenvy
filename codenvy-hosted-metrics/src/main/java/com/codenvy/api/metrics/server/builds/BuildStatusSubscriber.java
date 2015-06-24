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
package com.codenvy.api.metrics.server.builds;

import com.codenvy.api.metrics.server.MemoryUsedMetric;
import com.codenvy.api.metrics.server.ResourcesUsageTracker;

/*import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuildQueueTask;
import org.eclipse.che.api.builder.dto.BaseBuilderRequest;
import org.eclipse.che.api.builder.internal.BuilderEvent;*/
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
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
 * Registers start and end of builder resources usage
 *
 * @author Max Shaposhnik
 */
@Singleton
public class BuildStatusSubscriber extends MeteredBuildEventSubscriber {
    /*private static final Logger LOG = LoggerFactory.getLogger(BuildStatusSubscriber.class);

    private final Integer               schedulingPeriod;
    private final EventService          eventService;
    private final ResourcesUsageTracker resourcesUsageTracker;
    private final WorkspaceDao          workspaceDao;
    private final BuildQueue            buildQueue;

    @Inject
    public BuildStatusSubscriber(@Named(BuildTasksActivityChecker.RUN_ACTIVITY_CHECKING_PERIOD) Integer schedulingPeriod,
                                 EventService eventService,
                                 WorkspaceDao workspaceDao,
                                 BuildQueue buildQueue,
                                 ResourcesUsageTracker resourcesUsageTracker) {
        super(buildQueue);
        this.schedulingPeriod = schedulingPeriod;
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
        this.buildQueue = buildQueue;
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
    public void onMeteredBuildEvent(BuilderEvent event) {
        switch (event.getType()) {
            case BEGIN:
                registerMemoryUsage(event);
                break;
            case DONE:
                resourcesUsageTracker.resourceUsageStopped(BuildTasksActivityChecker.PFX + String.valueOf(event.getTaskId()));
                break;
        }
    }

    private void registerMemoryUsage(BuilderEvent event) {
        try {
            final Workspace workspace = workspaceDao.getById(event.getWorkspace());
            final BuildQueueTask task;
            try {
                task = buildQueue.getTask(event.getTaskId());
            } catch (NotFoundException nfe) {
                //task already is interrupted
                return;
            }
            final BaseBuilderRequest request = task.getRequest();
            final MemoryUsedMetric memoryUsedMetric = new MemoryUsedMetric(BuildTasksActivityChecker.BUILDER_MEMORY_SIZE,
                                                                           task.getCreationTime(),
                                                                           task.getCreationTime() + schedulingPeriod,
                                                                           request.getUserId(),
                                                                           workspace.getAccountId(),
                                                                           workspace.getId(),
                                                                           BuildTasksActivityChecker.PFX + String.valueOf(
                                                                                   event.getTaskId()));

            resourcesUsageTracker.resourceUsageStarted(memoryUsedMetric);
        } catch (NotFoundException | ServerException e) {
            LOG.error(String.format("Error registration usage of resources by build process %s in workspace %s in project %s",
                                    event.getTaskId(), event.getWorkspace(), event.getProject()), e);
        }
    }*/
}
