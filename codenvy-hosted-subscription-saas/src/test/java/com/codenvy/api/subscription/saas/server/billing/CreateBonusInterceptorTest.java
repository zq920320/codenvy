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
package com.codenvy.api.subscription.saas.server.billing;

import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.billing.BillingPeriod;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.Bonus;
import com.codenvy.api.subscription.saas.server.billing.CreateBonusInterceptor;
import com.codenvy.api.subscription.saas.server.billing.MonthlyBillingPeriod;

import org.aopalliance.intercept.MethodInvocation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Sergii Leschenko
 */
@Listeners({MockitoTestNGListener.class})
public class CreateBonusInterceptorTest {
    @Mock
    AccountLocker    accountLocker;
    @Mock
    BillingService   billingService;
    @Mock
    BillingPeriod    billingPeriod;
    @Mock
    MethodInvocation invocation;

    @InjectMocks
    CreateBonusInterceptor createBonusInterceptor;

    @BeforeMethod
    public void setUp() {
        when(billingPeriod.getCurrent()).thenReturn(new MonthlyBillingPeriod().getCurrent());
    }

    @Test
    public void shouldUnlockAccountWhenItGetsBonusAndHasAvailableResourcesAtTheMoment() throws Throwable {
        when(billingService.hasAvailableResources(eq("acc-id"), anyLong(), anyLong())).thenReturn(true);
        when(invocation.proceed()).thenReturn(new Bonus().withAccountId("acc-id"));

        createBonusInterceptor.invoke(invocation);

        verify(accountLocker).removeResourcesLock(eq("acc-id"));
    }

    @Test
    public void shouldNotUnlockAccountWhenItGetsBonusButHasNotAvailableResourcesAtTheMoment() throws Throwable {
        when(billingService.hasAvailableResources(eq("acc-id"), anyLong(), anyLong())).thenReturn(false);
        when(invocation.proceed()).thenReturn(new Bonus().withAccountId("acc-id"));

        createBonusInterceptor.invoke(invocation);

        verifyZeroInteractions(accountLocker);
    }

}
