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
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.PaymentService;
import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.Payment;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionHistoryEvent;
import com.codenvy.api.account.server.dao.SubscriptionPayment;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.env.EnvironmentContext;
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
    public void purchase(Payment payment) throws ConflictException, NotFoundException, ServerException {
        double amount;
        Subscription subscription;
        SubscriptionService service;
        Result<Transaction> result;
        try {
            subscription = accountDao.getSubscriptionById(payment.getSubscriptionId());
            service = registry.get(subscription.getServiceId());
            if (service == null) {
                throw new IllegalArgumentException("Subscription service not found " + subscription.getServiceId());
            }
            amount = service.tarifficate(subscription);
        } catch (ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        } catch (IllegalArgumentException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }

        if (0D == amount || ACTIVE == subscription.getState()) {
            throw new ConflictException("Payment not required");
        }

        try {
            LOG.info("PAYMENTS# payment-reason#{}# payment-reason-id#{}# amount#{}# user-id#{}#", "addSubscription",
                     payment.getSubscriptionId(), amount, EnvironmentContext.getCurrent().getUser().getId());

            TransactionRequest transactionRequest = new TransactionRequest()
                    .amount(new BigDecimal(amount))
                    .creditCard()
                    .number(payment.getCardNumber())
                    .cvv(payment.getCvv())
                    .expirationMonth(payment.getExpirationMonth())
                    .expirationYear(payment.getExpirationYear())
                    .cardholderName(payment.getCardholderName())
                    .done()
                            // every custom field should be added on braintree side
                    .customField("payment_reason", "addSubscription")
                    .customField("payment_reason_id", payment.getSubscriptionId())
                    .options()
                    .submitForSettlement(true)
                    .done();

            result = gateway.transaction().sale(transactionRequest);

            if (result.isSuccess()) {
                LOG.info("PAYMENTS# state#{}# payment-reason-id#{}# transaction-id#{}#", "Successful", payment.getSubscriptionId(),
                         result.getTarget().getId());
                Subscription newSubscription = DtoFactory.getInstance().clone(subscription).withState(ACTIVE);
                accountDao.updateSubscription(newSubscription);

                addSubscriptionHistoryEvent(amount, result, newSubscription);

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

    private void addSubscriptionHistoryEvent(double amount, Result<Transaction> result, Subscription subscription)
            throws ServerException, ConflictException {
        SubscriptionPayment payment = new SubscriptionPayment();
        payment.setAmount(amount);
        payment.setTransactionId(result.getTarget().getId());

        SubscriptionHistoryEvent event = DtoFactory.getInstance().createDto(SubscriptionHistoryEvent.class);
        event.setId(NameGenerator.generate(SubscriptionHistoryEvent.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH));
        event.setType(UPDATE);
        event.setUserId(EnvironmentContext.getCurrent().getUser().getId());
        event.setTime(System.currentTimeMillis());
        event.setSubscription(subscription);
        event.setSubscriptionPayment(payment);

        accountDao.addSubscriptionHistoryEvent(event);
    }
}
