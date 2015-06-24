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

import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.WorkspaceLocker;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.metrics.server.period.Period;
import com.codenvy.api.metrics.server.dao.MeterBasedStorage;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;

import org.eclipse.che.api.account.server.dao.AccountDao;
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

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;
import static org.eclipse.che.api.workspace.server.Constants.RESOURCES_USAGE_LIMIT_PROPERTY;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ResourcesWatchdogFactoryTest {
    @Mock
    AccountDao        accountDao;
    @Mock
    SubscriptionDao   subscriptionDao;
    @Mock
    MetricPeriod      metricPeriod;
    @Mock
    BillingService    billingService;
    @Mock
    WorkspaceDao      workspaceDao;
    @Mock
    MeterBasedStorage meterBasedStorage;
    @Mock
    AccountLocker     accountLocker;
    @Mock
    WorkspaceLocker   workspaceLocker;

    @InjectMocks
    ResourcesWatchdogFactory factory;

    @Mock
    Period period;

    @BeforeMethod
    public void setUp() {
        when(metricPeriod.getCurrent()).thenReturn(period);
        when(period.getStartDate()).thenReturn(new Date());
    }

    @Test
    public void testReachingOfLimitWhenAccountDoesNotHaveSubscriptionAndPaidGhB() throws Exception {
        when(subscriptionDao.getActiveByServiceId(eq("ACC_ID"), eq(SAAS_SUBSCRIPTION_ID)))
                .thenReturn(null);
        when(billingService.hasAvailableResources(anyString(), anyLong(), anyLong())).thenReturn(false);

        ResourcesWatchdog resourcesWatchdog = factory.createAccountWatchdog("ACC_ID");

        assertTrue(resourcesWatchdog.isLimitedReached());
    }

    @Test
    public void testReachingOfLimitWhenAccountDoesNotHaveSaasSubscriptionAndDoesNotHasPaidGhB() throws Exception {
        when(subscriptionDao.getActiveByServiceId(eq("ACC_ID"), eq(SAAS_SUBSCRIPTION_ID))).thenReturn(null);
        when(billingService.hasAvailableResources(anyString(), anyLong(), anyLong())).thenReturn(true);

        ResourcesWatchdog resourcesWatchdog = factory.createAccountWatchdog("ACC_ID");

        assertFalse(resourcesWatchdog.isLimitedReached());
    }

    @Test
    public void testReachingOfLimitWhenAccountHasPaidSubscriptionAndPaidGhB() throws Exception {
        when(subscriptionDao.getActiveByServiceId(eq("ACC_ID"), eq(SAAS_SUBSCRIPTION_ID)))
                .thenReturn(new Subscription().withPlanId("super-plan"));
        when(billingService.hasAvailableResources(anyString(), anyLong(), anyLong())).thenReturn(false);

        ResourcesWatchdog resourcesWatchdog = factory.createAccountWatchdog("ACC_ID");

        assertFalse(resourcesWatchdog.isLimitedReached());
    }

    @Test
    public void testReachingOfLimitWhenWorkspaceDoesNotHasResourcesUsageLimit() throws Exception {
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace());

        ResourcesWatchdog workspaceWatchdog = factory.createWorkspaceWatchdog("WS_ID");

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void testReachingOfLimitWhenWorkspaceHasResourcesUsageLimitAndUseLessThanIt() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(RESOURCES_USAGE_LIMIT_PROPERTY, "0.1");
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace().withAttributes(attributes));
        when(meterBasedStorage.getUsedMemoryByWorkspace(eq("WS_ID"), anyLong(), anyLong())).thenReturn(0.01);

        ResourcesWatchdog workspaceWatchdog = factory.createWorkspaceWatchdog("WS_ID");

        assertFalse(workspaceWatchdog.isLimitedReached());
    }

    @Test
    public void testReachingOfLimitWhenWorkspaceHasResourcesUsageLimitAndUseMoreThanIt() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(RESOURCES_USAGE_LIMIT_PROPERTY, "0.1");
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(new Workspace().withAttributes(attributes));
        when(meterBasedStorage.getUsedMemoryByWorkspace(eq("WS_ID"), anyLong(), anyLong())).thenReturn(0.5);

        ResourcesWatchdog workspaceWatchdog = factory.createWorkspaceWatchdog("WS_ID");

        assertTrue(workspaceWatchdog.isLimitedReached());
    }
}
