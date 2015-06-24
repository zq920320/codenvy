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
package com.codenvy.api.subscription.saas.server;

import com.braintreegateway.CreditCard;
import com.codenvy.api.creditcard.server.event.CreditCardRegistrationEvent;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.ResourcesFilter;
import com.codenvy.api.subscription.saas.server.service.util.SubscriptionMailSender;
import com.codenvy.api.subscription.server.SubscriptionServiceRegistry;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;

/**
 * Adds and removes subscriptions on adding and removing of credit cards
 *
 * @author Sergii Leschenko
 */
//TODO Add tests for this class
@Singleton
public class CreditCardRegistrationSubscriber implements EventSubscriber<CreditCardRegistrationEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(CreditCardRegistrationSubscriber.class);

    private final EventService                eventService;
    private final SubscriptionMailSender      subscriptionMailSender;
    private final SubscriptionDao             subscriptionDao;
    private final MetricPeriod                metricPeriod;
    private final SubscriptionServiceRegistry registry;
    private final BillingService              billingService;
    private final AccountLocker               accountLocker;

    @Inject
    public CreditCardRegistrationSubscriber(EventService eventService,
                                            SubscriptionMailSender subscriptionMailSender,
                                            SubscriptionDao subscriptionDao,
                                            MetricPeriod metricPeriod,
                                            SubscriptionServiceRegistry registry,
                                            BillingService billingService,
                                            AccountLocker accountLocker) {
        this.eventService = eventService;
        this.subscriptionMailSender = subscriptionMailSender;
        this.subscriptionDao = subscriptionDao;
        this.metricPeriod = metricPeriod;
        this.registry = registry;
        this.billingService = billingService;
        this.accountLocker = accountLocker;
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(CreditCardRegistrationEvent event) {
        String accountId = event.getAccountId();
        CreditCard creditCard = event.getCreditCard();
        switch (event.getType()) {
            case CREDIT_CARD_ADDED:
                try {
                    subscriptionMailSender.sendCCAddedNotification(accountId, creditCard.getLast4(), creditCard.getCardType());
                } catch (ServerException e) {
                    LOG.error("Error sending email notification about adding of credit card by account" + accountId);
                }
                break;
            case CREDIT_CARD_REMOVED:
                try {
                    //It is need to check existence paid resources before removing subscription
                    boolean hasUsedPaidResources = hasUsedPaidResources(accountId);
                    // Remove saas subscription
                    removeSaasSubscription(accountId);
                    // Check is paid resources used, lock and send notify emails.
                    if (hasUsedPaidResources) {
                        accountLocker.setPaymentLock(accountId);
                    }
                } catch (ServerException e) {
                    e.printStackTrace();
                }
                try {
                    subscriptionMailSender.sendCCRemovedNotification(accountId, creditCard.getLast4(), creditCard.getCardType());
                } catch (ServerException e) {
                    LOG.error("Error sending email notification about removing of credit card by account" + accountId);
                }
                break;
        }
    }

    private void removeSaasSubscription(String accountId) {
        try {
            final Subscription activeSaasSubscription = subscriptionDao.getActiveByServiceId(accountId, SAAS_SUBSCRIPTION_ID);
            if (activeSaasSubscription != null) {
                subscriptionDao.deactivate(activeSaasSubscription.getId());
                registry.get(SAAS_SUBSCRIPTION_ID).onRemoveSubscription(activeSaasSubscription);
            }
        } catch (ApiException e) {
            LOG.warn("Unable to remove subscription after CC deletion.", e);
        }
    }

    private boolean hasUsedPaidResources(String accountId) throws ServerException {
        //Get paid GbH before removing subscription
        ResourcesFilter filter = ResourcesFilter.builder().withAccountId(accountId)
                                                .withPaidGbHMoreThan(0)
                                                .withFromDate(metricPeriod.getCurrent().getStartDate().getTime())
                                                .withTillDate(System.currentTimeMillis())
                                                .build();
        return !billingService.getEstimatedUsageByAccount(filter).isEmpty();
    }
}
