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
package com.codenvy.api.payment;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.exceptions.BraintreeException;
import com.codenvy.api.account.server.PaymentService;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.NewSubscriptionAttributes;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.SimpleDateFormat;

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
    public NewSubscriptionAttributes addSubscription(Subscription subscription, NewSubscriptionAttributes subscriptionAttributes)
            throws ServerException, ConflictException, ForbiddenException {
        if (subscriptionAttributes == null || subscriptionAttributes.getBilling() == null ||
            subscriptionAttributes.getBilling().getPaymentToken() == null) {
            throw new ForbiddenException("No billing information provided");
        }

        Result<com.braintreegateway.Subscription> result;
        try {
            LOG.info("PAYMENTS# subscriptionId#{}# planId#{}#", subscription.getId(), subscription.getPlanId());

            SubscriptionRequest subscriptionRequest = new SubscriptionRequest()
                    .id(subscription.getId())
                    .paymentMethodToken(subscriptionAttributes.getBilling().getPaymentToken())
                    .planId(subscription.getPlanId());

            result = gateway.subscription().create(subscriptionRequest);
            if (result.isSuccess()) {
                com.braintreegateway.Subscription target = result.getTarget();
                LOG.info("PAYMENTS# state#{}# subscriptionId#{}# subscriptionStatus#{}#", "Successful", target.getId(), target.getStatus());

                NewSubscriptionAttributes newAttributes = DtoFactory.getInstance().clone(subscriptionAttributes);
                newAttributes.getBilling().withStartDate(new SimpleDateFormat("MM/dd/yyyy").format(target.getFirstBillingDate().getTime()));
                newAttributes.setTrialDuration(target.getTrialDuration());
                return newAttributes;
            }
        } catch (BraintreeException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Internal server error occurs. Please, contact support");
        }

        LOG.error("PAYMENTS# state#{}# subscriptionId#{}# message#{}#", "Payment error", subscription.getId(), result.getMessage());
        throw new ConflictException(result.getMessage());
    }

    @Override
    public void removeSubscription(String subscriptionId) throws ServerException, NotFoundException, ForbiddenException {
        if (null == subscriptionId) {
            throw new ForbiddenException("Subscription id is missing");
        }

        try {
            final Result<com.braintreegateway.Subscription> result = gateway.subscription().cancel(subscriptionId);
            if (!result.isSuccess()) {
                LOG.error(result.getMessage());
            }
        } catch (com.braintreegateway.exceptions.NotFoundException e) {
            // subscription is missing on BT
            throw new NotFoundException(e.getLocalizedMessage());
        } catch (BraintreeException e) {
            LOG.error(e.getLocalizedMessage(), e);
            // Braintree does not return exception message for now
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }
}
