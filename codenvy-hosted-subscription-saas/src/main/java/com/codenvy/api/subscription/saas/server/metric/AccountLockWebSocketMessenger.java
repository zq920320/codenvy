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
package com.codenvy.api.subscription.saas.server.metric;

import com.codenvy.api.subscription.saas.server.AccountLockEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.account.shared.dto.AccountLockDetails;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static java.lang.String.format;


/**
 * Class that notifies client side about changes of account lock state
 *
 * @author Oleksii Orel
 */
@Singleton
public class AccountLockWebSocketMessenger implements EventSubscriber<AccountLockEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AccountLockWebSocketMessenger.class);

    private final EventService eventService;

    @Inject
    public AccountLockWebSocketMessenger(EventService eventService) {
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
    
    @Override
    public void onEvent(AccountLockEvent event) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();

            bm.setChannel(format("account:%s:lock", event.getAccount()));

            final AccountLockDetails resourcesDescriptor = DtoFactory.getInstance().createDto(AccountLockDetails.class)
                                                                     .withAccountId(event.getAccount())
                                                                     .withLocked(AccountLockEvent.EventType.ACCOUNT_LOCKED.equals(
                                                                             event.getType()));

            bm.setBody(DtoFactory.getInstance().toJson(resourcesDescriptor));
            WSConnectionContext.sendMessage(bm);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
