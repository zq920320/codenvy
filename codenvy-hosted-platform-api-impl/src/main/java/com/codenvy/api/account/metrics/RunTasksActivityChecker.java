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

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.RunQueueTask;
import com.codenvy.api.runner.RunnerException;
import com.codenvy.api.runner.dto.ApplicationProcessDescriptor;
import com.codenvy.api.runner.dto.RunRequest;
import com.codenvy.commons.schedule.ScheduleRate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static com.codenvy.api.runner.ApplicationStatus.RUNNING;
import static java.lang.System.currentTimeMillis;

/**
 * Makes ticks about resources usage
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RunTasksActivityChecker {
    private static final Logger LOG = LoggerFactory.getLogger(RunTasksActivityChecker.class);

    static TimeUnit usedTimeUnit = TimeUnit.SECONDS;

    /** Period between run scheduler */
    public static final String RUN_ACTIVITY_CHECKING_PERIOD = "metrics.run_activity_checking.period_sec";

    /** Period between ticks of resources use */
    public static final String RUN_TICK_PERIOD = "metrics.run_tick.period_sec";

    /** prefix to store ID in usage tracker (to avoid runner and builder ID's match) */
    public static final String PFX            = "run-";

    private final Integer               runTickPeriod;
    private final Integer               schedulingPeriod;
    private final RunQueue              runQueue;
    private final ResourcesUsageTracker resourcesUsageTracker;

    @Inject
    public RunTasksActivityChecker(@Named(RUN_TICK_PERIOD) Integer runTickPeriod,
                                   @Named(RUN_ACTIVITY_CHECKING_PERIOD) Integer schedulingPeriod,
                                   RunQueue runQueue,
                                   ResourcesUsageTracker resourcesUsageTracker) {
        this.runTickPeriod = runTickPeriod;
        this.runQueue = runQueue;
        this.resourcesUsageTracker = resourcesUsageTracker;
        this.schedulingPeriod = schedulingPeriod;
    }

    @ScheduleRate(periodParameterName = RUN_ACTIVITY_CHECKING_PERIOD)
    public void check() {
        for (RunQueueTask runTask : runQueue.getTasks()) {
            ApplicationProcessDescriptor descriptor;
            try {
                descriptor = runTask.getDescriptor();
            } catch (RunnerException e) {
                LOG.warn("Can't get task descriptor of run task {}", runTask.getId(), e);
                continue;
            } catch (NotFoundException e) {
                // expired task
                continue;
            }

            final RunRequest request = runTask.getRequest();
            if (RUNNING.equals(descriptor.getStatus()) && isExpiredTickPeriod(descriptor.getStartTime())) {
                resourcesUsageTracker.resourceInUse(PFX + String.valueOf(descriptor.getProcessId()));
                LOG.info("EVENT#run-usage# WS#{}# USER#{}# PROJECT#{}# TYPE#{}# ID#{}# MEMORY#{}# USAGE-TIME#{}#",
                         descriptor.getWorkspace(),
                         descriptor.getUserId(),
                         descriptor.getProject(),
                         request.getProjectDescriptor().getType(),
                         descriptor.getCreationTime() + "-" + descriptor.getProcessId(),
                         request.getMemorySize(),
                         currentTimeMillis() - descriptor.getStartTime());
            }
        }
    }

    private boolean isExpiredTickPeriod(long startTime) {
        //Difference between current time and start time in usedTimeUnit
        final long diff = (currentTimeMillis() - startTime) / TimeUnit.MILLISECONDS.convert(1, usedTimeUnit);
        return diff % runTickPeriod < schedulingPeriod;
    }
}
