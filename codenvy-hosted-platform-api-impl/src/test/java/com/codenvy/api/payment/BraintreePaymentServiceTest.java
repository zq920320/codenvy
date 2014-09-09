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
package com.codenvy.api.payment;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.SubscriptionGateway;
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.Billing;
import com.codenvy.api.account.shared.dto.SubscriptionAttributes;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Collections;

import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link BraintreePaymentService}
 *
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class BraintreePaymentServiceTest {
    private static final String SUBSCRIPTION_ID = "subscriptionId";
    private static final String PLAN_ID         = "planId";
    private static final String PAYMENT_TOKEN   = "ptoken";

    @Mock
    private BraintreeGateway                  gateway;
    @Mock
    private SubscriptionGateway               subscriptionGateway;
    @Mock
    private Result                            result;
    @Mock
    private com.braintreegateway.Subscription btSubscription;

    @InjectMocks
    private BraintreePaymentService service;

    private Subscription           subscription;
    private SubscriptionAttributes subscriptionAttributes;

    @BeforeMethod
    public void setUp() throws Exception {
        subscription = new Subscription().withId(SUBSCRIPTION_ID).withPlanId(PLAN_ID);
        subscriptionAttributes =
                DtoFactory.getInstance().createDto(SubscriptionAttributes.class)
                          .withTrialDuration(7)
                          .withStartDate("11/12/2014")
                          .withEndDate("11/12/2015")
                          .withDescription("description")
                          .withCustom(Collections.singletonMap("key", "value"))
                          .withBilling(DtoFactory.getInstance().createDto(Billing.class)
                                                 .withStartDate("11/12/2014")
                                                 .withEndDate("11/12/2015")
                                                 .withUsePaymentSystem("true")
                                                 .withCycleType(1)
                                                 .withCycle(1)
                                                 .withContractTerm(1)
                                                 .withPaymentToken(PAYMENT_TOKEN));
    }

    @Test
    public void shouldBeAbleToAddSubscription() throws Exception {
        when(gateway.subscription()).thenReturn(subscriptionGateway);
        when(subscriptionGateway.create(Matchers.<SubscriptionRequest>any())).thenReturn(result);
        when(result.isSuccess()).thenReturn(true);
        when(result.getTarget()).thenReturn(btSubscription);
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2019, 9, 27);
        when(btSubscription.getFirstBillingDate()).thenReturn(calendar);
        when(btSubscription.getTrialDuration()).thenReturn(15);
        SubscriptionRequest expectedRequest = new SubscriptionRequest()
                .id(SUBSCRIPTION_ID)
                .paymentMethodToken(PAYMENT_TOKEN)
                .planId(PLAN_ID);
        SubscriptionAttributes expected = DtoFactory.getInstance().clone(subscriptionAttributes);
        expected.setTrialDuration(15);
        expected.getBilling().setStartDate("10/27/2019");

        SubscriptionAttributes actual = service.addSubscription(subscription, subscriptionAttributes);

        assertEquals(actual, expected);
        verify(subscriptionGateway).create(
                refEq(expectedRequest, "addOnsRequest", "billingDayOfMonth", "descriptorRequest", "discountsRequest", "firstBillingDate",
                      "hasTrialPeriod", "merchantAccountId", "neverExpires", "numberOfBillingCycles", "options", "paymentMethodNonce",
                      "price", "trialDuration", "trialDurationUnit")
                                          );
    }

    @Test(dataProvider = "missingPaymentTokenProvider", expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "No billing information provided")
    public void shouldThrowExceptionIfRequiredSubscriptionAttributesIsMissing(SubscriptionAttributes subscriptionAttributes)
            throws ServerException, ForbiddenException, ConflictException {
        service.addSubscription(subscription, subscriptionAttributes);
    }

    @DataProvider(name = "missingPaymentTokenProvider")
    public Object[][] missingPaymentTokenProvider() {
        return new Object[][]{
                {null},
                {DtoFactory.getInstance().createDto(SubscriptionAttributes.class)},
                {DtoFactory.getInstance().createDto(SubscriptionAttributes.class).withBilling(
                        DtoFactory.getInstance().createDto(Billing.class))}};
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "BraintreeMessage")
    public void shouldThrowConflictExceptionIfRequestToBTIsUnsuccessful() throws ServerException, ForbiddenException, ConflictException {
        when(gateway.subscription()).thenReturn(subscriptionGateway);
        when(subscriptionGateway.create(Matchers.<SubscriptionRequest>any())).thenReturn(result);
        when(result.isSuccess()).thenReturn(false);
        when(result.getMessage()).thenReturn("BraintreeMessage");

        service.addSubscription(subscription, subscriptionAttributes);
    }


    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursonAddSubscription() throws Exception {
        when(gateway.subscription()).thenReturn(subscriptionGateway);
        when(subscriptionGateway.create(Matchers.<SubscriptionRequest>any())).thenThrow(
                new BraintreeException("Braintree exception message"));

        service.addSubscription(subscription, subscriptionAttributes);
    }

    @Test
    public void shouldBeAbleToRemoveSubscription() throws ServerException, NotFoundException, ForbiddenException {
        when(gateway.subscription()).thenReturn(subscriptionGateway);
        when(subscriptionGateway.cancel(SUBSCRIPTION_ID)).thenReturn(result);

        service.removeSubscription(SUBSCRIPTION_ID);

        verify(subscriptionGateway).cancel(SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "Subscription id is missing")
    public void shouldThrowForbiddenExceptionIfSubscriptionIdIsNull() throws ServerException, NotFoundException, ForbiddenException {
        service.removeSubscription(null);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursOnRemoveSubscription()
            throws ServerException, NotFoundException, ForbiddenException {
        when(gateway.subscription()).thenReturn(subscriptionGateway);
        when(subscriptionGateway.cancel(SUBSCRIPTION_ID)).thenThrow(new BraintreeException("exception message"));

        service.removeSubscription(SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "exception message")
    public void shouldThrowNotFoundExceptionIfBTNotFoundExceptionOccursOnRemoveSubscription()
            throws ServerException, NotFoundException, ForbiddenException {
        when(gateway.subscription()).thenReturn(subscriptionGateway);
        when(subscriptionGateway.cancel(SUBSCRIPTION_ID))
                .thenThrow(new com.braintreegateway.exceptions.NotFoundException("exception message"));

        service.removeSubscription(SUBSCRIPTION_ID);
    }
}