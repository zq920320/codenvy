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
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.PaymentService;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * Add subscriptions at Braintree.
 *
 * @author Alexander Garagatyi
 */
public class BraintreePaymentService implements PaymentService {
    private static final Logger LOG = LoggerFactory.getLogger(BraintreePaymentService.class);

    private final BraintreeGateway gateway;

    @Inject
    public BraintreePaymentService(BraintreeGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void addSubscription(Subscription subscription, Map<String, String> billingProperties)
            throws ServerException, ConflictException, ForbiddenException {
        if (billingProperties == null || billingProperties.get("payment_token") == null) {
            throw new ForbiddenException("No billing information provided");
        }

        Result<com.braintreegateway.Subscription> result;
        try {
            LOG.info("PAYMENTS# subscriptionId#{}# planId#{}#", subscription.getId(), subscription.getPlanId());

            SubscriptionRequest subscriptionRequest = new SubscriptionRequest()
                    .id(subscription.getId())
                    .paymentMethodToken(billingProperties.get("payment_token"))
                    .planId(subscription.getPlanId());

            result = gateway.subscription().create(subscriptionRequest);
            if (result.isSuccess()) {
                com.braintreegateway.Subscription target = result.getTarget();
                LOG.info("PAYMENTS# state#{}# subscriptionId#{}# subscriptionStatus#{}#", "Successful", target.getId(), target.getStatus());
                return;
            }
        } catch (BraintreeException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }

        LOG.error("PAYMENTS# state#{}# subscriptionId#{}# message#{}#", "Payment error", subscription.getId(), result.getMessage());
        throw new ConflictException(result.getMessage());
    }
}
