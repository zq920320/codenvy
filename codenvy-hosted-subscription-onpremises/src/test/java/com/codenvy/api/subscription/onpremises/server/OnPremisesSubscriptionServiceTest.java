/*
 *  [2012] - [2016] Codenvy, S.A.
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

import com.codenvy.api.subscription.server.AbstractSubscriptionService;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.server.util.SubscriptionServiceHelper;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OnPremisesSubscriptionService}
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class OnPremisesSubscriptionServiceTest {
    @Mock
    private SubscriptionDao           subscriptionDao;
    @Mock
    private SubscriptionServiceHelper subscriptionServiceHelper;

    @InjectMocks
    private OnPremisesSubscriptionService service;

    @Test
    public void testBeforeCreateSubscription() throws ApiException {
        final String accountId = "acc1";
        when(subscriptionDao.getActiveByServiceId(accountId, service.getServiceId())).thenReturn(null);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withProperties(properties);

        service.beforeCreateSubscription(newSubscription);

        verify(subscriptionDao).getActiveByServiceId(accountId, service.getServiceId());
    }

    @Test(expectedExceptions = ConflictException.class,
            expectedExceptionsMessageRegExp = AbstractSubscriptionService.SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE)
    public void testBeforeCreateWhenSubscriptionExists() throws ApiException {
        final String accountId = "acc1";
        when(subscriptionDao.getActiveByServiceId(accountId, service.getServiceId()))
                .thenReturn(new Subscription().withServiceId(service.getServiceId()));
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }
}
