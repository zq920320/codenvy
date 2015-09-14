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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.notification.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//TODO fix it after account refactoring

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class AccountLockSubscriber /* implements EventSubscriber<AccountLockEvent> */ {
//    private static final Logger LOG = LoggerFactory.getLogger(AccountLockSubscriber.class);
//
//    private final EventService eventService;
//
//    @Inject
//    public AccountLockSubscriber(EventService eventService) {
//        this.eventService = eventService;
//    }
//
//    @PostConstruct
//    private void subscribe() {
//        eventService.subscribe(this);
//    }
//
//    @PreDestroy
//    private void unsubscribe() {
//        eventService.unsubscribe(this);
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void onEvent(AccountLockEvent event) {
//        switch (event.getType()) {
//            case ACCOUNT_LOCKED:
//                LOG.info("EVENT#account-locked# ACCOUNT#{}#", event.getAccount());
//                break;
//
//            case ACCOUNT_UNLOCKED:
//                LOG.info("EVENT#account-unlocked# ACCOUNT#{}#", event.getAccount());
//                break;
//            default:
//        }
//    }
}
