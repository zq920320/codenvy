/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.api.metrics;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.runner.ApplicationStatus;
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.RunQueueTask;
import com.codenvy.api.runner.dto.ApplicationProcessDescriptor;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.codenvy.api.runner.ApplicationStatus.RUNNING;
import static java.lang.System.currentTimeMillis;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link com.codenvy.api.metrics.TasksActivityChecker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class TasksActivityCheckerTest {
    private static final long    PROCESS_ID        = 1L;
    private static final Integer TICK_PERIOD       = 200;
    private static final Integer SCHEDULING_PERIOD = 100;

    @Mock
    RunQueue              runQueue;
    @Mock
    ResourcesUsageTracker resourcesUsageTracker;

    @Mock
    RunQueueTask                 runQueueTask;
    @Mock
    ApplicationProcessDescriptor applicationDescriptor;

    TasksActivityChecker tasksActivityChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        TasksActivityChecker.usedTimeUnit = TimeUnit.MILLISECONDS;

        tasksActivityChecker = new TasksActivityChecker(TICK_PERIOD, SCHEDULING_PERIOD, runQueue, resourcesUsageTracker);

        ApplicationProcessDescriptor processDescriptor = createProcessDescriptor(PROCESS_ID, currentTimeMillis(), RUNNING);
        when(runQueueTask.getDescriptor()).thenReturn(processDescriptor);
        when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);
    }

    @Test
    public void shouldSendTick() throws NotFoundException, ServerException {
        when(runQueue.getTasks()).thenReturn(new ArrayList(Arrays.asList(runQueueTask)));

        tasksActivityChecker.run();

        verify(resourcesUsageTracker).resourceInUse(eq(PROCESS_ID));
    }

    @Test
    public void shouldSendTicks2TimesIn2TickPeriods() throws NotFoundException, ServerException, InterruptedException {
        when(runQueue.getTasks()).thenReturn(new ArrayList(Arrays.asList(runQueueTask)));

        ApplicationProcessDescriptor applicationDescriptor = mock(ApplicationProcessDescriptor.class);
        when(applicationDescriptor.getStatus()).thenReturn(RUNNING);
        when(applicationDescriptor.getProcessId()).thenReturn(PROCESS_ID);
        when(applicationDescriptor.getStartTime()).thenReturn(currentTimeMillis());

        when(runQueueTask.getDescriptor()).thenReturn(applicationDescriptor);

        tasksActivityChecker.run();
        Thread.sleep(TICK_PERIOD);
        tasksActivityChecker.run();

        verify(resourcesUsageTracker, times(2)).resourceInUse(eq(PROCESS_ID));
    }

    @Test
    public void shouldDistributedSendingOfTicksOnTheTimeInterval() throws NotFoundException, ServerException, InterruptedException {
        RunQueueTask secondRunQueueTask = mock(RunQueueTask.class);

        when(runQueue.getTasks()).thenReturn(new ArrayList(Arrays.asList(runQueueTask, secondRunQueueTask)));

        final ApplicationProcessDescriptor secondDescriptor = createProcessDescriptor(2L, currentTimeMillis() - SCHEDULING_PERIOD, RUNNING);
        when(secondRunQueueTask.getDescriptor()).thenReturn(secondDescriptor);

        for (int i = 0; i < 3; ++i) {
            tasksActivityChecker.run();
            Thread.sleep(SCHEDULING_PERIOD);
        }

        verify(resourcesUsageTracker, times(2)).resourceInUse(eq(PROCESS_ID));
        verify(resourcesUsageTracker).resourceInUse(eq(2L));
    }

    private ApplicationProcessDescriptor createProcessDescriptor(long processId, long startTime, ApplicationStatus status) {
        ApplicationProcessDescriptor applicationDescriptor = mock(ApplicationProcessDescriptor.class);
        when(applicationDescriptor.getStatus()).thenReturn(status);
        when(applicationDescriptor.getProcessId()).thenReturn(processId);
        when(applicationDescriptor.getStartTime()).thenReturn(startTime);
        return applicationDescriptor;
    }
}
