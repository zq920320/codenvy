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
package com.codenvy.api.account.subscription.service.util;

import com.codenvy.api.account.PaymentService;
import com.codenvy.api.account.billing.CreditCardDao;
import com.codenvy.api.account.impl.shared.dto.CreditCard;
import com.codenvy.api.dao.mongo.AccountDaoImpl;
import com.codenvy.api.dao.mongo.SubscriptionQueryBuilder;

import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SubscriptionServiceHelper}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class SubscriptionServiceHelperTest {
    @Mock
    AccountDaoImpl accountDao;
    @Mock
    PaymentService paymentService;
    @Mock
    CreditCardDao  creditCardDao;

    @InjectMocks
    SubscriptionServiceHelper subscriptionServiceHelper;

    @Mock
    SubscriptionQueryBuilder                   subscriptionQueryBuilder;
    @Mock
    SubscriptionQueryBuilder.SubscriptionQuery subscriptionQuery;
    @Mock
    EnvironmentContext                         environmentContext;

    @BeforeMethod
    public void setUp() throws Exception {
        EnvironmentContext.setCurrent(environmentContext);

        when(accountDao.getSubscriptionQueryBuilder()).thenReturn(subscriptionQueryBuilder);
        when(subscriptionQueryBuilder.getTrialQuery(anyString(), anyString())).thenReturn(subscriptionQuery);
        when(subscriptionQuery.execute()).thenReturn(Collections.<Subscription>emptyList());
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "You can't add subscription without credit card")
    public void testCheckingOfCreditCardWhenSubscriptionHasNotTrialAndAccountHasNotAnyCard() throws Exception {
        when(creditCardDao.getCards(eq("acc_id"))).thenReturn(Collections.<CreditCard>emptyList());

        subscriptionServiceHelper.checkCreditCard(new Subscription().withAccountId("acc_id").withUsePaymentSystem(true));
    }

    @Test(expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "Can't add new trial. Please, contact support")
    public void shouldNotBeAbleToAddTrialIfUserHasGotTrialOfTheSameServiceBefore() throws Exception {
        prepareUserRole("user");

        when(accountDao.getSubscriptionQueryBuilder()).thenReturn(subscriptionQueryBuilder);
        when(subscriptionQueryBuilder.getTrialQuery(anyString(), anyString())).thenReturn(subscriptionQuery);
        when(subscriptionQuery.execute()).thenReturn(Collections.singletonList(new Subscription()));

        subscriptionServiceHelper.checkTrial(new Subscription().withTrialStartDate(new Date(10))
                                                               .withTrialEndDate(new Date(20)));
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Can't add subscription. Please, contact support")
    public void shouldRespondServerErrorIfServerExceptionIsThrownOnCheckTrialHistory() throws Exception {
        prepareUserRole("user");

        when(accountDao.getSubscriptionQueryBuilder()).thenReturn(subscriptionQueryBuilder);
        when(subscriptionQueryBuilder.getTrialQuery(anyString(), anyString())).thenReturn(subscriptionQuery);
        when(subscriptionQuery.execute()).thenThrow(new ServerException(""));

        subscriptionServiceHelper.checkTrial(new Subscription().withTrialStartDate(new Date(10))
                                                               .withTrialEndDate(new Date(20)));
    }

    @Test
    public void shouldNotCheckTrialHistoryIfUserIsAdmin() throws Exception {
        prepareUserRole("system/admin");

        subscriptionServiceHelper.checkTrial(new Subscription().withTrialStartDate(new Date(10))
                                                               .withTrialEndDate(new Date(20)));

        verifyZeroInteractions(accountDao);
    }

    @Test
    public void shouldNotCheckTrialHistoryIfSubscriptionDoesNotHaveTrial() throws Exception {
        subscriptionServiceHelper.checkTrial(new Subscription());

        verifyZeroInteractions(accountDao);
    }

    private void prepareUserRole(String role) {
        Set<String> roles = new HashSet<>();
        roles.add(role);
        when(environmentContext.getUser()).thenReturn(new UserImpl("user", "user_id", "token", roles, false));
    }
}
