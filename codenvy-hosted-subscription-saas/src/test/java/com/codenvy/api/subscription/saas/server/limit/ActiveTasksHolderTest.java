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

import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.saas.server.WorkspaceResourcesUsageLimitChangedEvent;
import com.codenvy.api.subscription.server.dao.Subscription;

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuildQueueTask;
import org.eclipse.che.api.builder.dto.BuildRequest;
import org.eclipse.che.api.builder.dto.DependencyRequest;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.internal.RunnerEvent;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link ActiveTasksHolder}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ActiveTasksHolderTest {
    @Mock
    WorkspaceDao             workspaceDao;
    @Mock
    EventService             eventService;
    @Mock
    RunQueue                 runQueue;
    @Mock
    BuildQueue               buildQueue;
    @Mock
    ResourcesWatchdogFactory watchdogFactory;

    ActiveTasksHolder activeTasksHolder;

    @Mock
    BuildQueueTask    buildQueueTask;
    @Mock
    ResourcesWatchdog resourcesWatchdog;

    private static final String ACC_ID = "accountId";
    private static final String WS_ID  = "workspaceId";

    @BeforeMethod
    public void setUp() throws Exception {
        when(buildQueue.getTask(anyLong())).thenReturn(buildQueueTask);

        //create instance manually without @InjectMocks because it is to necessary have new instance in each test
        activeTasksHolder = new ActiveTasksHolder(workspaceDao, eventService, buildQueue, runQueue, watchdogFactory);
    }

    @Test
    public void shouldAddAndRemoveMeteredBuildAndRunTasks() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(2L, WS_ID, "project2"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(3L, WS_ID, "project2"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(2L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(3L, WS_ID, "project1"));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.canceledEvent(2L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.canceledEvent(2L, WS_ID, "project1"));

        assertEquals(activeTasksHolder.getActiveTasks(ACC_ID).size(), 2);
    }

    @Test
    public void shouldRemoveMeteredBuildAndRunTasksAfterRemovingOfWorkspaces() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(2L, WS_ID, "project2"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(2L, WS_ID, "project1"));

        when(workspaceDao.getById(anyString())).thenThrow(new NotFoundException("Workspace not found"));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.canceledEvent(2L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.canceledEvent(2L, WS_ID, "project1"));

        assertTrue(activeTasksHolder.getActiveTasks(ACC_ID).isEmpty());
    }

    @Test
    public void shouldRemoveMeteredBuildAndRunTasksAfterRemovingOfWorkspacesAndItsAccountIds() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(2L, WS_ID, "project2"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(2L, WS_ID, "project1"));

        activeTasksHolder.accountIdsCache.invalidateAll();

        when(workspaceDao.getById(anyString())).thenThrow(new NotFoundException("Workspace not found"));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.canceledEvent(2L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.canceledEvent(2L, WS_ID, "project1"));

        assertTrue(activeTasksHolder.getActiveTasks(ACC_ID).isEmpty());
    }

    @Test
    public void shouldNotAddNotMeteredBuildTask() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(DependencyRequest.class));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.beginEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.beginEvent(2L, WS_ID, "project2"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.beginEvent(3L, WS_ID, "project4"));

        assertTrue(activeTasksHolder.getActiveTasks(ACC_ID).isEmpty());
    }

    @Test
    public void shouldRemoveWatchdogIfNoMoreTasks() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.beginEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.startedEvent(1L, WS_ID, "project2"));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, WS_ID, "project2"));

        assertTrue(activeTasksHolder.getActiveWatchdogs().isEmpty());
        assertTrue(activeTasksHolder.getActiveTasks(ACC_ID).isEmpty());
    }

    @Test
    public void shouldNotRemoveWatchdogIfAnyTaskLeft() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(2L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(3L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(2L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(3L, WS_ID, "project1"));

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "project1"));
        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.canceledEvent(2L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.runEventSubscriber.onEvent(RunnerEvent.canceledEvent(2L, WS_ID, "project1"));

        assertEquals(activeTasksHolder.getActiveWatchdogs().size(), 2);
        assertEquals(activeTasksHolder.getActiveTasks(ACC_ID).size(), 2);
    }

    @Test
    public void shouldRecheckWatchdogLimitWhenSaasSubscriptionRemovedAndExistActiveTask() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));
        when(watchdogFactory.createAccountWatchdog(eq(ACC_ID))).thenReturn(resourcesWatchdog);

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.changeSubscriptionSubscriber.onEvent(SubscriptionEvent.subscriptionAddedEvent(new Subscription()
                                                                                                                .withServiceId(
                                                                                                                        SAAS_SUBSCRIPTION_ID)
                                                                                                                .withAccountId(ACC_ID)));

        verify(resourcesWatchdog).checkLimit();
    }

    @Test
    public void shouldRecheckWatchdogLimitWhenWorkspaceResourcesUsageLimitChanged() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID)
                                                                          .withAccountId(ACC_ID));
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));
        when(watchdogFactory.createWorkspaceWatchdog(eq(WS_ID))).thenReturn(resourcesWatchdog);

        activeTasksHolder.buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, WS_ID, "project1"));
        activeTasksHolder.changeResourceUsageLimitSubscriber.onEvent(new WorkspaceResourcesUsageLimitChangedEvent(WS_ID));

        verify(resourcesWatchdog).checkLimit();
    }
}
