/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
package com.codenvy.api.subscription.server.util;

import com.codenvy.api.creditcard.server.CreditCardDao;
import com.codenvy.api.creditcard.shared.dto.CreditCard;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionQueryBuilder;
import com.codenvy.api.subscription.server.dao.mongo.SubscriptionDaoImpl;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SubscriptionServiceHelper}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class SubscriptionServiceHelperTest {
    @Mock
    SubscriptionDaoImpl subscriptionDao;
    @Mock
    CreditCardDao       creditCardDao;

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

        when(subscriptionDao.getSubscriptionQueryBuilder()).thenReturn(subscriptionQueryBuilder);
        when(subscriptionQueryBuilder.getTrialQuery(anyString(), anyString())).thenReturn(subscriptionQuery);
        when(subscriptionQuery.execute()).thenReturn(Collections.<Subscription>emptyList());
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "You can't add subscription without credit card")
    public void testCheckingOfCreditCardWhenSubscriptionHasNotTrialAndAccountHasNotAnyCard() throws Exception {
        when(creditCardDao.getCards(eq("acc_id"))).thenReturn(Collections.<CreditCard>emptyList());

        subscriptionServiceHelper.checkCreditCard(new Subscription().withAccountId("acc_id").withUsePaymentSystem(true));
    }
}
