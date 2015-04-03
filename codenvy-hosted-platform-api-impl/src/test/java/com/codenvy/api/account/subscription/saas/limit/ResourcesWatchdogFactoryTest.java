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

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.WorkspaceLocker;
import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.Period;
import com.codenvy.api.account.billing.ResourcesFilter;
import com.codenvy.api.account.impl.shared.dto.AccountResources;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.subscription.ServiceId;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.api.workspace.server.Constants.RESOURCES_USAGE_LIMIT_PROPERTY;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
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
    BillingPeriod     billingPeriod;
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
        when(billingPeriod.getCurrent()).thenReturn(period);
        when(period.getStartDate()).thenReturn(new Date());
    }

    @Test
    public void testReachingOfLimitWhenAccountHasCommunitySubscriptionAndPaidGhB() throws Exception {
        when(accountDao.getActiveSubscription(eq("ACC_ID"), eq(ServiceId.SAAS))).thenReturn(new Subscription().withPlanId("sas-community"));
        when(billingService.getEstimatedUsageByAccount((ResourcesFilter)anyObject()))
                .thenReturn(Collections.singletonList(DtoFactory.getInstance().createDto(AccountResources.class)));

        ResourcesWatchdog resourcesWatchdog = factory.createAccountWatchdog("ACC_ID");

        assertTrue(resourcesWatchdog.isLimitedReached());
    }

    @Test
    public void testReachingOfLimitWhenAccountHasCommunitySubscriptionAndDoesNotHasPaidGhB() throws Exception {
        when(accountDao.getActiveSubscription(eq("ACC_ID"), eq(ServiceId.SAAS))).thenReturn(new Subscription().withPlanId("sas-community"));
        when(billingService.getEstimatedUsageByAccount((ResourcesFilter)anyObject())).thenReturn(Collections.<AccountResources>emptyList());

        ResourcesWatchdog resourcesWatchdog = factory.createAccountWatchdog("ACC_ID");

        assertFalse(resourcesWatchdog.isLimitedReached());
    }

    @Test
    public void testReachingOfLimitWhenAccountHasPaidSubscriptionAndPaidGhB() throws Exception {
        when(accountDao.getActiveSubscription(eq("ACC_ID"), eq(ServiceId.SAAS))).thenReturn(new Subscription().withPlanId("super-plan"));
        when(billingService.getEstimatedUsageByAccount((ResourcesFilter)anyObject()))
                .thenReturn(Collections.singletonList(DtoFactory.getInstance().createDto(AccountResources.class)));

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
