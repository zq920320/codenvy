/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.subscription.onpremises.server;

import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Sends emails when OnPremises subscription is added or removed.
 *
 * @author Igor Vinokur
 */
@Singleton
public class OnPremisesSubscriptionSubscriber implements EventSubscriber<SubscriptionEvent> {

    private final EventService           eventService;
    private final SubscriptionMailSender subscriptionMailSender;

    @Inject
    public OnPremisesSubscriptionSubscriber(EventService eventService, SubscriptionMailSender mailSender) {
        this.eventService = eventService;
        this.subscriptionMailSender = mailSender;
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
    public void onEvent(SubscriptionEvent event) {
        Subscription subscription = event.getSubscription();
        if (!"OnPremises".equals(subscription.getServiceId())){
            return;
        }
        String accountId = subscription.getAccountId();
        switch (event.getType()) {
            case ADDED:
                if (subscription.getPlanId().equals("opm-free")) {
                    subscriptionMailSender.sendFreeOnPremisesSubscriptionNotification(accountId);
                } else {
                    subscriptionMailSender.sendPaidOnPremisesSubscriptionNotification(subscription);
                }
                break;
            case REMOVED:
                subscriptionMailSender.sendEmailAboutExpired(accountId);
        }
    }
}
