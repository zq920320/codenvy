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

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionHistoryEvent;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ServerException;

import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static com.codenvy.scheduler.SubscriptionScheduler.EVENTS_INITIATOR_SCHEDULER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RemoveExpiredSubscriptionSchedulerHandler}
 *
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class RemoveExpiredSubscriptionSchedulerHandlerTest {
    public static final String SERVICE_ID = "service id";
    public static final String ID         = "id";

    @Mock
    private AccountDao accountDao;

    @Mock
    private SubscriptionServiceRegistry registry;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private RemoveExpiredSubscriptionSchedulerHandler handler;

    @Test
    public void shouldNotDoAnythingOnCheckSubscriptionIfSubscriptionIsNotExpired() throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        final Subscription subscription = new Subscription().withId(ID)
                                                            .withServiceId(SERVICE_ID)
                                                            .withEndDate(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));

        handler.checkSubscription(subscription);
    }

    @Test
    public void shouldRemoveSubscriptionIfItIsExpired() throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        final Subscription subscription = new Subscription().withId(ID)
                                                            .withServiceId(SERVICE_ID)
                                                            .withEndDate(System.currentTimeMillis() - 1);

        handler.checkSubscription(subscription);

        verify(accountDao).removeSubscription(ID);
        verify(subscriptionService).onRemoveSubscription(eq(subscription));
        verify(accountDao).addSubscriptionHistoryEvent(argThat(new ArgumentMatcher<SubscriptionHistoryEvent>() {
            @Override
            public boolean matches(Object argument) {
                SubscriptionHistoryEvent event = (SubscriptionHistoryEvent)argument;
                if (event.getType() == SubscriptionHistoryEvent.Type.DELETE && event.getSubscription().equals(subscription) &&
                    event.getUserId().equals(EVENTS_INITIATOR_SCHEDULER)) {
                    return true;
                }
                return false;
            }
        }));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription service not found " + SERVICE_ID)
    public void shouldThrowConflictExceptionIfServiceIdIsUnknown() throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(null);
        final Subscription subscription = new Subscription().withId(ID)
                                                            .withServiceId(SERVICE_ID)
                                                            .withEndDate(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));

        handler.checkSubscription(subscription);
    }

    @Test(expectedExceptionsMessageRegExp = "Error on removing subscription " + ID + ". Message: exception",
          expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionIfApiExceptionIsThrownInOnRemoveSubscription()
            throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        doThrow(new ServerException("exception")).when(subscriptionService).onRemoveSubscription(any(Subscription.class));
        final Subscription subscription = new Subscription().withId(ID)
                                                            .withServiceId(SERVICE_ID)
                                                            .withEndDate(System.currentTimeMillis() - 1);

        handler.checkSubscription(subscription);

        verify(accountDao).removeSubscription(ID);
    }

    @Test(expectedExceptionsMessageRegExp = "Error on removing subscription " + ID + ". Message: exception",
          expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionIfApiExceptionIsThrownInOnHistoryEventAddition()
            throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        doThrow(new ServerException("exception")).when(accountDao).addSubscriptionHistoryEvent(any(SubscriptionHistoryEvent.class));
        final Subscription subscription = new Subscription().withId(ID)
                                                            .withServiceId(SERVICE_ID)
                                                            .withEndDate(System.currentTimeMillis() - 1);

        handler.checkSubscription(subscription);

        verify(accountDao).removeSubscription(ID);
    }

    @Test(expectedExceptionsMessageRegExp = "Error on removing subscription " + ID + ". Message: exception",
          expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionIfApiExceptionIsThrownOnRemoveSubscription()
            throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        doThrow(new ServerException("exception")).when(accountDao).removeSubscription(ID);
        final Subscription subscription = new Subscription().withId(ID)
                                                            .withServiceId(SERVICE_ID)
                                                            .withEndDate(System.currentTimeMillis() - 1);

        handler.checkSubscription(subscription);

        verify(accountDao).removeSubscription(ID);
    }
}