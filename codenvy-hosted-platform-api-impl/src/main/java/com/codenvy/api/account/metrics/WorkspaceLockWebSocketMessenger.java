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
package com.codenvy.api.account.metrics;

import com.codenvy.api.account.WorkspaceLockEvent;
import com.google.inject.Inject;

import org.eclipse.che.api.account.shared.dto.WorkspaceLockDetails;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import static com.codenvy.api.account.WorkspaceLockEvent.EventType;
import static java.lang.String.format;

/**
 * Class that notifies client side about changes of workspace lock state
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceLockWebSocketMessenger implements EventSubscriber<WorkspaceLockEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AccountLockWebSocketMessenger.class);

    private final EventService eventService;

    @Inject
    public WorkspaceLockWebSocketMessenger(EventService eventService) {
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
    public void onEvent(WorkspaceLockEvent event) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();

            bm.setChannel(format("workspace:%s:lock", event.getWorkspaceId()));

            final WorkspaceLockDetails resourcesDescriptor = DtoFactory.getInstance().createDto(WorkspaceLockDetails.class)
                                                                       .withWorkspaceId(event.getWorkspaceId())
                                                                       .withLocked(EventType.WORKSPACE_LOCKED.equals(event.getType()));

            bm.setBody(DtoFactory.getInstance().toJson(resourcesDescriptor));
            WSConnectionContext.sendMessage(bm);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
