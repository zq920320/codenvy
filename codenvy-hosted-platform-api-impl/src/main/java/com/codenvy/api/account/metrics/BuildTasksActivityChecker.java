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

import com.codenvy.api.builder.BuildQueue;
import com.codenvy.api.builder.BuildQueueTask;
import com.codenvy.api.builder.BuilderException;
import com.codenvy.api.builder.dto.BaseBuilderRequest;
import com.codenvy.api.builder.dto.BuildTaskDescriptor;
import com.codenvy.api.builder.dto.DependencyRequest;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.commons.schedule.ScheduleRate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static com.codenvy.api.builder.BuildStatus.IN_PROGRESS;
import static java.lang.System.currentTimeMillis;

/**
 * Periodically checks for build resources usage.
 *
 * @author Max Shaposhnik
 *
 */
@Singleton
public class BuildTasksActivityChecker {
    private static final Logger LOG = LoggerFactory.getLogger(BuildTasksActivityChecker.class);

    static TimeUnit usedTimeUnit = TimeUnit.SECONDS;

    /** Period between run scheduler */
    public static final String RUN_ACTIVITY_CHECKING_PERIOD = "metrics.run_activity_checking.period_sec";

    /** Period between ticks of resources use */
    public static final String RUN_TICK_PERIOD = "metrics.run_tick.period_sec";

    /** prefix to store ID in usage tracker (to avoid runner and builder ID's match) */
    public static final String PFX = "build-";

    public final static int    BUILDER_MEMORY_SIZE = 1536; // assume that builder uses 1.5GB of RAM

    private final Integer               runTickPeriod;
    private final Integer               schedulingPeriod;
    private final BuildQueue            buildQueue;
    private final ResourcesUsageTracker resourcesUsageTracker;

    @Inject
    public BuildTasksActivityChecker(@Named(RUN_TICK_PERIOD) Integer runTickPeriod,
                                     @Named(RUN_ACTIVITY_CHECKING_PERIOD) Integer schedulingPeriod,
                                     BuildQueue buildQueue,
                                     ResourcesUsageTracker resourcesUsageTracker) {
        this.runTickPeriod = runTickPeriod;
        this.buildQueue = buildQueue;
        this.resourcesUsageTracker = resourcesUsageTracker;
        this.schedulingPeriod = schedulingPeriod;
    }

    @ScheduleRate(periodParameterName = RUN_ACTIVITY_CHECKING_PERIOD)
    public void check() {
        for (BuildQueueTask task : buildQueue.getTasks()) {
            BuildTaskDescriptor descriptor;
            try {
                descriptor = task.getDescriptor();
            } catch (BuilderException e) {
                LOG.warn("Can't get task descriptor of build task {}", task.getId(), e);
                continue;
            } catch (NotFoundException e) {
                // expired task
                continue;
            }

            final BaseBuilderRequest request = task.getRequest();
            if (IN_PROGRESS.equals(descriptor.getStatus()) && isExpiredTickPeriod(descriptor.getStartTime()) &&
                !(request instanceof DependencyRequest)) {
                resourcesUsageTracker.resourceInUse(PFX + String.valueOf(descriptor.getTaskId()));
                LOG.info("EVENT#build-usage# TYPE#{}# ID#{}# MEMORY#{}# USAGE-TIME#{}#",
                         request.getProjectDescriptor().getType(),
                         descriptor.getCreationTime() + "-" + descriptor.getTaskId(),
                         BUILDER_MEMORY_SIZE,
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
