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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
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
public class ActiveTasksInterrupter implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveTasksInterrupter.class);

    private final ActiveTasksHolder activeTasksHolder;
    private final ResourcesChecker  resourcesChecker;

    @Inject
    public ActiveTasksInterrupter(ActiveTasksHolder activeTasksHolder,
                                  ResourcesChecker resourcesChecker) {
        this.activeTasksHolder = activeTasksHolder;
        this.resourcesChecker = resourcesChecker;
    }

    @ScheduleRate(period = 60)
    @Override
    public void run() {
        for (String account : activeTasksHolder.getAccountsWithActiveTasks()) {
            try {
                if (!resourcesChecker.hasAvailableResources(account)) {
                    interruptAccountTasks(account);
                }
            } catch (NotFoundException | ServerException e) {
                LOG.error("Error check remaining resources  in account {} .", account);
            }
        }
    }

    private void interruptAccountTasks(String account) {
        for (Interruptable interruptable : activeTasksHolder.getActiveTasks(account)) {
            try {
                interruptable.interrupt();
            } catch (Exception e) {
                LOG.error("Can't interrupt task " + interruptable.getId(), e);
            }
        }
    }
}
