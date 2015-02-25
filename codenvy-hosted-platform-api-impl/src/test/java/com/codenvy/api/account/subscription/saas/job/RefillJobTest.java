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
package com.codenvy.api.account.subscription.saas.job;

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.api.account.subscription.saas.job.RefillJob}.
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RefillJobTest {
    @Mock
    AccountDao    accountDao;
    @Mock
    AccountLocker accountLocker;

    @InjectMocks
    RefillJob refillJob;

    @Test
    public void shouldRefillResources() throws Exception {
        final Account lockedAccountId = new Account().withId("lockedAccountId");
        when(accountDao.getAccountsWithLockedResources()).thenReturn(Arrays.asList(lockedAccountId));

        refillJob.run();

        verify(accountLocker).unlockAccountResources(eq("lockedAccountId"));
    }

    @Test
    public void shouldNotRefillResourcesForPaidLockedAccount() throws Exception {
        final Account lockedAccountId = new Account().withId("lockedAccountId");
        lockedAccountId.getAttributes().put(Constants.PAYMENT_LOCKED_PROPERTY, "true");
        when(accountDao.getAccountsWithLockedResources()).thenReturn(Arrays.asList(lockedAccountId));

        refillJob.run();

        verifyZeroInteractions(accountLocker);
    }
}
