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
import com.braintreegateway.CreditCardRequest;
import com.braintreegateway.Customer;
import com.braintreegateway.CustomerGateway;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionGateway;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionHistoryEvent;
import com.codenvy.api.account.shared.dto.CreditCardDescriptor;
import com.codenvy.api.account.shared.dto.NewCreditCard;
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
import java.util.Arrays;
import java.util.Collections;

import static com.codenvy.api.account.server.dao.Subscription.State.ACTIVE;
import static com.codenvy.api.account.server.dao.Subscription.State.WAIT_FOR_PAYMENT;
import static com.codenvy.api.account.server.dao.SubscriptionHistoryEvent.Type.UPDATE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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
    public static final  String TRANSACTION_ID   = "TRANSACTION_ID";
    public static final  String ACCOUNT_ID       = "ACCOUNT_ID";
    private static final User   USER             = User.ANONYMOUS;

    @Mock
    private BraintreeGateway                       gateway;
    @Mock
    private TransactionGateway                     transactionGateway;
    @Mock
    private CustomerGateway                        customerGateway;
    @Mock
    private com.braintreegateway.CreditCardGateway creditCardGateway;
    @Mock
    private Result                                 result;
    @Mock
    private Customer                               customer;
    @Mock
    private com.braintreegateway.CreditCard        btCreditCard;
    @Mock
    private Transaction                            transaction;
    @Mock
    private SubscriptionServiceRegistry            registry;
    @Mock
    private AccountDao                             accountDao;
    @Mock
    private SubscriptionService                    subscriptionService;
    private Subscription                           subscription;
    private Subscription                           activeSubscription;

    private SubscriptionHistoryEvent expectedEvent;

    @InjectMocks
    private BraintreePaymentService service;

    @BeforeMethod
    public void setUp() throws Exception {
        subscription = new Subscription().withId(SUBSCRIPTION_ID).withServiceId(SERVICE_ID)
                                         .withAccountId(ACCOUNT_ID).withState(WAIT_FOR_PAYMENT);

        activeSubscription = new Subscription().withId(SUBSCRIPTION_ID).withServiceId(SERVICE_ID)
                                               .withAccountId("ACCOUNT_ID").withState(ACTIVE);

        expectedEvent = new SubscriptionHistoryEvent().withUserId(User.ANONYMOUS.getId()).withType(
                UPDATE).withSubscription(activeSubscription).withAmount(AMOUNT)
                                                      .withTransactionId(TRANSACTION_ID);

        when(gateway.transaction()).thenReturn(transactionGateway);
        EnvironmentContext.getCurrent().setUser(USER);
    }

    @Test
    public void shouldBeAbleToPurchaseSubscription() throws Exception {
        final TransactionRequest expectedTransactionRequest = new TransactionRequest()
                .amount(new BigDecimal(AMOUNT))
                .customerId(USER.getId())
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
        when(transaction.getId()).thenReturn(TRANSACTION_ID);

        service.purchase(USER.getId(), SUBSCRIPTION_ID);

        // verify custom fields, submit for settlement, amount.
        verify(transactionGateway).sale(refEq(expectedTransactionRequest, "billingAddressRequest", "creditCardRequest", "customerRequest",
                                              "descriptorRequest", "shippingAddressRequest", "transactionOptionsRequest"));
        verify(accountDao).updateSubscription(argThat(new ArgumentMatcher<Subscription>() {
            @Override
            public boolean matches(Object argument) {
                Subscription actualSubscription = (Subscription)argument;
                return new Subscription().withId(SUBSCRIPTION_ID).withServiceId(SERVICE_ID)
                                         .withAccountId("ACCOUNT_ID").withState(ACTIVE).equals(actualSubscription);
            }
        }));
        verify(accountDao).addSubscriptionHistoryEvent(argThat(new ArgumentMatcher<SubscriptionHistoryEvent>() {
            @Override
            public boolean matches(Object argument) {
                SubscriptionHistoryEvent actualSubscriptionEvent = (SubscriptionHistoryEvent)argument;
                return expectedEvent.equals(actualSubscriptionEvent.withId(null).withTime(0));
            }
        }));
        verify(subscriptionService).onUpdateSubscription(subscription, activeSubscription);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfServerExceptionOccursOnGetSubscriptionInPurchaseMethod() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenThrow(new ServerException(""));

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription service not found " + SERVICE_ID)
    public void shouldThrowConflictExceptionIfUnknownServiceIdIsUsed() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(null);

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support",
          dataProvider = "tariffExceptionProvider")
    public void shouldThrowServerExceptionIfApiExceptionOccursOnTariff(ApiException e) throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenThrow(e);

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @DataProvider(name = "tariffExceptionProvider")
    public ApiException[][] tariffExceptionProvider() {
        return new ApiException[][]{{new ApiException("message")},
                                    {new ServerException("message")},
                                    {new InvalidArgumentException("message")},
                                    {new ForbiddenException("message")}
        };
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Payment not required")
    public void shouldThrowConflictExceptionIfSubscriptionAmountIs0() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(0D);

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Payment not required")
    public void shouldThrowConflictExceptionIfSubscriptionStateIsActive() throws Exception {
        Subscription subscription = new Subscription(this.subscription).withState(ACTIVE);
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "BraintreeMessage")
    public void shouldThrowConflictExceptionIfSubscriptionPurchaseIsUnsuccessful() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale((com.braintreegateway.TransactionRequest)any())).thenReturn(result);
        when(result.isSuccess()).thenReturn(false);
        when(result.getMessage()).thenReturn("BraintreeMessage");

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
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
        doThrow(exception).when(accountDao).updateSubscription(eq(activeSubscription));

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
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
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        doThrow(e).when(accountDao).addSubscriptionHistoryEvent(any(SubscriptionHistoryEvent.class));

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @DataProvider(name = "addSubscriptionHistoryEventExceptionProvider")
    public ApiException[][] addSubscriptionHistoryEventExceptionProvider() {
        return new ApiException[][]{{new ServerException("")},
                                    {new ConflictException("")},

        };
    }

    @Test(dataProvider = "onUpdateSubscriptionExceptionProvider", expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Subscription was payed, but some error occurs. Please, contact support.")
    public void shouldThrowServerExceptionIfExceptionOccursOnOnUpdateSubscription(ApiException e) throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale((com.braintreegateway.TransactionRequest)any())).thenReturn(result);
        when(result.isSuccess()).thenReturn(true);
        when(result.getTarget()).thenReturn(transaction);
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        doThrow(e).when(subscriptionService).onUpdateSubscription(any(Subscription.class), any(Subscription.class));

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @DataProvider(name = "onUpdateSubscriptionExceptionProvider")
    public ApiException[][] onUpdateSubscriptionExceptionProvider() {
        return new ApiException[][]{{new ServerException("message")},
                                    {new ConflictException("message")}

        };
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBraintreeExceptionOccurs() throws Exception {
        when(accountDao.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(registry.get(SERVICE_ID)).thenReturn(subscriptionService);
        when(subscriptionService.tarifficate(any(Subscription.class))).thenReturn(AMOUNT);
        when(transactionGateway.sale(any(TransactionRequest.class))).thenThrow(new BraintreeException());

        service.purchase(USER.getId(), SUBSCRIPTION_ID);
    }

    @Test
    public void shouldBeAbleToGetCreditCard() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Arrays.asList(btCreditCard));
        when(btCreditCard.getCardholderName()).thenReturn(CARDHOLDER);
        when(btCreditCard.getExpirationMonth()).thenReturn(EXPIRATION_MONTH);
        when(btCreditCard.getExpirationYear()).thenReturn(EXPIRATION_YEAR);
        when(btCreditCard.getMaskedNumber()).thenReturn("maskedNumber");
        CreditCardDescriptor expectedCreditCard =
                DtoFactory.getInstance().createDto(CreditCardDescriptor.class).withCardNumber("maskedNumber").withCardholderName(CARDHOLDER)
                          .withExpirationMonth(EXPIRATION_MONTH)
                          .withExpirationYear(EXPIRATION_YEAR);

        CreditCardDescriptor actualCreditCard = service.getCreditCard(USER.getId());

        assertEquals(actualCreditCard, expectedCreditCard);

        verify(customerGateway).find(USER.getId());
        verify(btCreditCard).getCardholderName();
        verify(btCreditCard).getExpirationMonth();
        verify(btCreditCard).getExpirationYear();
        verify(btCreditCard).getMaskedNumber();
        verifyNoMoreInteractions(btCreditCard);
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "User's credit card not found")
    public void shouldThrowNotFoundExceptionIfThereIsNoCreditCardOnGetCreditCard() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Collections.<com.braintreegateway.CreditCard>emptyList());

        service.getCreditCard(USER.getId());
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "User's credit card not found")
    public void shouldThrowNotFoundExceptionIfThereIsNoSuchBTUserOnGetCreditCard() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenThrow(new com.braintreegateway.exceptions.NotFoundException());

        service.getCreditCard(USER.getId());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursOnGetCreditCard() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenThrow(new BraintreeException());

        service.getCreditCard(USER.getId());
    }

    @Test
    public void shouldBeAbleToSaveCreditCardIfUserExistInBT() throws ApiException {
        CreditCardRequest expectedCreditCardRequest = new CreditCardRequest()
                .customerId(USER.getId())
                .cardholderName(CARDHOLDER)
                .cvv(CVV)
                .number(CARD_NUMBER)
                .expirationMonth(EXPIRATION_MONTH)
                .expirationYear(EXPIRATION_YEAR)
                // TODO validate it
//                .options()
//                .verifyCard(true)
//                .done()
                ;
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Collections.<com.braintreegateway.CreditCard>emptyList());
        when(gateway.creditCard()).thenReturn(creditCardGateway);
        when(creditCardGateway.create(any(CreditCardRequest.class))).thenReturn(result);
        when(result.isSuccess()).thenReturn(true);

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);

        verify(creditCardGateway).create(refEq(expectedCreditCardRequest, "billingAddressRequest", "parent", "optionsRequest"));
    }

    @Test
    public void shouldBeAbleToSaveCreditCardIfUserDoesNotExistInBT() throws ApiException {
        CustomerRequest expectedCustomerRequest = new CustomerRequest()
                .id(USER.getId())
                // TODO validate it
//                .creditCard()
//                .cardholderName(CARDHOLDER)
//                .cvv(CVV)
//                .number(CARD_NUMBER)
//                .expirationMonth(EXPIRATION_MONTH)
//                .expirationYear(EXPIRATION_YEAR)
//                .options()
//                .verifyCard(true)
//                .done()
//                .done()
                ;
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenThrow(new com.braintreegateway.exceptions.NotFoundException());
        when(customerGateway.create(any(CustomerRequest.class))).thenReturn(result);
        when(result.isSuccess()).thenReturn(true);

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);

        verify(customerGateway).create(refEq(expectedCustomerRequest, "creditCardRequest", "parent"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "User is allowed to have 1 credit card. Remove current credit card to be able to add a new one.")
    public void shouldThrowConflictExceptionIfUserAlreadyHaveCreditCardOnSaveCreditCard() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Arrays.asList(btCreditCard));

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "BT message")
    public void shouldThrowConflictExceptionIfCreditCardSavingIsUnsuccessful() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Collections.<com.braintreegateway.CreditCard>emptyList());
        when(gateway.creditCard()).thenReturn(creditCardGateway);
        when(creditCardGateway.create(any(CreditCardRequest.class))).thenReturn(result);
        when(result.isSuccess()).thenReturn(false);
        when(result.getMessage()).thenReturn("BT message");

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "BT message")
    public void shouldThrowConflictExceptionIfUserWithCreditCardSavingIsUnsuccessful() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenThrow(new com.braintreegateway.exceptions.NotFoundException());
        when(customerGateway.create(any(CustomerRequest.class))).thenReturn(result);
        when(result.isSuccess()).thenReturn(false);
        when(result.getMessage()).thenReturn("BT message");

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursOnGetUserInSaveCreditCardMethod() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(anyString())).thenThrow(new BraintreeException());

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursOnCreateCreditCardInSaveCreditCardMethod() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Collections.<com.braintreegateway.CreditCard>emptyList());
        when(gateway.creditCard()).thenReturn(creditCardGateway);
        when(creditCardGateway.create(any(CreditCardRequest.class))).thenThrow(new BraintreeException());

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursOnCreateUserWithCreditCardInSaveCreditCardMethod() throws ApiException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenThrow(new com.braintreegateway.exceptions.NotFoundException());
        when(customerGateway.create(any(CustomerRequest.class))).thenThrow(new BraintreeException());

        NewCreditCard creditCard =
                DtoFactory.getInstance().createDto(NewCreditCard.class).withCardNumber(CARD_NUMBER).withCardholderName(CARDHOLDER).withCvv(
                        CVV).withExpirationMonth(EXPIRATION_MONTH).withExpirationYear(EXPIRATION_YEAR);

        service.saveCreditCard(USER.getId(), creditCard);
    }

    @Test
    public void shouldBeAbleToRemoveAllUserCreditCards() throws ServerException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Arrays.asList(btCreditCard, btCreditCard));
        when(btCreditCard.getToken()).thenReturn("card token1").thenReturn("card token2");
        when(gateway.creditCard()).thenReturn(creditCardGateway);

        service.removeCreditCard(USER.getId());

        verify(creditCardGateway).delete("card token1");
        verify(creditCardGateway).delete("card token2");
    }

    @Test
    public void shouldNotThrowExceptionIfBTUserIsNotFoundOnRemoveCreditCard() throws ServerException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenThrow(new com.braintreegateway.exceptions.NotFoundException());

        service.removeCreditCard(USER.getId());

        verify(customerGateway).find(USER.getId());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursOnFindUserInRemoveCreditCardMethod() throws ServerException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenThrow(new BraintreeException());
        when(customer.getCreditCards()).thenReturn(Arrays.asList(btCreditCard, btCreditCard));
        when(btCreditCard.getToken()).thenReturn("card token1").thenReturn("card token2");
        when(gateway.creditCard()).thenReturn(creditCardGateway);

        service.removeCreditCard(USER.getId());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Internal server error occurs. Please, contact support")
    public void shouldThrowServerExceptionIfBTExceptionOccursOnRemoveCardInRemoveCreditCardMethod() throws ServerException {
        when(gateway.customer()).thenReturn(customerGateway);
        when(customerGateway.find(USER.getId())).thenReturn(customer);
        when(customer.getCreditCards()).thenReturn(Arrays.asList(btCreditCard, btCreditCard));
        when(btCreditCard.getToken()).thenReturn("card token1").thenReturn("card token2");
        when(gateway.creditCard()).thenReturn(creditCardGateway);
        when(creditCardGateway.delete(anyString())).thenThrow(new BraintreeException());

        service.removeCreditCard(USER.getId());
    }
}