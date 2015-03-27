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

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.Period;
import com.codenvy.api.account.billing.ResourcesFilter;
import com.codenvy.api.account.impl.shared.dto.AccountResources;
import com.codenvy.api.account.subscription.ServiceId;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ResourcesChecker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ResourcesCheckerTest {
    private static final String ACCOUNT_ID = "ACC_ID";
    private static final Date   START_DATE = new Date(12345);
    @Mock
    AccountDao     accountDao;
    @Mock
    BillingService billingService;
    @Mock
    BillingPeriod  billingPeriod;

    @InjectMocks
    ResourcesChecker resourcesChecker;

    @Mock
    Period period;

    @BeforeMethod
    public void setUp() {
        when(billingPeriod.getCurrent()).thenReturn(period);

        when(period.getStartDate()).thenReturn(START_DATE);
    }

    @Test
    public void shouldHasAvailableResourcesIfAccountHasNotCommunitySubscription() throws Exception {
        Subscription premiumSaas = new Subscription().withPlanId("premium-plan");
        when(accountDao.getActiveSubscription(eq(ACCOUNT_ID), eq(ServiceId.SAAS))).thenReturn(premiumSaas);

        boolean hasAvailableResources = resourcesChecker.hasAvailableResources(ACCOUNT_ID);

        assertTrue(hasAvailableResources);
    }

    @Test
    public void shouldHasAvailableResourcesIfAccountHasCommunitySubscriptionAndDoesNotHasPaidGbH() throws Exception {
        Subscription premiumSaas = new Subscription().withPlanId("sas-community");
        when(accountDao.getActiveSubscription(eq(ACCOUNT_ID), eq(ServiceId.SAAS))).thenReturn(premiumSaas);
        when(billingService.getEstimatedUsageByAccount((ResourcesFilter)anyObject())).thenReturn(Collections.<AccountResources>emptyList());

        boolean hasAvailableResources = resourcesChecker.hasAvailableResources(ACCOUNT_ID);

        assertTrue(hasAvailableResources);
    }

    @Test
    public void shouldHasAvailableResourcesIfAccountHasCommunitySubscriptionAndHasPaidGbH() throws Exception {
        Subscription premiumSaas = new Subscription().withPlanId("sas-community");
        when(accountDao.getActiveSubscription(eq(ACCOUNT_ID), eq(ServiceId.SAAS))).thenReturn(premiumSaas);

        when(billingService.getEstimatedUsageByAccount((ResourcesFilter)anyObject()))
                .thenReturn(Arrays.asList(DtoFactory.getInstance().createDto(AccountResources.class)));

        boolean hasAvailableResources = resourcesChecker.hasAvailableResources(ACCOUNT_ID);

        assertFalse(hasAvailableResources);
    }
}
