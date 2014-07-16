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
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CheckSubscriptionSchedulerHandler}
 *
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CheckSubscriptionSchedulerHandlerTest {
    public static final String SERVICE_ID = "service id";
    public static final String ID         = "id";

    @Mock
    private AccountDao accountDao;

    @Mock
    private SubscriptionServiceRegistry registry;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private CheckSubscriptionSchedulerHandler handler;

    @Test
    public void shouldCallOnCheckSubscriptionIfSubscriptionIsNotExpired() throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        final Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class).withId(ID).withServiceId(
                SERVICE_ID).withEndDate(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));

        handler.checkSubscription(subscription);

        verify(subscriptionService).onCheckSubscription(eq(subscription));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription service not found " + SERVICE_ID)
    public void shouldThrowConflictExceptionIfServiceIdIsUnknown() throws ApiException {
        when(registry.get(SERVICE_ID)).thenReturn(null);
        final Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class).withId(ID).withServiceId(
                SERVICE_ID).withEndDate(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));

        handler.checkSubscription(subscription);
    }
}