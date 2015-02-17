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

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Plan;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.PaymentService;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.schedule.ScheduleDelay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Charge subscription with the Braintree.
 *
 * @author Alexander Garagatyi
 */
// must be eager singleton
@Singleton
public class BraintreePaymentService implements PaymentService {
    private static final Logger LOG = LoggerFactory.getLogger(BraintreePaymentService.class);

    private final BraintreeGateway        gateway;
    private       Map<String, BigDecimal> prices;

    @Inject
    public BraintreePaymentService(BraintreeGateway gateway) {
        this.gateway = gateway;
        this.prices = Collections.emptyMap();
    }

    @Override
    public void charge(Subscription subscription) throws ServerException, ConflictException, ForbiddenException {
        if (subscription == null) {
            throw new ForbiddenException("No subscription information provided");
        }
        if (subscription.getId() == null) {
            throw new ForbiddenException("Subscription id required");
        }
        if (subscription.getPaymentToken() == null) {
            throw new ForbiddenException("Payment token required");
        }

        try {
            // prices should be set already by getPrices method
            final BigDecimal price = prices.get(subscription.getPlanId());
            if (null == price) {
                LOG.error("PAYMENTS# state#Error# subscriptionId#{}# message#{}#", subscription.getId(),
                          "Price of plan is not found " + subscription.getPlanId());
                throw new ServerException("Internal server error occurs. Please, contact support");
            }

            final TransactionRequest request = new TransactionRequest()
                    .paymentMethodToken(subscription.getPaymentToken())
                            // add subscription id to identify charging reason
                    .customField("subscription_id", subscription.getId())
                    .options().submitForSettlement(true).done()
                    .amount(price);

            final Result<Transaction> result = gateway.transaction().sale(request);
            final Transaction target = result.getTarget();
            if (result.isSuccess()) {
                // transaction successfully submitted for settlement
                LOG.info("PAYMENTS# state#Success# subscriptionId#{}# transactionStatus#{}# message#{}# transactionId#{}#",
                         subscription.getId(), target.getStatus(), result.getMessage(), target.getId());
            } else {
                LOG.error("PAYMENTS# state#Error# subscriptionId#{}# message#{}#", subscription.getId(), result.getMessage());
                throw new ForbiddenException(result.getMessage());
            }
        } catch (ApiException e) {
            // rethrow user-friendly API exceptions
            throw e;
        } catch (Exception e) {
            LOG.error(String.format("PAYMENTS# state#Error# subscriptionId#%s# message#%s#", subscription.getId(), e.getLocalizedMessage()),
                      e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }

    @Override
    public void charge(String creditCardToken, double amount, String account, String paymentDescription)
            throws ServerException, ForbiddenException {
        if (creditCardToken == null) {
            throw new ForbiddenException("Credit card token can't be null");
        }
        if (amount == 0) {
            throw new ForbiddenException("Amount can't be 0");
        }

        try {
            final TransactionRequest request = new TransactionRequest()
                    .paymentMethodToken(creditCardToken)
                    .customField("reason", paymentDescription + "; accountId:" + account)
                    .options().submitForSettlement(true).done()
                    .amount(new BigDecimal(amount, new MathContext(2)));

            final Result<Transaction> result = gateway.transaction().sale(request);
            final Transaction target = result.getTarget();
            if (result.isSuccess()) {
                // transaction successfully submitted for settlement
                LOG.info("PAYMENTS# state#Success# subscription#Saas# accountId#{}# transactionStatus#{}# message#{}# transactionId#{}#",
                         account, target.getStatus(), result.getMessage(), target.getId());
            } else {
                LOG.error("PAYMENTS# state#Error# subscription#Saas# accountId#{}# message#{}#", account, result.getMessage());
                throw new ForbiddenException(result.getMessage());
            }
        } catch (ApiException e) {
            // rethrow user-friendly API exceptions
            throw e;
        } catch (Exception e) {
            LOG.error(
                    String.format("PAYMENTS# state#Error# subscription#Saas# accountId#%s# message#%s#", account, e.getLocalizedMessage()),
                    e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }


    @ScheduleDelay(delay = 1, unit = TimeUnit.HOURS)
    public void updatePrices() {
        try {
            List<Plan> plans = gateway.plan().all();
            final HashMap<String, BigDecimal> newPrices = new HashMap<>(plans.size());
            for (Plan plan : plans) {
                newPrices.put(plan.getId(), plan.getPrice());
            }
            prices = newPrices;
        } catch (Exception e) {
            LOG.error("Can't retrieve prices for subscription plans." + e.getLocalizedMessage(), e);
        }
    }
}
