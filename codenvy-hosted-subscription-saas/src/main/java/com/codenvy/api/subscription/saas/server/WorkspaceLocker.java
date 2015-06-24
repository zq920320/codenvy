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
package com.codenvy.api.subscription.saas.server;

import com.codenvy.api.metrics.server.WorkspaceLockEvent;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.metrics.server.dao.MeterBasedStorage;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.lang.String.format;
import static org.eclipse.che.api.account.server.Constants.RESOURCES_LOCKED_PROPERTY;
import static org.eclipse.che.api.workspace.server.Constants.RESOURCES_USAGE_LIMIT_PROPERTY;

/**
 * Locks and unlocks resources usage in workspace
 *
 * @author Sergii Leschenko
 */
public class WorkspaceLocker {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceLocker.class);
    private final WorkspaceDao      workspaceDao;
    private final EventService      eventService;
    private final MetricPeriod      metricPeriod;
    private final MeterBasedStorage meterBasedStorage;

    @Inject
    public WorkspaceLocker(WorkspaceDao workspaceDao,
                           EventService eventService,
                           MetricPeriod metricPeriod,
                           MeterBasedStorage meterBasedStorage) {
        this.workspaceDao = workspaceDao;
        this.eventService = eventService;
        this.metricPeriod = metricPeriod;
        this.meterBasedStorage = meterBasedStorage;
    }

    /**
     * Sets resources lock for workspace with given id.
     * Workspace won't be locked second time if it already has resources lock
     */
    public void setResourcesLock(String workspaceId) {
        try {
            Workspace workspace = workspaceDao.getById(workspaceId);
            if (!workspace.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY)) {
                workspace.getAttributes().put(RESOURCES_LOCKED_PROPERTY, "true");
                try {
                    workspaceDao.update(workspace);
                    eventService.publish(WorkspaceLockEvent.workspaceLockedEvent(workspaceId));
                } catch (NotFoundException | ServerException | ConflictException e) {
                    LOG.error(format("Error writing lock property into workspace %s .", workspace.getId()), e);
                }
            }
        } catch (NotFoundException | ServerException e) {
            LOG.error(format("Can't get workspace %s for writing resources lock property", workspaceId), e);
        }
    }

    /**
     * Removes resources lock for workspace with given id.
     * Workspace's resources won't be unlocked if workspace hasn't resources lock.
     * Workspace won't be unlocked if resources usage reached workspace's limit
     */
    public void removeResourcesLock(String workspaceId) {
        try {
            Workspace workspace = workspaceDao.getById(workspaceId);
            if (workspace.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY)) {
                if (isReachedResourcesUsageLimit(workspace)) {
                    // Reached resources usage limit in workspace. Do not unlock it
                    return;
                }
                workspace.getAttributes().remove(RESOURCES_LOCKED_PROPERTY);
                try {
                    workspaceDao.update(workspace);
                    eventService.publish(WorkspaceLockEvent.workspaceUnlockedEvent(workspaceId));
                } catch (NotFoundException | ServerException | ConflictException e) {
                    LOG.error(format("Error writing lock property into workspace %s .", workspace.getId()), e);
                }
            }
        } catch (NotFoundException | ServerException e) {
            LOG.error(format("Can't get workspace %s for writing resources lock property", workspaceId), e);
        }
    }

    private boolean isReachedResourcesUsageLimit(Workspace workspace) throws ServerException {
        if (workspace.getAttributes().containsKey(RESOURCES_USAGE_LIMIT_PROPERTY)) {
            long billingPeriodStart = metricPeriod.getCurrent().getStartDate().getTime();
            Double usedMemory = meterBasedStorage.getUsedMemoryByWorkspace(workspace.getId(),
                                                                           billingPeriodStart,
                                                                           System.currentTimeMillis());
            Double allowedMemoryUsage = Double.parseDouble(workspace.getAttributes().get(RESOURCES_USAGE_LIMIT_PROPERTY));
            return usedMemory >= allowedMemoryUsage;
        }
        return false;
    }
}
