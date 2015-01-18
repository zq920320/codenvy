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
package com.codenvy.subscription.service;

import com.codenvy.api.account.server.subscription.PaymentService;
import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.subscription.service.FactorySubscriptionService}
 *
 * @author Sergii Kabashniuk
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactorySubscriptionServiceTest {

    @Mock
    private AccountDao          accountDao;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private FactorySubscriptionService service;

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = SubscriptionService.SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE)
    public void beforeCreateSubscriptionWhenOneAlreadyExists() throws ApiException {
        final String accountId = "acc1";
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("RAM", "2GB");
        properties.put("Package", "Tracked");
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId()));
        when(accountDao.getActiveSubscriptions(accountId, service.getServiceId())).thenReturn(existedSubscriptions);

        final Subscription newSubscription = new Subscription().withServiceId(service.getServiceId())
                                                               .withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void beforeCreateSubscription() throws ApiException {
        final String accountId = "acc1";
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("RAM", "2GB");
        properties.put("Package", "Tracked");
        final Subscription newSubscription = new Subscription().withServiceId(service.getServiceId())
                                                               .withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);

        verify(accountDao).getActiveSubscriptions(accountId, service.getServiceId());
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription property 'RAM' required")
    public void shouldThrowExceptionIfRAMPropertyIsMissingOnBeforeCreateSubscription() throws ApiException {
        final String accountId = "acc1";
        final Map<String, String> properties = new HashMap<>(1);
        properties.put("Package", "Tracked");
        final Subscription newSubscription = new Subscription().withServiceId(service.getServiceId())
                                                               .withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription property 'Package' required")
    public void shouldThrowExceptionIfPackagePropertyIsMissingOnBeforeCreateSubscription() throws ApiException {
        final String accountId = "acc1";
        final Map<String, String> properties = new HashMap<>(1);
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withServiceId(service.getServiceId())
                                                               .withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Package 'NotTracked' is unknown")
    public void shouldThrowExceptionIfPackagePropertyIsIllegalOnBeforeCreateSubscription() throws ApiException {
        final String accountId = "acc1";
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("RAM", "2GB");
        properties.put("Package", "NotTracked");
        final Subscription newSubscription = new Subscription().withServiceId(service.getServiceId())
                                                               .withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }
}
