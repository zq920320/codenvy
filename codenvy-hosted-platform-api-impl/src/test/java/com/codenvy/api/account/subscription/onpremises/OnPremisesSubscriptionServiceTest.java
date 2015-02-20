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
package com.codenvy.api.account.subscription.onpremises;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;

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
 * Tests for {@link com.codenvy.api.account.subscription.onpremises.OnPremisesSubscriptionService}
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class OnPremisesSubscriptionServiceTest {
    @Mock
    private AccountDao accountDao;

    @InjectMocks
    private OnPremisesSubscriptionService service;

    @Test
    public void testBeforeCreateSubscription() throws ApiException {
        final String accountId = "acc1";
        when(accountDao.getActiveSubscription(accountId, service.getServiceId())).thenReturn(null);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withProperties(properties);

        service.beforeCreateSubscription(newSubscription);

        verify(accountDao).getActiveSubscription(accountId, service.getServiceId());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = SubscriptionService.SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE)
    public void testBeforeCreateWhenSubscriptionExists() throws ApiException {
        final String accountId = "acc1";
        when(accountDao.getActiveSubscription(accountId, service.getServiceId()))
                .thenReturn(new Subscription().withServiceId(service.getServiceId()));
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "startup");
        properties.put("Users", "5");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }
}
