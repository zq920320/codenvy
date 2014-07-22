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
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.PaymentService;
import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionHistoryEvent;
import com.codenvy.api.account.shared.dto.CreditCardDescriptor;
import com.codenvy.api.account.shared.dto.NewCreditCard;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;

import static com.codenvy.api.account.server.dao.Subscription.State.ACTIVE;
import static com.codenvy.api.account.server.dao.SubscriptionHistoryEvent.Type.UPDATE;

/**
 * Send payment requests to Braintree and activate subscription.
 *
 * @author Alexander Garagatyi
 */
public class BraintreePaymentService implements PaymentService {
    private static final Logger LOG = LoggerFactory.getLogger(BraintreePaymentService.class);

    private final SubscriptionServiceRegistry registry;
    private final AccountDao                  accountDao;
    private final BraintreeGateway            gateway;

    @Inject
    public BraintreePaymentService(SubscriptionServiceRegistry registry, AccountDao accountDao, BraintreeGateway gateway) {
        this.registry = registry;
        this.accountDao = accountDao;
        this.gateway = gateway;
    }

    @Override
    public void purchase(String userId, String subscriptionId) throws ServerException, ConflictException {
        double amount;
        Subscription subscription;
        SubscriptionService service;
        Result<Transaction> result;
        try {
            subscription = accountDao.getSubscriptionById(subscriptionId);
            service = registry.get(subscription.getServiceId());
            if (service == null) {
                throw new ConflictException("Subscription service not found " + subscription.getServiceId());
            }
            amount = service.tarifficate(subscription);
        } catch (ConflictException e) {
            throw e;
        } catch (NotFoundException e) {
            throw new ConflictException(e.getLocalizedMessage());
        } catch (ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }

        if (0D == amount || Subscription.State.WAIT_FOR_PAYMENT != subscription.getState()) {
            throw new ConflictException("Payment not required");
        }

        try {
            LOG.info("PAYMENTS# payment-reason#{}# payment-reason-id#{}# amount#{}# user-id#{}#", "addSubscription", subscriptionId, amount,
                     userId);

            TransactionRequest transactionRequest = new TransactionRequest()
                    .amount(new BigDecimal(amount))
                    .customerId(userId)
                            // every custom field should be added on braintree side
                    .customField("payment_reason", "addSubscription")
                    .customField("payment_reason_id", subscriptionId)
                    .options()
                    .submitForSettlement(true)
                    .done();

            result = gateway.transaction().sale(transactionRequest);

            if (result.isSuccess()) {
                LOG.info("PAYMENTS# state#{}# payment-reason-id#{}# transaction-id#{}#", "Successful", subscriptionId,
                         result.getTarget().getId());
                Subscription newSubscription = new Subscription()
                                              .withId(subscription.getId())
                                              .withAccountId(subscription.getAccountId())
                                              .withServiceId(subscription.getServiceId())
                                              .withStartDate(subscription.getStartDate())
                                              .withEndDate(subscription.getEndDate())
                                              .withProperties(subscription.getProperties())
                                              .withState(ACTIVE);

                accountDao.updateSubscription(newSubscription);

                accountDao.addSubscriptionHistoryEvent(
                        createSubscriptionHistoryEvent(amount, result.getTarget().getId(), newSubscription, userId));

                service.onUpdateSubscription(subscription, newSubscription);
                return;
            }
        } catch (ApiException e) {
            LOG.error(String.format("Error occurs on activating subscription %s. Message: %s", subscription.getId(),
                                    e.getLocalizedMessage()), e);
            throw new ServerException("Subscription was payed, but some error occurs. Please, contact support.");
        } catch (BraintreeException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }

        LOG.error("PAYMENTS# state#{}# payment-reason-id#{}# message#{}#", "Payment error", subscription.getId(),
                  result.getMessage());
        throw new ConflictException(result.getMessage());
    }

