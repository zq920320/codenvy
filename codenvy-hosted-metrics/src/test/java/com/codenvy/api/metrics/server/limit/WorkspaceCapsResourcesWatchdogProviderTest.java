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
package com.codenvy.api.metrics.server.limit;

import com.codenvy.api.metrics.server.dao.MeterBasedStorage;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.metrics.server.period.Period;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WorkspaceCapsResourcesWatchdogProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceCapsResourcesWatchdogProviderTest {
    @Mock
    MetricPeriod      metricPeriod;
    @Mock
    WorkspaceDao      workspaceDao;
    @Mock
    MeterBasedStorage meterBasedStorage;
    @Mock
    WorkspaceLocker   workspaceLocker;
    @Mock
    MeteredTask       meteredTask;

    @InjectMocks
    WorkspaceCapsResourcesWatchdogProvider provider;

    @Mock
    Period period;

    @BeforeMethod
    public void setUp() {
        when(metricPeriod.getCurrent()).thenReturn(period);
        when(period.getStartDate()).thenReturn(new Date());
        when(meteredTask.getId()).thenReturn("meteredTask");
        when(meteredTask.getWorkspaceId()).thenReturn("WS_ID");
    }

    @Test
    public void shouldNotHaveReachedLimitWhenWorkspaceDoesNotHasResourcesUsageLimit() throws Exception {
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace());

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldNotHaveReachedLimitWhenWorkspaceHasResourcesUsageLimitAndUseLessThanIt() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(RESOURCES_USAGE_LIMIT_PROPERTY, "0.1");
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace().withAttributes(attributes));
        when(meterBasedStorage.getUsedMemoryByWorkspace(eq("WS_ID"), anyLong(), anyLong())).thenReturn(0.01);

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldNotHaveReachedLimitWhenSomeExceptionOccursOnCheckingOfLimit() throws Exception {
        when(workspaceDao.getById(eq("WS_ID"))).thenThrow(new NotFoundException(""));

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldNotHaveReachedLimitWhenSomeExceptionOccursOnCheckExceedingTheLimit() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(RESOURCES_USAGE_LIMIT_PROPERTY, "0.1");
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace().withAttributes(attributes));
        when(meterBasedStorage.getUsedMemoryByWorkspace(anyString(), anyLong(), anyLong())).thenThrow(new ServerException(""));

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldHaveReachedLimitWhenWorkspaceHasResourcesUsageLimitAndUseMoreThanIt() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(RESOURCES_USAGE_LIMIT_PROPERTY, "0.1");
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace().withAttributes(attributes));
        when(meterBasedStorage.getUsedMemoryByWorkspace(eq("WS_ID"), anyLong(), anyLong())).thenReturn(0.5);

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertTrue(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldLockWorkspaceOnWatchdogLock() throws NotFoundException, ServerException {
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace());
        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        workspaceWatchdog.lock();

        verify(workspaceLocker).setResourcesLock(meteredTask.getWorkspaceId());
    }
}
