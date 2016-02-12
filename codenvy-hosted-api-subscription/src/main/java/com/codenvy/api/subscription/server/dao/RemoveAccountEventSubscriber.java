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
package com.codenvy.api.subscription.server.dao;

import com.codenvy.api.event.user.RemoveAccountEvent;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Removes subscriptions on removing account which are related to removed account
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RemoveAccountEventSubscriber implements EventSubscriber<RemoveAccountEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveAccountEventSubscriber.class);

    private final EventService    eventService;
    private final SubscriptionDao subscriptionDao;

    @Inject
    public RemoveAccountEventSubscriber(EventService eventService, SubscriptionDao subscriptionDao) {
        this.eventService = eventService;
        this.subscriptionDao = subscriptionDao;
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
    public void onEvent(RemoveAccountEvent event) {
        try {
            for (Subscription subscription : subscriptionDao.getByAccountId(event.getAccountId())) {
                try {
                    subscriptionDao.remove(subscription.getId());
                } catch (NotFoundException e) {
                    //do nothing
                }
            }
        } catch (ServerException e) {
            LOG.error("Can't remove subscriptions of removed account with id " + event.getAccountId(), e);
        }
    }
}
