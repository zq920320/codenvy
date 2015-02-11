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
package com.codenvy.api.account.metrics;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.builder.BuildQueue;
import com.codenvy.api.builder.BuildQueueTask;
import com.codenvy.api.builder.dto.BaseBuilderRequest;
import com.codenvy.api.builder.dto.DependencyRequest;
import com.codenvy.api.builder.internal.BuilderEvent;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;

/**
 * Registers start and end of builder resources usage
 *
 * @author Max Shaposhnik
 */
@Singleton
public class BuildStatusSubscriber implements EventSubscriber<BuilderEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(BuildStatusSubscriber.class);

    private final Integer               schedulingPeriod;
    private final EventService          eventService;
    private final ResourcesUsageTracker resourcesUsageTracker;
    private final WorkspaceDao          workspaceDao;
    private final BuildQueue            buildQueue;
    private final BillingPeriod         billingPeriod;

    @Inject
    public BuildStatusSubscriber(@Named(BuildTasksActivityChecker.RUN_ACTIVITY_CHECKING_PERIOD) Integer schedulingPeriod,
                                 EventService eventService,
                                 WorkspaceDao workspaceDao,
                                 BuildQueue buildQueue,
                                 ResourcesUsageTracker resourcesUsageTracker,
                                 BillingPeriod billingPeriod) {
        this.schedulingPeriod = schedulingPeriod;
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
        this.buildQueue = buildQueue;
        this.resourcesUsageTracker = resourcesUsageTracker;
        this.billingPeriod = billingPeriod;
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
    public void onEvent(BuilderEvent event) {
        switch (event.getType()) {
            case BEGIN:
                if (!isDependencyRequest(event)) {
                    registerMemoryUsage(event);
                }
                break;
            case DONE:
                resourcesUsageTracker.resourceUsageStopped(BuildTasksActivityChecker.PFX + String.valueOf(event.getTaskId()));
                break;
        }
    }

    private void registerMemoryUsage(BuilderEvent event) {
        try {
            final Workspace workspace = workspaceDao.getById(event.getWorkspace());
            final BuildQueueTask task = buildQueue.getTask(event.getTaskId());
            final BaseBuilderRequest request = task.getRequest();
            final MemoryUsedMetric memoryUsedMetric = new MemoryUsedMetric(BuildTasksActivityChecker.BUILDER_MEMORY_SIZE,
                                                                           task.getCreationTime(),
                                                                           Math.min(task.getCreationTime() +
                                                                                    schedulingPeriod,
                                                                                    billingPeriod
                                                                                            .get(new Date(
                                                                                                    task.getCreationTime()))
                                                                                            .getEndDate().getTime()),
                                                                           request.getUserId(),
                                                                           workspace.getAccountId(),
                                                                           workspace.getId(),
                                                                           BuildTasksActivityChecker.PFX + String.valueOf(
                                                                                   event.getTaskId()));

            resourcesUsageTracker.resourceUsageStarted(memoryUsedMetric);
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error registration usage of resources by build process {} in workspace {} in project {}",
                      event.getTaskId(),
                      event.getWorkspace(), event.getProject());
        }
    }

    private boolean isDependencyRequest(BuilderEvent event) {
        try {
            final BaseBuilderRequest request = buildQueue.getTask(event.getTaskId()).getRequest();
            return request instanceof DependencyRequest;
        } catch (NotFoundException e) {
            LOG.error("Unable to determine request type for request {}", event.getTaskId());
        }
        return false;
    }
}
