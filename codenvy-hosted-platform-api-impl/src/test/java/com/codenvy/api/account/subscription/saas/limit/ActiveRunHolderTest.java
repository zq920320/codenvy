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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

import com.codenvy.api.account.billing.MonthlyBillingPeriod;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.RunQueueTask;
import com.codenvy.api.runner.internal.RunnerEvent;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for ActiveRunHolder.
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/19/15.
 *
 */
@Listeners(MockitoTestNGListener.class)
public class ActiveRunHolderTest {

    @Mock
    WorkspaceDao workspaceDao;

    ActiveRunHolder activeRunHolder;

    private static final String ACC_ID = "accountId";
    private static final String WS_ID  = "workspaceId";

    @BeforeMethod
    public void setUp() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID).withAccountId(ACC_ID));
        activeRunHolder = new ActiveRunHolder(workspaceDao);
    }

    @Test
    public void shouldAddAndRemoveTask() throws Exception {
        activeRunHolder.addRun(RunnerEvent.startedEvent(1L, WS_ID, "project1"));
        activeRunHolder.addRun(RunnerEvent.startedEvent(2L, WS_ID, "project2"));
        activeRunHolder.addRun(RunnerEvent.startedEvent(3L, WS_ID, "project4"));

        activeRunHolder.removeRun(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));

        Assert.assertEquals(activeRunHolder.getActiveRuns().get(ACC_ID).size(), 2);
    }

    @Test
    public void shouldRemoveAccountIfNoMoreTasks() throws Exception {
        activeRunHolder.addRun(RunnerEvent.startedEvent(1L, WS_ID, "project1"));
        activeRunHolder.addRun(RunnerEvent.startedEvent(2L, WS_ID, "project2"));

        activeRunHolder.removeRun(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));
        activeRunHolder.removeRun(RunnerEvent.startedEvent(2L, WS_ID, "project2"));

        assertFalse(activeRunHolder.getActiveRuns().containsKey(ACC_ID));
    }


    /**
     * Tests for ActiveRunRemainResourcesChecker.
     *
     * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/19/15.
     */

    @Listeners(MockitoTestNGListener.class)
    public static class ActiveRunRemainResourcesCheckerTest {

        private ActiveRunRemainResourcesChecker checker;

        private static final Integer RUN_ACTIVITY_CHECKING_PERIOD = 60;
        private static final long    FREE_LIMIT                   = 100L;
        private static final String  ACC_ID                       = "accountId";
        private static final long    PROCESS_ID                   = 1L;

        @Mock
        ActiveRunHolder   activeRunHolder;
        @Mock
        AccountDao        accountDao;
        @Mock
        MeterBasedStorage storage;
        @Mock
        RunQueue          runQueue;
        @Mock
        RunQueueTask      runQueueTask;


        @BeforeMethod
        public void setUp() throws Exception {
            this.checker =
                    new ActiveRunRemainResourcesChecker(activeRunHolder, accountDao, storage, runQueue, new MonthlyBillingPeriod(),
                                                        FREE_LIMIT);
            Map<String, Set<Long>> activeRuns = new HashMap<>();
            Set<Long> pIds = new HashSet<>();
            pIds.add(PROCESS_ID);
            activeRuns.put(ACC_ID, pIds);
            when(activeRunHolder.getActiveRuns()).thenReturn(activeRuns);
        }

        @Test
        public void shouldNotCheckOnPaidAccounts() throws ServerException, NotFoundException {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("codenvy:paid", "true");
            when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACC_ID).withAttributes(attributes));

            checker.run();
            verifyZeroInteractions(storage);
            verifyZeroInteractions(runQueue);
        }

        @Test
        public void shouldStopRunIfLimitExeeded() throws Exception {
            when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACC_ID));
            when(storage.getMemoryUsed(anyString(), anyLong(), anyLong())).thenReturn(180D);
            when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);

            checker.run();
            verify(runQueueTask, times(1)).stop();
        }

        @Test
        public void shouldNotStopRunIfLimitNotExeeded() throws Exception {
            when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACC_ID));
            when(storage.getMemoryUsed(anyString(), anyLong(), anyLong())).thenReturn(80D);
            when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);

            verifyZeroInteractions(runQueue);
        }

    }

    @Listeners(MockitoTestNGListener.class)
    public static class CheckRemainResourcesOnStopSubscriberTest {

        private static final long PROCESS_ID = 1L;

        private static final Double FREE_LIMIT = 100D;

        private static final String WS_ID = "workspaceId";

        private static final String ACC_ID = "accountId";

        @Mock
        EventService      eventService;
        @Mock
        WorkspaceDao      workspaceDao;
        @Mock
        AccountDao        accountDao;
        @Mock
        MeterBasedStorage storage;
        @Mock
        ActiveRunHolder   activeRunHolder;


        CheckRemainResourcesOnStopSubscriber subscriber;


        @BeforeMethod
        public void setUp() throws Exception {
            subscriber = new CheckRemainResourcesOnStopSubscriber(eventService, workspaceDao, accountDao, storage,
                                                                  activeRunHolder, new MonthlyBillingPeriod(), FREE_LIMIT);
            when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withAccountId("accountId")
                                                                              .withId(ACC_ID));

            when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACC_ID));
        }


        @Test
        public void shouldAddEventOnRunStarted() throws ServerException {
            subscriber.onEvent(RunnerEvent.startedEvent(PROCESS_ID, WS_ID, "/project"));
            verify(activeRunHolder, times(1)).addRun(any(RunnerEvent.class));
        }

        @Test
        public void shouldAddEventOnRunStopped() throws ServerException, NotFoundException {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("codenvy:paid", "true");
            when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACC_ID).withAttributes(attributes));

            subscriber.onEvent(RunnerEvent.stoppedEvent(PROCESS_ID, WS_ID, "/project"));
            verify(activeRunHolder, times(1)).removeRun(any(RunnerEvent.class));
        }

        @Test
        public void shouldNotUpdateAccountAndWorkspacesIfResourcesAreLeft() throws ServerException, NotFoundException,
                                                                                   ConflictException {
            when(storage.getMemoryUsed(anyString(), anyLong(), anyLong())).thenReturn(80D);
            when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(new Workspace().withAccountId("accountId")
                                                                                                 .withId(ACC_ID)));
            subscriber.onEvent(RunnerEvent.stoppedEvent(PROCESS_ID, WS_ID, "/project"));
            verify(accountDao, never()).update(any(Account.class));
            verify(workspaceDao, never()).update(any(Workspace.class));
        }

        @Test
        public void shouldUpdateAccountAndWorkspacesIfNoResourcesLeft() throws ServerException, NotFoundException,
                                                                               ConflictException {
            when(storage.getMemoryUsed(anyString(), anyLong(), anyLong())).thenReturn(120D);
            when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(new Workspace().withAccountId("accountId")
                                                                                                 .withId(ACC_ID)));
            subscriber.onEvent(RunnerEvent.stoppedEvent(PROCESS_ID, WS_ID, "/project"));
            verify(accountDao, times(1)).update((Account)argThat(new ArgumentMatcher<Object>() {
                @Override
                public boolean matches(Object o) {
                    return ((Account)o).getAttributes().get(Constants.LOCKED_PROPERTY).equals("true");
                }
            }));
            verify(workspaceDao, times(1)).update((Workspace)argThat(new ArgumentMatcher<Object>() {
                @Override
                public boolean matches(Object o) {
                    return ((Workspace)o).getAttributes().get(Constants.LOCKED_PROPERTY).equals("true");
                }
            }));
        }


    }
}
