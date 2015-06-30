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

import com.codenvy.api.metrics.server.limit.MeteredTask;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdog;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.metrics.server.period.Period;
import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.service.http.WorkspaceInfoCache;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Date;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link AccountLimitResourcesWatchdogProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class AccountLimitResourcesWatchdogProviderTest {
    @Mock
    WorkspaceInfoCache  workspaceInfoCache;
    @Mock
    BillingService      billingService;
    @Mock
    MetricPeriod        metricPeriod;
    @Mock
    SubscriptionDao     subscriptionDao;
    @Mock
    AccountLocker       accountLocker;
    @Mock
    MeteredTask         meteredTask;
    @Mock
    Period              period;
    @Mock
    WorkspaceDescriptor workspaceDescriptor;

    @InjectMocks
    AccountLimitResourcesWatchdogProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(metricPeriod.getCurrent()).thenReturn(period);
        when(period.getStartDate()).thenReturn(new Date());
        when(meteredTask.getId()).thenReturn("meteredTask");
        when(meteredTask.getWorkspaceId()).thenReturn("WS_ID");

        when(workspaceDescriptor.getId()).thenReturn("WS_ID");
        when(workspaceDescriptor.getAccountId()).thenReturn("ACC_ID");
        when(workspaceInfoCache.getById("WS_ID")).thenReturn(workspaceDescriptor);
    }

    @Test
    public void shouldNotHaveReachedLimitWhenAccountHasSaasSubscription() throws Exception {
        when(subscriptionDao.getActiveByServiceId("ACC_ID", SAAS_SUBSCRIPTION_ID)).thenReturn(new Subscription());

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldNotHaveReachedLimitWhenAccountDoesNotHaveSaasSubscriptionAndDoesNotUseFreeResources() throws Exception {
        when(billingService.hasAvailableResources(eq("ACC_ID"), anyLong(), anyLong())).thenReturn(true);

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldNotHaveReachedLimitWhenAccountDoesNotHaveSaasSubscriptionAndSomeExceptionOccursOnCheckExceedingTheLimit()
            throws Exception {
        when(billingService.hasAvailableResources(eq("ACC_ID"), anyLong(), anyLong())).thenThrow(new ServerException(""));

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
    }


    @Test
    public void shouldNotHaveReachedLimitWhenSomeExceptionOccursOnCheckActiveSubscriptionAndDoesNotUseFreeResources() throws Exception {
        when(subscriptionDao.getActiveByServiceId(anyString(), anyString())).thenThrow(new ServerException(""));
        when(billingService.hasAvailableResources(eq("ACC_ID"), anyLong(), anyLong())).thenReturn(true);

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertFalse(workspaceWatchdog.isLimitedReached());
        verify(billingService).hasAvailableResources(anyString(), anyLong(), anyLong());
    }

    @Test
    public void shouldHaveReachedLimitWhenAccountDoesNotHaveSaasSubscriptionAndUseFreeResources() throws Exception {
        when(billingService.hasAvailableResources(eq("ACC_ID"), anyLong(), anyLong())).thenReturn(false);

        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        assertTrue(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void shouldLockAccountOnWatchdogLock() throws Exception {
        ResourcesWatchdog workspaceWatchdog = provider.createWatchdog(meteredTask);

        workspaceWatchdog.lock();

        verify(accountLocker).setResourcesLock("ACC_ID");
    }
}
