/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.service;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Tests for {@link com.codenvy.service.OnPremisesService}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class OnPremisesServiceTest {
    private SubscriptionService service;
    @Mock
    private AccountDao          accountDao;

    @BeforeClass
    public void initialize() {
        service = new OnPremisesService(accountDao);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Tariff plan not found")
    public void testOnCreateSubscriptionWithBadSubscriptionUserCount() throws ApiException {
        final String accountId = "acc1";
        when(accountDao.getSubscriptions(accountId)).thenReturn(Collections.<Subscription>emptyList());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "1");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Tariff plan not found")
    public void testOnCreateSubscriptionWithBadSubscriptionPackage() throws ApiException {
        final String accountId = "acc1";
        when(accountDao.getSubscriptions(accountId)).thenReturn(Collections.<Subscription>emptyList());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "fake");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void testBeforeCreateSubscriptionWithNewStateWaitForPayment() throws ApiException {
        final String accountId = "acc1";
        when(accountDao.getSubscriptions(accountId)).thenReturn(Collections.<Subscription>emptyList());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void testBeforeCreateSubscriptionWithActiveState() throws ApiException {
        final String accountId = "acc1";
        when(accountDao.getSubscriptions(accountId)).thenReturn(Collections.<Subscription>emptyList());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscriptions limit exhausted")
    public void testBeforeCreateSubscriptionWithWaitForPaymentStateWhenExistsActiveState() throws ApiException {
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                   .withState(Subscription.State.ACTIVE));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription with WAIT_FOR_PAYMENT state already exists")
    public void testBeforeCreateSubscriptionWithWaitForPaymentStateWhenExistsWaitForPaymentState() throws ApiException {
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                   .withState(Subscription.State.WAIT_FOR_PAYMENT));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscriptions limit exhausted")
    public void testBeforeCreateSubscriptionWithActiveStateWhenExistsActiveState() throws ApiException {
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        final Calendar calendar = Calendar.getInstance();
        final long startDate = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        final long endDate = calendar.getTimeInMillis();
        existedSubscriptions.add(new Subscription().withState(Subscription.State.ACTIVE)
                                                   .withServiceId(service.getServiceId())
                                                   .withStartDate(startDate)
                                                   .withEndDate(endDate));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void shouldSetDatesOfStartAndEndInAccordingToSubscription() throws ApiException {
        final String accountId = "acc1";

        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);

        Calendar now = Calendar.getInstance();
        Calendar yearLater = Calendar.getInstance();
        yearLater.setTimeInMillis(now.getTimeInMillis());
        yearLater.add(Calendar.YEAR, 1);
        // may fail in debug if user spends too much time
        assertFalse(now.getTimeInMillis() - newSubscription.getStartDate() > TimeUnit.SECONDS.toMillis(1));
        assertEquals(newSubscription.getEndDate() - newSubscription.getStartDate(),
                     yearLater.getTimeInMillis() - now.getTimeInMillis());
    }

    @Test
    public void shouldSetDatesOfStartAndEndInAccordingToTrialSubscription() throws ApiException {
        final String accountId = "acc1";

        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        properties.put("codenvy:trial", String.valueOf(true));
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);

        Calendar now = Calendar.getInstance();
        Calendar trialPeriodLater = Calendar.getInstance();
        trialPeriodLater.setTimeInMillis(now.getTimeInMillis());
        trialPeriodLater.add(Calendar.DAY_OF_YEAR, 7);
        // may fail in debug if user spends too much time
        assertFalse(now.getTimeInMillis() - newSubscription.getStartDate() > TimeUnit.SECONDS.toMillis(1));
        assertEquals(newSubscription.getEndDate() - newSubscription.getStartDate(),
                     trialPeriodLater.getTimeInMillis() - now.getTimeInMillis());
    }

    @Test
    public void shouldSetStateOfTrialSubscriptionToActive() throws ApiException {
        final String accountId = "acc1";

        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        properties.put("codenvy:trial", String.valueOf(true));
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);

        assertEquals(newSubscription.getState(), Subscription.State.ACTIVE);
    }
}
