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

import com.google.common.collect.ImmutableSet;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collection;

import static org.mockito.Matchers.anyObject;
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
    private static final String WATCHDOG_ID = "resourcesWatchdog";

    @Mock
    ResourcesWatchdogProvider resourcesWatchdogProvider;
    @Mock
    MeteredTask               meteredTask;
    @Mock
    ResourcesWatchdog         resourcesWatchdog;

    ActiveTasksHolder activeTasksHolder;

    @BeforeMethod
    public void setUp() throws Exception {
        when(resourcesWatchdogProvider.getId((MeteredTask)anyObject())).thenReturn(WATCHDOG_ID);
        when(resourcesWatchdogProvider.createWatchdog(Matchers.<MeteredTask>anyObject())).thenReturn(resourcesWatchdog);
        //create instance manually without @InjectMocks because it is to necessary have new instance in each test
        activeTasksHolder = new ActiveTasksHolder(ImmutableSet.of(resourcesWatchdogProvider));
    }

    @Test
    public void shouldBeAbleToAddMeteredTask() {
        activeTasksHolder.addMeteredTask(meteredTask);

        Collection<MeteredTask> activeTasks = activeTasksHolder.getActiveTasks(WATCHDOG_ID);
        assertEquals(activeTasks.size(), 1);
        assertEquals(activeTasks.toArray()[0], meteredTask);

        assertEquals(activeTasksHolder.getActiveWatchdogs().toArray()[0], resourcesWatchdog);
        assertEquals(activeTasksHolder.getWatchdog(WATCHDOG_ID), resourcesWatchdog);
    }

    @Test
    public void shouldBeAbleToRemoveMeteredTask() {
        activeTasksHolder.addMeteredTask(meteredTask);

        activeTasksHolder.removeMeteredTask(meteredTask);

        assertTrue(activeTasksHolder.getActiveTasks(WATCHDOG_ID).isEmpty());
        assertTrue(activeTasksHolder.getActiveWatchdogs().isEmpty());
        assertEquals(activeTasksHolder.getWatchdog(WATCHDOG_ID), null);
    }

    @Test
    public void shouldNotAddMeteredTaskIfResourcesWatchdogProviderReturnNullableId() {
        when(resourcesWatchdogProvider.getId(Matchers.<MeteredTask>anyObject())).thenReturn(null);

        activeTasksHolder.addMeteredTask(meteredTask);

        assertTrue(activeTasksHolder.getActiveTasks(WATCHDOG_ID).isEmpty());
        assertTrue(activeTasksHolder.getActiveWatchdogs().isEmpty());
        assertEquals(activeTasksHolder.getWatchdog(WATCHDOG_ID), null);
    }

    @Test
    public void shouldNotRemoveMeteredTaskIfResourcesWatchdogProviderReturnNullableId() {
        activeTasksHolder.addMeteredTask(meteredTask);
        when(resourcesWatchdogProvider.getId(Matchers.<MeteredTask>anyObject())).thenReturn(null);

        activeTasksHolder.removeMeteredTask(meteredTask);

        assertTrue(activeTasksHolder.getActiveTasks(WATCHDOG_ID).isEmpty());
        assertTrue(activeTasksHolder.getActiveWatchdogs().isEmpty());
        assertEquals(activeTasksHolder.getWatchdog(WATCHDOG_ID), null);
    }
}
