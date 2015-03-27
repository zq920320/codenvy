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

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuildQueueTask;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.RunQueueTask;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ActiveTasksInterrupter}
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/19/15.
 */
@Listeners(MockitoTestNGListener.class)
public class ActiveTasksInterrupterTest {
    private static final String ACC_ID   = "accountId";
    private static final long   RUN_ID   = 1L;
    private static final long   BUILD_ID = 10L;

    @Mock
    ActiveTasksHolder activeTasksHolder;
    @Mock
    RunQueue          runQueue;
    @Mock
    BuildQueue        buildQueue;
    @Mock
    ResourcesChecker  resourcesChecker;

    @InjectMocks
    ActiveTasksInterrupter checker;

    @Mock
    RunQueueTask   runQueueTask;
    @Mock
    BuildQueueTask buildQueueTask;
    @Mock
    Interruptable  interruptable;

    @BeforeMethod
    public void setUp() throws Exception {
        when(runQueue.getTask(eq(RUN_ID))).thenReturn(runQueueTask);
        when(buildQueue.getTask(eq(BUILD_ID))).thenReturn(buildQueueTask);

        Set<String> activeAccounts = new HashSet<>();
        activeAccounts.add(ACC_ID);

        Set<Interruptable> activeTasks = new HashSet<>();
        activeTasks.add(interruptable);
        when(activeTasksHolder.getActiveTasks(eq(ACC_ID))).thenReturn(activeTasks);
        when(activeTasksHolder.getAccountsWithActiveTasks()).thenReturn(activeAccounts);
    }

    @Test
    public void shouldNotInterruptIfAccountHasAvailableResources() throws Exception {
        when(resourcesChecker.hasAvailableResources(eq(ACC_ID))).thenReturn(Boolean.TRUE);

        checker.run();

        //then
        verifyZeroInteractions(interruptable);
    }

    @Test
    public void shouldInterruptBuildAndRunIfAccountHasNotAvailableResources() throws Exception {
        when(resourcesChecker.hasAvailableResources(eq(ACC_ID))).thenReturn(Boolean.FALSE);

        checker.run();

        verify(interruptable).interrupt();
    }

}
