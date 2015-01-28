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

import com.codenvy.api.account.server.MemoryUsedMetric;
import com.codenvy.api.account.server.MeterBasedStorage;
import com.codenvy.api.account.server.UsageInformer;
import com.codenvy.api.core.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class ResourcesUsageTracker {
    private static final Logger LOG = LoggerFactory.getLogger(ResourcesUsageTracker.class);

    private final MeterBasedStorage meterBasedStorage;
    private final Map<Long, UsageInformer> inMemoryStorage = new ConcurrentHashMap<>();

    @Inject
    public ResourcesUsageTracker(MeterBasedStorage meterBasedStorage) {
        this.meterBasedStorage = meterBasedStorage;
    }

    public void resourceUsageStarted(MemoryUsedMetric metric) {
        try {
            inMemoryStorage.put(Long.parseLong(metric.getRunId()), meterBasedStorage.createMemoryUsedRecord(metric));
        } catch (ServerException e) {
            LOG.error("Error registration usage of resources by process {} in workspace {}", metric.getRunId(), metric.getWorkspaceId());
        }
    }

    public void resourceInUse(long processId) {
        final UsageInformer usageInformer = inMemoryStorage.get(processId);

        if (usageInformer == null) {
            return;
        }

        try {
            usageInformer.resourceInUse();
        } catch (ServerException e) {
            LOG.warn("Can't register usage of resources by run task {}. ", processId, e.getMessage());
        }
    }

    public void resourceUsageStopped(long processId) {
        final UsageInformer removed = inMemoryStorage.remove(processId);

        if (removed == null) {
            return;
        }

        try {
            removed.resourceUsageStopped();
        } catch (ServerException e) {
            LOG.warn("Can't register end of resource usage by run task {}", processId);
        }
    }
}
