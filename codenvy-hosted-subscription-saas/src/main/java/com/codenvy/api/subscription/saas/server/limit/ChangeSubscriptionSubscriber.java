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
package com.codenvy.api.subscription.saas.server.limit;

import com.codenvy.api.metrics.server.limit.ActiveTasksHolder;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdog;
import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.server.dao.Subscription;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class ChangeSubscriptionSubscriber implements EventSubscriber<SubscriptionEvent> {
    private final EventService      eventService;
    private final ActiveTasksHolder activeTasksHolder;

    @Inject
    public ChangeSubscriptionSubscriber(EventService eventService, ActiveTasksHolder activeTasksHolder) {
        this.eventService = eventService;
        this.activeTasksHolder = activeTasksHolder;
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(SubscriptionEvent event) {
        Subscription subscription = event.getSubscription();
        if (!SAAS_SUBSCRIPTION_ID.equals(subscription.getServiceId())) {
            return;
        }

        switch (event.getType()) {
            case ADDED:
            case REMOVED:
                final ResourcesWatchdog watchdog = activeTasksHolder.getWatchdog(subscription.getAccountId());
                if (watchdog != null) {
                    watchdog.checkLimit();
                }
                break;
            case RENEWED:
                //do nothing
                break;
        }
    }
}
