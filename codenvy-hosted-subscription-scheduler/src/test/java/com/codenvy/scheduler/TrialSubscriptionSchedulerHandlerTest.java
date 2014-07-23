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
package com.codenvy.scheduler;

import com.codenvy.api.account.server.PaymentService;
import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionHistoryEvent;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ServerException;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.api.account.server.dao.Subscription.State.ACTIVE;
import static com.codenvy.api.account.server.dao.Subscription.State.WAIT_FOR_PAYMENT;
import static com.codenvy.api.account.server.dao.SubscriptionHistoryEvent.Type.CREATE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test for {@link TrialSubscriptionSchedulerHandler}
 *
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class TrialSubscriptionSchedulerHandlerTest {
    public static final String SERVICE_ID = "service id";
    public static final String ID         = "id";

    @Mock
    private AccountDao accountDao;

    @Mock
    private SubscriptionServiceRegistry registry;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private PaymentService paymentService;

    private TrialSubscriptionSchedulerHandler handler;

    @BeforeMethod
    public void setUp() throws Exception {
        handler = new TrialSubscriptionSchedulerHandler(registry, paymentService, accountDao);
    }

//    @Test
    public void shouldBeAbleToPurchaseASubscriptionOnCheckSubscriptionIfTrialIsExpired() throws ApiException {
        final SubscriptionHistoryEvent event = new SubscriptionHistoryEvent().withType(
                CREATE).withSubscription(new Subscription().withId(ID)).withUserId("user id");

        final Map<String, String> properties = new HashMap<>();
        properties.put("codenvy:trial", "true");
        properties.put("TariffPlan", "yearly");

        final Subscription subscription = new Subscription().withId(ID).withServiceId(
                SERVICE_ID).withState(ACTIVE).withProperties(properties).withStartDate(System.currentTimeMillis()).withEndDate(
                System.currentTimeMillis());

        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(accountDao.getSubscriptionHistoryEvents(any(SubscriptionHistoryEvent.class))).thenReturn(Arrays.asList(event));

        handler.checkSubscription(subscription);

        verify(accountDao).updateSubscription(argThat(new ArgumentMatcher<Subscription>() {
            @Override
            public boolean matches(Object argument) {
                Subscription actual = (Subscription)argument;

                return WAIT_FOR_PAYMENT.equals(actual.getState()) && ID.equals(actual.getId()) &&
                       SERVICE_ID.equals(actual.getServiceId()) && "yearly".equals(actual.getProperties().get("TariffPlan")) &&
                       "true".equals(actual.getProperties().get("codenvy:trial"));
            }
        }));
        verify(accountDao).addSubscriptionHistoryEvent(any(SubscriptionHistoryEvent.class));
        verify(subscriptionService).onUpdateSubscription(argThat(new ArgumentMatcher<Subscription>() {
            @Override
            public boolean matches(Object argument) {
                Subscription actual = (Subscription)argument;
                return ACTIVE.equals(actual.getState());
            }
        }), argThat(new ArgumentMatcher<Subscription>() {
            @Override
            public boolean matches(Object argument) {
                Subscription actual = (Subscription)argument;
                return WAIT_FOR_PAYMENT.equals(actual.getState());
            }
        }));
        verify(paymentService).purchase("user id", ID);

    }

    @Test
    public void shouldNotDoAnythingOnCheckSubscriptionIfSubscriptionIsNotTrial() throws ApiException {
        final Subscription subscription = new Subscription().withId(ID).withServiceId(
                SERVICE_ID).withState(ACTIVE).withStartDate(System.currentTimeMillis())
                                                            .withEndDate(System.currentTimeMillis());

        handler.checkSubscription(subscription);

        verifyZeroInteractions(paymentService, accountDao, registry);
        verifyZeroInteractions(accountDao, paymentService);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription service not found " + SERVICE_ID)
    public void shouldThrowConflictExceptionIfServiceIdIsUnknown() throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(null);
        final Subscription subscription = new Subscription().withId(ID).withServiceId(
                SERVICE_ID).withState(ACTIVE).withProperties(Collections.singletonMap("codenvy:trial", "true"))
                                                            .withStartDate(System.currentTimeMillis())
                                                            .withEndDate(System.currentTimeMillis());

        handler.checkSubscription(subscription);
        verifyZeroInteractions(accountDao, paymentService);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Can't find creation event of subscription " + ID)
    public void shouldThrowConflictExceptionIfNoCreateSubscriptionEventWasFound() throws Exception {
        final Map<String, String> properties = new HashMap<>();
        properties.put("codenvy:trial", "true");
        properties.put("TariffPlan", "yearly");

        final Subscription subscription = new Subscription().withId(ID).withServiceId(
                SERVICE_ID).withState(ACTIVE).withProperties(properties).withStartDate(System.currentTimeMillis() - 2).withEndDate(
                System.currentTimeMillis() - 1);

        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(accountDao.getSubscriptionHistoryEvents(any(SubscriptionHistoryEvent.class))).thenReturn(
                Collections.<SubscriptionHistoryEvent>emptyList());

        handler.checkSubscription(subscription);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Error on purchasing trial subscription " + ID + ". Message: api exception message")
    public void shouldThrowServerExceptionIfApiExceptionOccursOnGetSubscriptionEvent() throws Exception {
        final Map<String, String> properties = new HashMap<>();
        properties.put("codenvy:trial", "true");
        properties.put("TariffPlan", "yearly");

        final Subscription subscription = new Subscription().withId(ID).withServiceId(
                SERVICE_ID).withState(ACTIVE).withProperties(properties).withStartDate(System.currentTimeMillis() - 2).withEndDate(
                System.currentTimeMillis() - 1);

        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(accountDao.getSubscriptionHistoryEvents(any(SubscriptionHistoryEvent.class)))
                .thenThrow(new ServerException("api exception message"));

        handler.checkSubscription(subscription);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Error on purchasing trial subscription " + ID + ". Message: api exception message")
    public void shouldThrowServerExceptionIfApiExceptionOccursOnPurchase() throws Exception {
        final SubscriptionHistoryEvent event = new SubscriptionHistoryEvent().withType(
                CREATE).withSubscription(new Subscription().withId(ID)).withUserId("user id");

        final Map<String, String> properties = new HashMap<>();
        properties.put("codenvy:trial", "true");
        properties.put("TariffPlan", "yearly");

        final Subscription subscription = new Subscription().withId(ID).withServiceId(
                SERVICE_ID).withState(ACTIVE).withProperties(properties).withStartDate(System.currentTimeMillis()).withEndDate(
                System.currentTimeMillis());

        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(accountDao.getSubscriptionHistoryEvents(any(SubscriptionHistoryEvent.class))).thenReturn(Arrays.asList(event));
        doThrow(new ServerException("api exception message")).when(paymentService).purchase(anyString(), anyString());

        handler.checkSubscription(subscription);
    }
}