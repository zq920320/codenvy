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
package com.codenvy.api.analytics;

import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class SubscriptionSubscriber implements EventSubscriber<SubscriptionEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionSubscriber.class);

    private final EventService eventService;

    @Inject
    public SubscriptionSubscriber(EventService eventService) {
        this.eventService = eventService;
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(SubscriptionEvent event) {
        Subscription subscription = event.getSubscription();
        switch (event.getType()) {
            case ADDED:
                doLog("subscription-added", subscription);
                break;

            case REMOVED:
                doLog("subscription-removed", subscription);
                break;

            case RENEWED:
                doLog("subscription-renewed", subscription);
                break;
        }
    }

    private void doLog(String event, Subscription subscription) {
        LOG.info("EVENT#{}# ACCOUNT#{}# SERVICE#{}# PLAN#{}#",
                 event,
                 subscription.getAccountId(),
                 subscription.getServiceId(),
                 subscription.getPlanId());
    }
}
