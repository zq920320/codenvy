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

import com.codenvy.api.metrics.server.limit.ActiveTasksHolder;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdog;
import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.server.dao.Subscription;

import org.eclipse.che.api.core.notification.EventService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ChangeSubscriptionSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ChangeSubscriptionSubscriberTest {
    @Mock
    EventService      eventService;
    @Mock
    ActiveTasksHolder activeTasksHolder;
    @Mock
    ResourcesWatchdog resourcesWatchdog;

    @InjectMocks
    ChangeSubscriptionSubscriber changeSubscriptionSubscriber;

    Subscription saasSubscription;

    @BeforeMethod
    public void setUp() {
        saasSubscription = new Subscription().withAccountId("ACC_ID")
                                             .withServiceId(SAAS_SUBSCRIPTION_ID);
    }

    @Test
    public void shouldStartCheckingInResourcesWatchdogIfItPresentInActiveTaskHolderOnSubscriptionAddedEvent() {
        when(activeTasksHolder.getWatchdog("ACC_ID")).thenReturn(resourcesWatchdog);

        changeSubscriptionSubscriber.onEvent(SubscriptionEvent.subscriptionAddedEvent(saasSubscription));

        verify(resourcesWatchdog).checkLimit();
    }


    @Test
    public void shouldStartCheckingInResourcesWatchdogIfItPresentInActiveTaskHolderOnSubscriptionRemovedEvent() {
        when(activeTasksHolder.getWatchdog("ACC_ID")).thenReturn(resourcesWatchdog);

        changeSubscriptionSubscriber.onEvent(SubscriptionEvent.subscriptionAddedEvent(saasSubscription));

        verify(resourcesWatchdog).checkLimit();
    }

    @Test
    public void shouldNotStartCheckingInResourcesWatchdogIfItPresentInActiveTaskHolderButSubscriptionIsNotSaas() {
        when(activeTasksHolder.getWatchdog("ACC_ID")).thenReturn(resourcesWatchdog);

        changeSubscriptionSubscriber.onEvent(SubscriptionEvent.subscriptionAddedEvent(saasSubscription.withServiceId("superService")));

        verify(resourcesWatchdog, never()).checkLimit();
    }


    @Test
    public void shouldNotStartCheckingInResourcesWatchdogIfItAbsentInActiveTaskHolderOnEvent() {
        when(activeTasksHolder.getWatchdog("ACC_ID")).thenReturn(null);

        changeSubscriptionSubscriber.onEvent(SubscriptionEvent.subscriptionRemovedEvent(saasSubscription));

        verify(resourcesWatchdog, never()).checkLimit();
    }
}
