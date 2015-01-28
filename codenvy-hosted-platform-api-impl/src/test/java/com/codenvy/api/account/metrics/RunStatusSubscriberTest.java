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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codenvy.api.account.server.MemoryUsedMetric;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.RunQueueTask;
import com.codenvy.api.runner.dto.RunRequest;
import com.codenvy.api.runner.internal.RunnerEvent;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link com.codenvy.api.account.metrics.RunStatusSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RunStatusSubscriberTest {
    private static final long   PROCESS_ID = 1;
    private static final String WS_ID      = "workspaceId";

    @Mock
    EventService          eventService;
    @Mock
    WorkspaceDao          workspaceDao;
    @Mock
    RunQueue              runQueue;
    @Mock
    ResourcesUsageTracker resourcesUsageTracker;
    @Mock
    RunRequest            runRequest;
    @Mock
    RunQueueTask          runQueueTask;

    RunStatusSubscriber runStatusSubscriber;

    @BeforeMethod
    public void setUp() throws Exception {
        runStatusSubscriber = new RunStatusSubscriber(10, eventService, workspaceDao, runQueue, resourcesUsageTracker);
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withAccountId("accountId")
                                                                          .withId(WS_ID));

        when(runRequest.getMemorySize()).thenReturn(256);
        when(runRequest.getUserId()).thenReturn("userId");

        when(runQueueTask.getRequest()).thenReturn(runRequest);

        when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);
    }

    @Test
    public void shouldCreateMemoryUsedRecordWhenApplicationStarted() throws ServerException {
        runStatusSubscriber.onEvent(RunnerEvent.startedEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStarted(argThat(new ArgumentMatcher<MemoryUsedMetric>() {
            @Override
            public boolean matches(Object argument) {
                final MemoryUsedMetric memoryUsedMetric = (MemoryUsedMetric)argument;
                return memoryUsedMetric.getRunId().equals(String.valueOf(PROCESS_ID))
                       && memoryUsedMetric.getUserId().equals("userId")
                       && memoryUsedMetric.getAccountId().equals("accountId")
                       && memoryUsedMetric.getAmount() == 256;
            }
        }));
    }

    @Test
    public void shouldCreateMemoryUsedRecordWithMinValueForRunDuration() throws ServerException {
        runStatusSubscriber.onEvent(RunnerEvent.startedEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStarted(argThat(new ArgumentMatcher<MemoryUsedMetric>() {
            @Override
            public boolean matches(Object argument) {
                final MemoryUsedMetric memoryUsedMetric = (MemoryUsedMetric)argument;
                return memoryUsedMetric.getStopTime() - memoryUsedMetric.getStartTime() == 10;
            }
        }));
    }

    @Test
    public void shouldStopUsingResourcesWhenApplicationStopped() throws ServerException {
        runStatusSubscriber.onEvent(RunnerEvent.stoppedEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStopped(eq(PROCESS_ID));
    }
}
