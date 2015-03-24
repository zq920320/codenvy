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

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuildQueueTask;
import org.eclipse.che.api.builder.dto.BaseBuilderRequest;
import org.eclipse.che.api.builder.dto.BuildRequest;
import org.eclipse.che.api.builder.dto.DependencyRequest;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;
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
 *
 * @author Max Shaposhnik
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

    BuildStatusSubscriber buildStatusSubscriber;

    @Mock
    BuildQueueTask buildQueueTask;

    @BeforeMethod
    public void setUp() throws Exception {
        buildStatusSubscriber = new BuildStatusSubscriber(10, eventService, workspaceDao, buildQueue, resourcesUsageTracker);

        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withAccountId("accountId")
                                                                          .withId(WS_ID));

        when(buildQueue.getTask(anyLong())).thenReturn(buildQueueTask);
    }

    @Test
    public void shouldCreateMemoryUsedRecordWhenMeteredBuildStarted() throws ServerException {
        when(buildQueueTask.getRequest()).thenReturn(createBuilderRequest(true));

        buildStatusSubscriber.onEvent(BuilderEvent.beginEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStarted(argThat(new ArgumentMatcher<MemoryUsedMetric>() {
            @Override
            public boolean matches(Object argument) {
                final MemoryUsedMetric memoryUsedMetric = (MemoryUsedMetric)argument;
                return memoryUsedMetric.getRunId().equals(BuildTasksActivityChecker.PFX + String.valueOf(PROCESS_ID))
                       && "userId".equals(memoryUsedMetric.getUserId())
                       && "accountId".equals(memoryUsedMetric.getAccountId())
                       && memoryUsedMetric.getAmount() == 1536;
            }
        }));
    }

    @Test
    public void shouldNotCreateMemoryUsedRecordWhenNotMeteredBuildStarted() throws ServerException {
        when(buildQueueTask.getRequest()).thenReturn(createBuilderRequest(false));

        BaseBuilderRequest myRequest = mock(DependencyRequest.class);
        when(buildQueueTask.getRequest()).thenReturn(myRequest);
        buildStatusSubscriber.onEvent(BuilderEvent.beginEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker, never()).resourceUsageStarted(any(MemoryUsedMetric.class));
    }

    @Test
    public void shouldCreateMemoryUsedRecordWithMinValueForBuildDuration() throws ServerException {
        when(buildQueueTask.getRequest()).thenReturn(createBuilderRequest(true));

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
    public void shouldStopUsingResourcesWhenMeteredBuildStopped() throws ServerException {
        when(buildQueueTask.getRequest()).thenReturn(createBuilderRequest(true));

        buildStatusSubscriber.onEvent(BuilderEvent.doneEvent(PROCESS_ID, WS_ID, "/project"));

        verify(resourcesUsageTracker).resourceUsageStopped(eq(BuildTasksActivityChecker.PFX + PROCESS_ID));
    }

    private BaseBuilderRequest createBuilderRequest(boolean metered) {
        BaseBuilderRequest baseBuilderRequest;
        if (metered) {
            baseBuilderRequest = DtoFactory.getInstance().createDto(BuildRequest.class);
        } else {
            baseBuilderRequest = DtoFactory.getInstance().createDto(DependencyRequest.class);
        }
        baseBuilderRequest.setUserId("userId");
        return baseBuilderRequest;
    }
}
