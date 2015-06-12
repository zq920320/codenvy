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
package com.codenvy.api.subscription.saas.server.limit;

import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Periodically check the active runs and builds to make sure they not exceeded the free RAM limit.
 *
 * @author Max Shaposhnik
 * @author Sergii Leschenko
 */
@Singleton
public class ResourcesUsageLimitProvider implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ResourcesUsageLimitProvider.class);

    private final ActiveTasksHolder activeTasksHolder;

    @Inject
    public ResourcesUsageLimitProvider(ActiveTasksHolder activeTasksHolder) {
        this.activeTasksHolder = activeTasksHolder;
    }

    @ScheduleRate(period = 60)
    @Override
    public void run() {
        try {
            /*for (ResourcesWatchdog resourcesWatchdog : activeTasksHolder.getActiveWatchdogs()) {
                if (resourcesWatchdog.isLimitedReached()) {
                    resourcesWatchdog.lock();
                    for (MeteredTask meteredTask : activeTasksHolder.getActiveTasks(resourcesWatchdog.getId())) {
                        try {
                            meteredTask.interrupt();
                        } catch (Exception e) {
                            LOG.error("Can't interrupt task with id " + meteredTask.getId(), e);
                        }
                    }
                }
            }*/
        } catch (Exception e) {
            LOG.error("Error checking of resources usage limit", e);
        }
    }
}
