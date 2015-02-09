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

import com.codenvy.api.account.billing.MonthlyBillingPeriod;
import com.codenvy.api.builder.BuildQueue;
import com.codenvy.api.builder.BuildQueueTask;
import com.codenvy.api.builder.dto.BaseBuilderRequest;
import com.codenvy.api.builder.dto.BuildRequest;
import com.codenvy.api.builder.dto.DependencyRequest;
import com.codenvy.api.builder.internal.BuilderEvent;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.api.account.metrics.BuildStatusSubscriber}
 * @author Max Shaposhnik
 *
 */
@Listeners(MockitoTestNGListener.class)
public class BuildStatusSubscriberTest {
    private static final long   PROCESS_ID = 1L;
    private static final String WS_ID      = "workspaceId";

    @Mock
    EventService          eventService;
    @Mock
    WorkspaceDao          workspaceDao;
    @Mock
    BuildQueue            buildQueue;
    @Mock
    ResourcesUsageTracker resourcesUsageTracker;
    @Mock
    BuildRequest          buildRequest;
    @Mock
    BuildQueueTask        buildQueueTask;

    BuildStatusSubscriber buildStatusSubscriber;

    @BeforeMethod
    public void setUp() throws Exception {
        buildStatusSubscriber = new BuildStatusSubscriber(10, eventService, workspaceDao, buildQueue, resourcesUsageTracker,
                                                        new MonthlyBillingPeriod());
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withAccountId("accountId")
                                                                          .withId(WS_ID));

        when(buildRequest.getUserId()).thenReturn("userId");

        when(buildQueueTask.getRequest()).thenReturn(buildRequest);

        when(buildQueue.getTask(anyLong())).thenReturn(buildQueueTask);
    }

    @Test
    public void shouldCreateMemoryUsedRecordWhenBuildStarted() throws ServerException {
        buildStatusSubscriber.onEvent(BuilderEvent.beginEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStarted(argThat(new ArgumentMatcher<MemoryUsedMetric>() {
            @Override
            public boolean matches(Object argument) {
                final MemoryUsedMetric memoryUsedMetric = (MemoryUsedMetric)argument;
                return memoryUsedMetric.getRunId().equals(BuildTasksActivityChecker.PFX + String.valueOf(PROCESS_ID))
                       && memoryUsedMetric.getUserId().equals("userId")
                       && memoryUsedMetric.getAccountId().equals("accountId")
                       && memoryUsedMetric.getAmount() == 1536;
            }
        }));
    }

    @Test
    public void shouldNotCreateMemoryUsedRecordWhenDependencyResolvingStarted() throws ServerException {
        BaseBuilderRequest myRequest = mock(DependencyRequest.class);
        when(buildQueueTask.getRequest()).thenReturn(myRequest);
        buildStatusSubscriber.onEvent(BuilderEvent.beginEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker, never()).resourceUsageStarted(any(MemoryUsedMetric.class));
    }

    @Test
    public void shouldCreateMemoryUsedRecordWithMinValueForBuildDuration() throws ServerException {
        buildStatusSubscriber.onEvent(BuilderEvent.beginEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStarted(argThat(new ArgumentMatcher<MemoryUsedMetric>() {
            @Override
            public boolean matches(Object argument) {
                final MemoryUsedMetric memoryUsedMetric = (MemoryUsedMetric)argument;
                return memoryUsedMetric.getStopTime() - memoryUsedMetric.getStartTime() == 10;
            }
        }));
    }

    @Test
    public void shouldStopUsingResourcesWhenBuildStopped() throws ServerException {
        buildStatusSubscriber.onEvent(BuilderEvent.doneEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStopped(eq(BuildTasksActivityChecker.PFX + PROCESS_ID));
    }
}
