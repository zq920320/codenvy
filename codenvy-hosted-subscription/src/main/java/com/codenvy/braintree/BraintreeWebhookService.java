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
package com.codenvy.braintree;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.WebhookNotification;
import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Receive notifications from Braintree and performs the appropriate action with subscriptions.
 *
 * @author Alexander Garagatyi
 */
@Path("subscription/webhook")
public class BraintreeWebhookService {
    private static final Logger LOG = LoggerFactory.getLogger(BraintreeWebhookService.class);
    private BraintreeGateway            gateway;
    private AccountDao                  accountDao;
    private SubscriptionServiceRegistry registry;

    @Inject
    public BraintreeWebhookService(BraintreeGateway gateway, AccountDao accountDao, SubscriptionServiceRegistry registry) {
        this.gateway = gateway;
        this.accountDao = accountDao;
        this.registry = registry;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String verifyWebhookInitialRequest(@QueryParam("bt_challenge") String challenge) {
        return gateway.webhookNotification().verify(challenge);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response processWebhooks(@FormParam("bt_signature") String signature, @FormParam("bt_payload") String payload) {
        try {
            WebhookNotification notification = gateway.webhookNotification().parse(signature, payload);
            switch (notification.getKind()) {
                case SUBSCRIPTION_CANCELED:
                case SUBSCRIPTION_EXPIRED:
                    LOG.info("Subscription webhook was received. Kind#{}# Id#{}# Timestamp#{}#", notification.getKind(),
                             notification.getSubscription().getId(), notification.getTimestamp().getTimeInMillis());
                    com.braintreegateway.Subscription btSubscription = notification.getSubscription();

                    // remove subscription
                    final Subscription subscription = accountDao.getSubscriptionById(btSubscription.getId());
                    accountDao.removeSubscription(btSubscription.getId());
                    accountDao.removeSubscriptionAttributes(btSubscription.getId());
                    final SubscriptionService service = registry.get(subscription.getServiceId());
                    service.onRemoveSubscription(subscription);
                    break;
                default:
                    LOG.error("Payment error. Kind#{}# Timestamp#{}#", notification.getKind(),
                              notification.getTimestamp().getTimeInMillis());
                    return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Response.serverError().build();
        }

        return Response.ok().build();
    }
}
