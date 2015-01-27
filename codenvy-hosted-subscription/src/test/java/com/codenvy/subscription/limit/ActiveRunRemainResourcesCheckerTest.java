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
package com.codenvy.subscription.limit;

import com.codenvy.api.account.MeterBasedStorage;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.RunQueueTask;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for ActiveRunRemainResourcesChecker.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/19/15.
 */

@Listeners(MockitoTestNGListener.class)
public class ActiveRunRemainResourcesCheckerTest {

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
        this.checker = new ActiveRunRemainResourcesChecker(activeRunHolder, accountDao, storage, runQueue, FREE_LIMIT);
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
        when(storage.getMemoryUsed(anyString(), anyLong(), anyLong())).thenReturn(180L);
        when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);

        checker.run();
        verify(runQueueTask, times(1)).stop();
    }

    @Test
    public void shouldNotStopRunIfLimitNotExeeded() throws Exception {
        when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACC_ID));
        when(storage.getMemoryUsed(anyString(), anyLong(), anyLong())).thenReturn(80L);
        when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);

        verifyZeroInteractions(runQueue);
    }

}
