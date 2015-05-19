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
package com.codenvy.api.account;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.Period;
import com.codenvy.api.account.metrics.MeterBasedStorage;

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.api.workspace.server.Constants.RESOURCES_USAGE_LIMIT_PROPERTY;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorkspaceLocker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceLockerTest {
    @Mock
    WorkspaceDao      workspaceDao;
    @Mock
    EventService      eventService;
    @Mock
    BillingPeriod     billingPeriod;
    @Mock
    MeterBasedStorage meterBasedStorage;
    @Mock
    Period            period;

    @InjectMocks
    WorkspaceLocker workspaceLocker;

    @BeforeMethod
    public void setUp() {
        when(billingPeriod.getCurrent()).thenReturn(period);
        when(period.getStartDate()).thenReturn(new Date(0));
        when(period.getEndDate()).thenReturn(new Date(10));
    }

    @Test
    public void shouldSetResourcesLockForWorkspace() throws Exception {
        Workspace workspace = new Workspace().withId("WS_ID");

        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);

        workspaceLocker.setResourcesLock("WS_ID");

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                final Workspace o1 = (Workspace)o;
                return o1.getId().equals("WS_ID") &&
                       "true".equals(o1.getAttributes().get(Constants.RESOURCES_LOCKED_PROPERTY));
            }
        }));
        verify(eventService).publish(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof WorkspaceLockEvent) {
                    final WorkspaceLockEvent workspaceLockEvent = (WorkspaceLockEvent)o;
                    return workspaceLockEvent.getType().equals(WorkspaceLockEvent.EventType.WORKSPACE_LOCKED);
                }
                return false;
            }
        }));
    }

    @Test
    public void shouldNotSetResourcesLockForWorkspaceIfItAlreadyHasResourceLock() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        Workspace workspace = new Workspace().withId("WS_ID")
                                             .withAttributes(attributes);

        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);

        workspaceLocker.setResourcesLock("WS_ID");

        verify(workspaceDao, never()).update((Workspace)anyObject());
        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldRemoveResourcesLockForWorkspace() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        Workspace workspace = new Workspace().withId("WS_ID")
                                             .withAttributes(attributes);
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);

        workspaceLocker.removeResourcesLock("WS_ID");

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                final Workspace o1 = (Workspace)o;
                return o1.getId().equals("WS_ID") &&
                       !o1.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(eventService).publish(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof WorkspaceLockEvent) {
                    final WorkspaceLockEvent workspaceLockEvent = (WorkspaceLockEvent)o;
                    return workspaceLockEvent.getType().equals(WorkspaceLockEvent.EventType.WORKSPACE_UNLOCKED);
                }
                return false;
            }
        }));
    }

    @Test
    public void shouldNotRemoveResourcesLockForWorkspaceIfItAlreadyDoesNotHaveResourceLock() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        Workspace workspace = new Workspace().withId("WS_ID")
                                             .withAttributes(attributes);

        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);

        workspaceLocker.setResourcesLock("WS_ID");

        verify(workspaceDao, never()).update((Workspace)anyObject());
        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldNotRemoveResourcesLockForWorkspaceIfItHasResourcesUsageLimitAndReachedIts() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        attributes.put(RESOURCES_USAGE_LIMIT_PROPERTY, "1");
        Workspace workspace = new Workspace().withId("WS_ID")
                                             .withAttributes(attributes);
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);
        when(meterBasedStorage.getUsedMemoryByWorkspace(eq("WS_ID"), anyLong(), anyLong())).thenReturn(2D);

        workspaceLocker.removeResourcesLock("WS_ID");

        verify(workspaceDao, never()).update((Workspace)anyObject());
        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldRemoveResourcesLockForWorkspaceIfItHasResourcesUsageLimitButDidNotReachIts() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        attributes.put(RESOURCES_USAGE_LIMIT_PROPERTY, "10");
        Workspace workspace = new Workspace().withId("WS_ID")
                                             .withAttributes(attributes);
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);
        when(meterBasedStorage.getUsedMemoryByWorkspace(eq("WS_ID"), anyLong(), anyLong())).thenReturn(2D);

        workspaceLocker.removeResourcesLock("WS_ID");

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                final Workspace o1 = (Workspace)o;
                return o1.getId().equals("WS_ID") &&
                       !o1.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(eventService).publish(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof WorkspaceLockEvent) {
                    final WorkspaceLockEvent workspaceLockEvent = (WorkspaceLockEvent)o;
                    return workspaceLockEvent.getType().equals(WorkspaceLockEvent.EventType.WORKSPACE_UNLOCKED);
                }
                return false;
            }
        }));
    }
}
