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
package com.codenvy.api.subscription.onpremises.server;


import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.server.dao.Subscription;

import org.eclipse.che.api.core.notification.EventService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OnPremisesSubscriptionSubscriber}
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class OnPremisesSubscriptionSubscriberTest {
    @Mock
    EventService eventService;

    @Mock
    SubscriptionMailSender subscriptionMailSender;

    @Mock
    SubscriptionEvent subscriptionEvent;

    @Mock
    Subscription subscription;

    @InjectMocks
    OnPremisesSubscriptionSubscriber subscriber;

    @BeforeMethod
    public void setUp() {
        when(subscriptionEvent.getType()).thenReturn(SubscriptionEvent.EventType.ADDED);
        when(subscriptionEvent.getSubscription()).thenReturn(subscription);
        when(subscription.getServiceId()).thenReturn("OnPremises");
        when(subscription.getPlanId()).thenReturn("opm-free");
        when(subscription.getAccountId()).thenReturn("accountId");
    }

    @Test
    public void shouldNotSendOnPremisesNotificationWhenSubscriptionIsNotForOnPremises() {
        when(subscription.getServiceId()).thenReturn("Saas");

        subscriber.onEvent(subscriptionEvent);

        verifyZeroInteractions(subscriptionMailSender);
    }

    @Test
    public void shouldSendFreeOnPremisesNotification() {
        subscriber.onEvent(subscriptionEvent);

        verify(subscriptionMailSender).sendFreeOnPremisesSubscriptionNotification("accountId");
    }

    @Test
    public void shouldSendPaidOnPremisesNotification() {
        when(subscription.getPlanId()).thenReturn("paid-subscription");

        subscriber.onEvent(subscriptionEvent);

        verify(subscriptionMailSender).sendPaidOnPremisesSubscriptionNotification(subscription);
    }

    @Test
    public void shouldSendOnPremisesSubscriptionExpiredNotification() {
        when(subscriptionEvent.getType()).thenReturn(SubscriptionEvent.EventType.REMOVED);

        subscriber.onEvent(subscriptionEvent);

        verify(subscriptionMailSender).sendEmailAboutExpired("accountId");
    }


}
