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
package com.codenvy.api.subscription.saas.server.metric;

import com.codenvy.api.subscription.saas.server.metric.BuildTasksActivityChecker;
import com.codenvy.api.subscription.saas.server.metric.ResourcesUsageTracker;

/*import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuildQueueTask;
import org.eclipse.che.api.builder.BuildStatus;
import org.eclipse.che.api.builder.dto.BuildRequest;
import org.eclipse.che.api.builder.dto.BuildTaskDescriptor;*/
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BuildTasksActivityChecker}
 *
 * @author Max Shaposhnik
 */
@Listeners(MockitoTestNGListener.class)
public class BuildTasksActivityCheckerTest {
    /*private static final long    PROCESS_ID        = 1L;
    private static final Integer TICK_PERIOD       = 200;
    private static final Integer SCHEDULING_PERIOD = 100;

    @Mock
    BuildQueue            buildQueue;
    @Mock
    ResourcesUsageTracker resourcesUsageTracker;

    @Mock
    BuildQueueTask      buildQueueTask;
    @Mock
    BuildTaskDescriptor applicationDescriptor;
    @Mock
    BuildRequest        buildRequest;
    @Mock
    ProjectDescriptor   projectDescriptor;

    BuildTasksActivityChecker tasksActivityChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        BuildTasksActivityChecker.usedTimeUnit = TimeUnit.MILLISECONDS;

        tasksActivityChecker = new BuildTasksActivityChecker(TICK_PERIOD, SCHEDULING_PERIOD, buildQueue, resourcesUsageTracker);

        BuildTaskDescriptor processDescriptor = createProcessDescriptor(PROCESS_ID, currentTimeMillis(), BuildStatus.IN_PROGRESS);
        when(buildQueueTask.getDescriptor()).thenReturn(processDescriptor);
        when(buildQueue.getTask(anyLong())).thenReturn(buildQueueTask);

        when(buildQueueTask.getRequest()).thenReturn(buildRequest);
        when(buildRequest.getProjectDescriptor()).thenReturn(projectDescriptor);
    }

    @Test
    public void shouldSendTick() throws NotFoundException, ServerException {
        when(buildQueue.getTasks()).thenReturn(new ArrayList(Arrays.asList(buildQueueTask)));

        tasksActivityChecker.check();

        verify(resourcesUsageTracker).resourceInUse(eq(BuildTasksActivityChecker.PFX + PROCESS_ID));
    }

    @Test
    public void shouldSendTicks2TimesIn2TickPeriods() throws NotFoundException, ServerException, InterruptedException {
        when(buildQueue.getTasks()).thenReturn(new ArrayList(Arrays.asList(buildQueueTask)));

        BuildTaskDescriptor buildTaskDescriptor = mock(BuildTaskDescriptor.class);
        when(buildTaskDescriptor.getStatus()).thenReturn(BuildStatus.IN_PROGRESS);
        when(buildTaskDescriptor.getTaskId()).thenReturn(PROCESS_ID);
        when(buildTaskDescriptor.getStartTime()).thenReturn(currentTimeMillis());

        when(buildQueueTask.getDescriptor()).thenReturn(buildTaskDescriptor);

        tasksActivityChecker.check();
        Thread.sleep(TICK_PERIOD);
        tasksActivityChecker.check();

        verify(resourcesUsageTracker, times(2)).resourceInUse(eq(BuildTasksActivityChecker.PFX + PROCESS_ID));
    }

    @Test
    public void shouldDistributedSendingOfTicksOnTheTimeInterval() throws NotFoundException, ServerException, InterruptedException {
        BuildQueueTask secondBuildQueueTask = mock(BuildQueueTask.class);

        when(buildQueue.getTasks()).thenReturn(new ArrayList(Arrays.asList(buildQueueTask, secondBuildQueueTask)));

        when(secondBuildQueueTask.getRequest()).thenReturn(buildRequest);

        final BuildTaskDescriptor secondDescriptor =
                createProcessDescriptor(2L, currentTimeMillis() - SCHEDULING_PERIOD, BuildStatus.IN_PROGRESS);
        when(secondBuildQueueTask.getDescriptor()).thenReturn(secondDescriptor);

        for (int i = 0; i < 3; ++i) {
            tasksActivityChecker.check();
            Thread.sleep(SCHEDULING_PERIOD);
        }

        verify(resourcesUsageTracker, times(2)).resourceInUse(eq(BuildTasksActivityChecker.PFX + PROCESS_ID));
        verify(resourcesUsageTracker).resourceInUse(eq(BuildTasksActivityChecker.PFX + 2L));
    }

    private BuildTaskDescriptor createProcessDescriptor(long processId, long startTime, BuildStatus status) {
        BuildTaskDescriptor buildTaskDescriptor = mock(BuildTaskDescriptor.class);
        when(buildTaskDescriptor.getStatus()).thenReturn(status);
        when(buildTaskDescriptor.getTaskId()).thenReturn(processId);
        when(buildTaskDescriptor.getStartTime()).thenReturn(startTime);
        return buildTaskDescriptor;
    }*/
}
