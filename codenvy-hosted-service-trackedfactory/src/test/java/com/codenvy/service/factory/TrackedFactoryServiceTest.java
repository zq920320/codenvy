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
package com.codenvy.service.factory;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.service.factory.TrackedFactoryService}
 *
 * @author Sergii Kabashniuk
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class TrackedFactoryServiceTest {

    private SubscriptionService service;
    @Mock
    private AccountDao          accountDao;

    @BeforeClass
    public void initialize() {
        service = new TrackedFactoryService(accountDao);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "TrackedFactory subscription already exists")
    public void beforeCreateSubscriptionWhenOneAlreadyExists() throws ApiException {
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(DtoFactory.getInstance().createDto(Subscription.class)
                                           .withServiceId(service.getServiceId()));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);

        final Subscription newSubscription = DtoFactory.getInstance().createDto(Subscription.class)
                                                       .withServiceId(service.getServiceId())
                                                       .withAccountId(accountId);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void beforeCreateSubscription() throws ApiException {
        final String accountId = "acc1";
        final Subscription newSubscription = DtoFactory.getInstance().createDto(Subscription.class)
                                                       .withServiceId(service.getServiceId())
                                                       .withAccountId(accountId);
        service.beforeCreateSubscription(newSubscription);
    }
}
