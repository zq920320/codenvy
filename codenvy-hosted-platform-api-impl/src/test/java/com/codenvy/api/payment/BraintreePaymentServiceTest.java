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
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionGateway;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.Payment;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.account.shared.dto.SubscriptionHistoryEvent;
import com.codenvy.api.account.shared.dto.SubscriptionPayment;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.rest.InvalidArgumentException;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.user.User;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static com.codenvy.api.account.shared.dto.Subscription.State.ACTIVE;
import static com.codenvy.api.account.shared.dto.Subscription.State.WAIT_FOR_PAYMENT;
import static com.codenvy.api.account.shared.dto.SubscriptionHistoryEvent.Type.UPDATE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class BraintreePaymentServiceTest {
    private static final String SUBSCRIPTION_ID  = "subscriptionId";
    private static final double AMOUNT           = 1000D;
    private static final String SERVICE_ID       = "serviceId";
    private static final String CARD_NUMBER      = "4111 1111 1111 1111";
    private static final String CVV              = "111";
    private static final String CARDHOLDER       = "cardholder";
    private static final String EXPIRATION_MONTH = "11";
    private static final String EXPIRATION_YEAR  = "2014";

    @Mock
    private SubscriptionServiceRegistry registry;
    @Mock
    private AccountDao                  accountDao;
    @Mock
    private BraintreeGateway            gateway;
    @Mock
    private SubscriptionService         subscriptionService;
    @Mock
    private TransactionGateway          transactionGateway;
    @Mock
    private Result<Transaction>         result;

    @Mock
    private Transaction              transaction;
    private Subscription             subscription;
    private Payment                  payment;
    private SubscriptionHistoryEvent expectedEvent;

    @InjectMocks
    private BraintreePaymentService service;

    @BeforeMethod
    public void setUp() throws Exception {
        subscription = DtoFactory.getInstance().createDto(Subscription.class).withId(SUBSCRIPTION_ID).withServiceId(SERVICE_ID)
                                 .withAccountId("ACCOUNT_ID").withState(WAIT_FOR_PAYMENT);

        SubscriptionPayment subscriptionPayment =
                DtoFactory.getInstance().createDto(SubscriptionPayment.class).withTransactionId("TRANSACTION_ID").withAmount(AMOUNT);

        expectedEvent = DtoFactory.getInstance().createDto(SubscriptionHistoryEvent.class).withUserId(User.ANONYMOUS.getId())
                                  .withType(
                                          UPDATE).withSubscription(
                        DtoFactory.getInstance().clone(subscription).withState(ACTIVE)).withSubscriptionPayment(subscriptionPayment);

        payment = DtoFactory.getInstance().createDto(Payment.class).withSubscriptionId(SUBSCRIPTION_ID).withCardNumber(CARD_NUMBER)
                            .withCvv(CVV).withCardholderName(CARDHOLDER).withExpirationMonth(EXPIRATION_MONTH)
                            .withExpirationYear(EXPIRATION_YEAR);

        when(gateway.transaction()).thenReturn(transactionGateway);
        EnvironmentContext.getCurrent().setUser(User.ANONYMOUS);
    }

    @Test
    public void shouldBeAbleToPurchaseSubscription() throws Exception {
        final TransactionRequest expectedTransactionRequest = new TransactionRequest()
                .amount(new BigDecimal(AMOUNT))
                .creditCard()
                .number(CARD_NUMBER)
                .cvv(CVV)
                .expirationMonth(EXPIRATION_MONTH)
                .expirationYear(EXPIRATION_YEAR)
                .cardholderName(CARDHOLDER)
                .done()
                        // every custom field should be added on braintree side
                .customField("payment_reason", "addSubscription")
                .customField("payment_reason_id", SUBSCRIPTION_ID)
                .options()
                .submitForSettlement(true)
                .done();
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale(any(TransactionRequest.class))).thenReturn(result);
        when(result.isSuccess()).thenReturn(true);
        when(result.getTarget()).thenReturn(transaction);
        when(transaction.getId()).thenReturn("TRANSACTION_ID");

        service.purchase(payment);

        // verify custom fields, submit for settlement, amount.
        // TODO Need more difficult comparison to verify credit card data
        verify(transactionGateway).sale(refEq(expectedTransactionRequest, "billingAddressRequest", "creditCardRequest", "customerRequest",
                                              "descriptorRequest", "shippingAddressRequest", "transactionOptionsRequest"));
        verify(accountDao).updateSubscription(argThat(new ArgumentMatcher<Subscription>() {
            @Override
            public boolean matches(Object argument) {
                Subscription actualSubscription = (Subscription)argument;
                return DtoFactory.getInstance().clone(subscription).withState(ACTIVE).equals(actualSubscription);
            }
        }));
        verify(accountDao).addSubscriptionHistoryEvent(argThat(new ArgumentMatcher<SubscriptionHistoryEvent>() {
            @Override
            public boolean matches(Object argument) {
                SubscriptionHistoryEvent actualSubscriptionEvent = (SubscriptionHistoryEvent)argument;
                return expectedEvent.equals(actualSubscriptionEvent.withId(null).withTime(0));
            }
        }));
        verify(subscriptionService).onUpdateSubscription(subscription, DtoFactory.getInstance().clone(subscription).withState(ACTIVE));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "BraintreeMessage")
    public void shouldThrowConflictExceptionIfSubscriptionPurchaseIsUnsuccessful() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale((com.braintreegateway.TransactionRequest)any())).thenReturn(result);
        when(result.isSuccess()).thenReturn(false);
        when(result.getMessage()).thenReturn("BraintreeMessage");

        service.purchase(payment);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support",
          dataProvider = "getSubscriptionExceptionProvider")
    public void shouldThrowServerExceptionIfApiExceptionOccursOnGetSubscription(ApiException e) throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenThrow(e);

        service.purchase(payment);
    }

    @DataProvider(name = "getSubscriptionExceptionProvider")
    public ApiException[][] getSubscriptionExceptionProvider() {
        return new ApiException[][]{{new NotFoundException("")},
                                    {new ServerException("")}
        };
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support",
          dataProvider = "tariffExceptionProvider")
    public void shouldThrowServerExceptionIfApiExceptionOccursOnTariff(ApiException e) throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenThrow(e);

        service.purchase(payment);
    }

    @DataProvider(name = "tariffExceptionProvider")
    public ApiException[][] tariffExceptionProvider() {
        return new ApiException[][]{{new ApiException("")},
                                    {new ServerException("")},
                                    {new ConflictException("")},
                                    {new InvalidArgumentException("")},
                                    {new ForbiddenException("")}
        };
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Subscription service not found " + SERVICE_ID)
    public void shouldThrowServerExceptionIfUnknownServiceIdIsUsed() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(null);

        service.purchase(payment);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Payment not required")
    public void shouldThrowConflictExceptionIfSubscriptionAmountIs0() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(0D);

        service.purchase(payment);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Payment not required")
    public void shouldThrowConflictExceptionIfSubscriptionIsActive() throws Exception {
        Subscription subscription = DtoFactory.getInstance().clone(this.subscription).withState(ACTIVE);
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(1000D);

        service.purchase(payment);
    }

    @Test(dataProvider = "updateSubscriptionExceptionProvider", expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Subscription was payed, but some error occurs. Please, contact support.")
    public void shouldThrowServerExceptionIfExceptionOccursOnUpdateSubscription(ApiException exception) throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale((com.braintreegateway.TransactionRequest)any())).thenReturn(result);
        when(result.isSuccess()).thenReturn(true);
        when(result.getTarget()).thenReturn(transaction);
        when(transaction.getId()).thenReturn("TRANSACTION_ID");
        doThrow(exception).when(accountDao).updateSubscription(eq(DtoFactory.getInstance().clone(subscription).withState(ACTIVE)));

        service.purchase(payment);
    }

    @DataProvider(name = "updateSubscriptionExceptionProvider")
    public ApiException[][] updateSubscriptionExceptionProvider() {
        return new ApiException[][]{{new ServerException("")},
                                    {new NotFoundException("")},

        };
    }

    @Test(dataProvider = "addSubscriptionHistoryEventExceptionProvider", expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Subscription was payed, but some error occurs. Please, contact support.")
    public void shouldThrowServerExceptionIfExceptionOccursOnAddSubscriptionHistoryEvent(ApiException e) throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale((com.braintreegateway.TransactionRequest)any())).thenReturn(result);
        when(result.isSuccess()).thenReturn(true);
        when(result.getTarget()).thenReturn(transaction);
        when(transaction.getId()).thenReturn("TRANSACTION_ID");
        doThrow(e).when(accountDao).addSubscriptionHistoryEvent(any(SubscriptionHistoryEvent.class));

        service.purchase(payment);
    }

    @DataProvider(name = "addSubscriptionHistoryEventExceptionProvider")
    public ApiException[][] addSubscriptionHistoryEventExceptionProvider() {
        return new ApiException[][]{{new ServerException("")},
                                    {new ConflictException("")},

        };
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBraintreeExceptionOccurs() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale(any(TransactionRequest.class))).thenThrow(BraintreeException.class);

        service.purchase(payment);
    }
}