    @Override
    public CreditCardDescriptor getCreditCard(String userId) throws ServerException, NotFoundException {
        try {
            Customer result = gateway.customer().find(userId);
            if (result.getCreditCards().size() > 1) {
                LOG.error("User {} has {} credit cards", userId, result.getCreditCards().size());
            } else if (result.getCreditCards().size() == 0) {
                throw new NotFoundException("User's credit card not found");
            }
            final com.braintreegateway.CreditCard btCreditCard = result.getCreditCards().get(0);
            return DtoFactory.getInstance().createDto(CreditCardDescriptor.class)
                             .withCardholderName(btCreditCard.getCardholderName())
                             .withCardNumber(btCreditCard.getMaskedNumber())
                             .withExpirationMonth(btCreditCard.getExpirationMonth())
                             .withExpirationYear(btCreditCard.getExpirationYear());
        } catch (com.braintreegateway.exceptions.NotFoundException e) {
            throw new NotFoundException("User's credit card not found");
        } catch (BraintreeException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }

    @Override
    public void saveCreditCard(String userId, NewCreditCard creditCard) throws ConflictException, ServerException {
        try {
            try {
                Customer customerResult = gateway.customer().find(userId);
                if (customerResult.getCreditCards().size() > 0) {
                    if (customerResult.getCreditCards().size() > 1) {
                        LOG.error("User {} has {} credit cards", userId, customerResult.getCreditCards().size());
                    }
                    throw new ConflictException(
                            "User is allowed to have 1 credit card. Remove current credit card to be able to add a new one.");
                }

                CreditCardRequest request = new CreditCardRequest()
                        .customerId(userId)
                        .cardholderName(creditCard.getCardholderName())
                        .cvv(creditCard.getCvv())
                        .number(creditCard.getCardNumber())
                        .expirationMonth(creditCard.getExpirationMonth())
                        .expirationYear(creditCard.getExpirationYear())
                        .options()
                        .verifyCard(true)
                        .done();
                Result<com.braintreegateway.CreditCard> creditCardResult = gateway.creditCard().create(request);

                if (!creditCardResult.isSuccess()) {
                    throw new ServerException(creditCardResult.getMessage());
                }
                // there is no such user in BT database, add user with credit card
            } catch (com.braintreegateway.exceptions.NotFoundException e) {
                CustomerRequest request = new CustomerRequest()
                        .id(userId)
                        .creditCard()
                        .cardholderName(creditCard.getCardholderName())
                        .cvv(creditCard.getCvv())
                        .number(creditCard.getCardNumber())
                        .expirationMonth(creditCard.getExpirationMonth())
                        .expirationYear(creditCard.getExpirationYear())
                        .options()
                        .verifyCard(true)
                        .done()
                        .done();

                Result<Customer> creditCardResult = gateway.customer().create(request);

                if (!creditCardResult.isSuccess()) {
                    throw new ServerException(creditCardResult.getMessage());
                }
            }
        } catch (BraintreeException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }

    @Override
    public void removeCreditCard(String userId) throws ServerException {
        try {
            Customer result = gateway.customer().find(userId);
            if (result.getCreditCards().size() > 1) {
                LOG.error("User {} has {} credit cards", userId, result.getCreditCards().size());
            }
            for (com.braintreegateway.CreditCard creditCard : result.getCreditCards()) {
                gateway.creditCard().delete(creditCard.getToken());
            }
        } catch (com.braintreegateway.exceptions.NotFoundException ignored) {
        } catch (BraintreeException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }

    private SubscriptionHistoryEvent createSubscriptionHistoryEvent(double amount, String transactionId, Subscription subscription,
                                                                    String userId)
            throws ServerException, ConflictException {
        SubscriptionHistoryEvent event = new SubscriptionHistoryEvent();
        event.setId(NameGenerator.generate(SubscriptionHistoryEvent.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH));
        event.setType(UPDATE);
        event.setUserId(userId);
        event.setTime(System.currentTimeMillis());
        event.setSubscription(subscription);
        event.setAmount(amount);
        event.setTransactionId(transactionId);

        return event;
    }
}
