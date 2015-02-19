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
package com.codenvy.api.account.billing;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Plan;
import com.braintreegateway.PlanGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionGateway;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.BillingCycleType;
import com.codenvy.api.account.shared.dto.SubscriptionState;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.schedule.ScheduleDelay;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link com.codenvy.api.account.billing.BraintreePaymentService}
 *
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class BraintreePaymentServiceTest {
    private static final String     SUBSCRIPTION_ID = "subscriptionId";
    private static final String     PLAN_ID         = "planId";
    private static final String     PAYMENT_TOKEN   = "ptoken";
    private static final BigDecimal PRICE           = new BigDecimal(10);

    @Mock
    private BraintreeGateway   gateway;
    @Mock
    private PlanGateway        planGateway;
    @Mock
    private Plan               plan;
    @Mock
    private TransactionGateway transactionGateway;
    @Mock
    private Result             result;
    @Mock
    private Transaction        transaction;

    @InjectMocks
    private BraintreePaymentService service;

    @BeforeMethod
    public void setUp() throws Exception {
        Field pricesField = BraintreePaymentService.class.getDeclaredField("prices");
        pricesField.setAccessible(true);
        pricesField.set(service, Collections.emptyMap());
    }

    @Test
    public void shouldBeAbleToChargeSubscription() throws Exception {
        prepareSuccessfulCharge();

        service.charge(createSubscription());

        verify(transactionGateway).sale(any(TransactionRequest.class));
    }

    @Test(expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "No subscription information provided")
    public void shouldThrowForbiddenExceptionIfSubscriptionToChargeIsNull() throws Exception {
        prepareSuccessfulCharge();

        service.charge((Subscription)null);
    }

    @Test(expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "Subscription id required")
    public void shouldThrowForbiddenExceptionIfIdIsNull() throws Exception {
        prepareSuccessfulCharge();

        service.charge(createSubscription().withId(null));
    }

    @Test(expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "Payment token required")
    public void shouldThrowForbiddenExceptionIfTokenIsNull() throws Exception {
        prepareSuccessfulCharge();

        service.charge(createSubscription().withPaymentToken(null));
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact " +
                                                                                        "support")
    public void shouldThrowServerExceptionIfPriceIsMissing() throws Exception {
        prepareSuccessfulCharge();

        Field prices = BraintreePaymentService.class.getDeclaredField("prices");
        prices.setAccessible(true);
        prices.set(service, Collections.emptyMap());

        service.charge(createSubscription());
    }

    @Test(expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "error message")
    public void shouldThrowConflictExceptionIfChargeWasUnsuccessful() throws Exception {
        prepareSuccessfulCharge();
        when(result.isSuccess()).thenReturn(false);
        when(result.getMessage()).thenReturn("error message");

        service.charge(createSubscription());

        verify(transactionGateway).sale(any(TransactionRequest.class));
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfOtherExceptionOccurs() throws Exception {
        prepareSuccessfulCharge();
        when(transactionGateway.sale(any(TransactionRequest.class))).thenThrow(new BraintreeException("message"));

        service.charge(createSubscription());

        verify(transactionGateway).sale(any(TransactionRequest.class));
    }

    @Test(timeOut = 1000)
    public void shouldHavePostConstructMethodWhichFillsPrices() throws Exception {
        when(gateway.plan()).thenReturn(planGateway);
        when(planGateway.all()).thenReturn(Arrays.asList(plan));
        when(plan.getId()).thenReturn("planId");
        when(plan.getPrice()).thenReturn(new BigDecimal(1));

        Method getPrices = BraintreePaymentService.class.getDeclaredMethod("updatePrices");
        assertTrue(getPrices.isAnnotationPresent(ScheduleDelay.class));
        getPrices.setAccessible(true);
        getPrices.invoke(service);

        Field pricesField = BraintreePaymentService.class.getDeclaredField("prices");
        pricesField.setAccessible(true);
        Map<String, BigInteger> planPricesMap;
        while (true) {
            planPricesMap = (Map<String, BigInteger>)pricesField.get(service);
            if (!planPricesMap.isEmpty()) {
                break;
            }

        }
        assertEquals(planPricesMap, Collections.singletonMap("planId", new BigDecimal(1)));
    }

    private Subscription createSubscription() {
        final HashMap<String, String> properties = new HashMap<>(4);
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        return new Subscription().withId(SUBSCRIPTION_ID)
                                 .withAccountId("test_account_id")
                                 .withPlanId(PLAN_ID)
                                 .withServiceId("test_service_id")
                                 .withProperties(properties)
                                 .withBillingCycleType(BillingCycleType.AutoRenew)
                                 .withBillingCycle(1)
                                 .withDescription("description")
                                 .withBillingContractTerm(1)
                                 .withStartDate(new Date())
                                 .withEndDate(new Date())
                                 .withBillingStartDate(new Date())
                                 .withBillingEndDate(new Date())
                                 .withNextBillingDate(new Date())
                                 .withTrialStartDate(new Date())
                                 .withTrialEndDate(new Date())
                                 .withPaymentToken(PAYMENT_TOKEN)
                                 .withState(SubscriptionState.ACTIVE)
                                 .withUsePaymentSystem(true);
    }

    private void prepareSuccessfulCharge() throws NoSuchFieldException, IllegalAccessException {
        Field prices = BraintreePaymentService.class.getDeclaredField("prices");
        prices.setAccessible(true);
        prices.set(service, Collections.singletonMap(PLAN_ID, PRICE));
        when(gateway.transaction()).thenReturn(transactionGateway);
        when(transactionGateway.sale(any(TransactionRequest.class))).thenReturn(result);
        when(result.getTarget()).thenReturn(transaction);
        when(result.isSuccess()).thenReturn(true);
    }
}