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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ResourcesUsageLimitProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ResourcesUsageLimitProviderTest {
    @Mock
    ActiveTasksHolder activeTasksHolder;

    @InjectMocks
    ResourcesUsageLimitProvider provider;

    @Mock
    MeteredTask       meteredTask;
    @Mock
    ResourcesWatchdog resourcesWatchdog;

    @BeforeMethod
    public void setUp() throws Exception {
        when(resourcesWatchdog.getId()).thenReturn("watchdog_id");
        when(activeTasksHolder.getActiveWatchdogs()).thenReturn(Collections.singletonList(resourcesWatchdog));
        when(activeTasksHolder.getActiveTasks(eq("watchdog_id"))).thenReturn(Collections.singletonList(meteredTask));
    }

    @Test
    public void shouldInterruptTasksAndLockWatchdogIfLimitIsReached() throws Exception {
        when(resourcesWatchdog.isLimitedReached()).thenReturn(true);

        provider.run();

        verify(resourcesWatchdog).lock();
        verify(meteredTask).interrupt();
    }

    @Test
    public void shouldNoInterruptTasksAndNoLockWatchdogIfLimitIsReached() throws Exception {
        when(resourcesWatchdog.isLimitedReached()).thenReturn(false);

        provider.run();

        verify(resourcesWatchdog, never()).lock();
        verify(meteredTask, never()).interrupt();
    }

}
