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

import com.codenvy.api.metrics.server.dao.MeterBasedStorage;
import com.codenvy.api.metrics.server.period.MetricPeriod;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static org.eclipse.che.api.workspace.server.Constants.RESOURCES_USAGE_LIMIT_PROPERTY;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceCapsResourcesWatchdogProvider implements ResourcesWatchdogProvider {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceCapsResourcesWatchdogProvider.class);

    private final MetricPeriod      metricPeriod;
    private final MeterBasedStorage meterBasedStorage;
    private final WorkspaceDao      workspaceDao;
    private final WorkspaceLocker   workspaceLocker;

    @Inject
    public WorkspaceCapsResourcesWatchdogProvider(MetricPeriod metricPeriod,
                                                  MeterBasedStorage meterBasedStorage,
                                                  WorkspaceDao workspaceDao,
                                                  WorkspaceLocker workspaceLocker) {
        this.metricPeriod = metricPeriod;
        this.meterBasedStorage = meterBasedStorage;
        this.workspaceDao = workspaceDao;
        this.workspaceLocker = workspaceLocker;
    }

    @Override
    public String getId(MeteredTask meteredTask) {
        return meteredTask.getWorkspaceId();
    }

    @Override
    public ResourcesWatchdog createWatchdog(MeteredTask meteredTask) {
        return new WorkspaceResourcesWatchdog(meteredTask.getWorkspaceId());
    }

    class WorkspaceResourcesWatchdog implements ResourcesWatchdog {
        private final String  workspaceId;
        private       boolean hasLimit;
        private       double  resourcesUsageLimit;

        public WorkspaceResourcesWatchdog(String workspaceId) {
            this.workspaceId = workspaceId;
            checkLimit();
        }

        @Override
        public String getId() {
            return workspaceId;
        }

        @Override
        public boolean isLimitedReached() {
            if (!hasLimit) {
                return false;
            }

            try {
                long billingPeriodStart = metricPeriod.getCurrent().getStartDate().getTime();
                Double usedMemory = meterBasedStorage.getUsedMemoryByWorkspace(workspaceId, billingPeriodStart, System.currentTimeMillis());
                return usedMemory > resourcesUsageLimit;
            } catch (ServerException e) {
                LOG.error("Can't check resources consuming in workspace " + workspaceId, e);
            }

            return false;
        }

        @Override
        public void checkLimit() {
            try {
                final Map<String, String> attributes = workspaceDao.getById(workspaceId).getAttributes();
                if (attributes.containsKey(RESOURCES_USAGE_LIMIT_PROPERTY)) {
                    resourcesUsageLimit = Double.parseDouble(attributes.get(RESOURCES_USAGE_LIMIT_PROPERTY));
                    hasLimit = true;
                } else {
                    hasLimit = false;
                }
            } catch (NotFoundException | ServerException e) {
                hasLimit = false;
                LOG.error("Can't check resources usage limit in workspace " + workspaceId, e);
            }
        }

        @Override
        public void lock() {
            workspaceLocker.setResourcesLock(workspaceId);
        }
    }
}
