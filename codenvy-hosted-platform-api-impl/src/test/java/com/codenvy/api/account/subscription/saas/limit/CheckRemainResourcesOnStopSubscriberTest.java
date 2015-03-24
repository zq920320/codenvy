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

import com.codenvy.api.account.AccountLocker;

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuildQueueTask;
import org.eclipse.che.api.builder.dto.BuildRequest;
import org.eclipse.che.api.builder.dto.DependencyRequest;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.internal.RunnerEvent;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CheckRemainResourcesOnStopSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class CheckRemainResourcesOnStopSubscriberTest {
    private static final String WS_ID  = "workspaceId";
    private static final String ACC_ID = "accountId";

    @Mock
    BuildQueue       buildQueue;
    @Mock
    ResourcesChecker resourcesChecker;
    @Mock
    AccountLocker    accountLocker;
    @Mock
    WorkspaceDao     workspaceDao;

    @InjectMocks
    CheckRemainResourcesOnStopSubscriber subscriber;

    @Mock
    BuildQueueTask buildQueueTask;

    @BeforeMethod
    public void setUp() throws Exception {
        when(workspaceDao.getById(eq(WS_ID))).thenReturn(new Workspace().withAccountId(ACC_ID)
                                                                        .withId(WS_ID));
        when(buildQueue.getTask(anyLong())).thenReturn(buildQueueTask);
    }

    @Test
    public void shouldLockAccountResourcesIfNoResourcesLeftOnMeteredBuildDoneEvent() throws Exception {
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));
        when(resourcesChecker.hasAvailableResources(eq(ACC_ID))).thenReturn(false);

        subscriber.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "/project"));

        verify(accountLocker).lockResources(eq(ACC_ID));
    }

    @Test
    public void shouldNoLockAccountResourcesIfNoResourcesLeftOnNoMeteredBuildDoneEvent() throws Exception {
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(DependencyRequest.class));
        when(resourcesChecker.hasAvailableResources(eq(ACC_ID))).thenReturn(false);

        subscriber.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "/project"));

        verifyZeroInteractions(accountLocker);
    }

    @Test
    public void shouldNoLockAccountResourcesIfResourcesLeftOnMeteredBuildDoneEvent() throws Exception {
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));
        when(resourcesChecker.hasAvailableResources(eq(ACC_ID))).thenReturn(true);

        subscriber.buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, WS_ID, "/project"));

        verifyZeroInteractions(accountLocker);
    }

    @Test
    public void shouldLockAccountResourcesIfNoResourcesLeftOnRunStopEvent() throws Exception {
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));

        when(resourcesChecker.hasAvailableResources(eq(ACC_ID))).thenReturn(false);

        subscriber.runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, WS_ID, "/project"));

        verify(accountLocker).lockResources(eq(ACC_ID));
    }

    @Test
    public void shouldNoLockAccountResourcesIfResourcesLeftOnRunStopEvent() throws Exception {
        when(buildQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(BuildRequest.class));
        when(resourcesChecker.hasAvailableResources(eq(ACC_ID))).thenReturn(true);

        subscriber.runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, WS_ID, "/project"));

        verifyZeroInteractions(accountLocker);
    }

}
