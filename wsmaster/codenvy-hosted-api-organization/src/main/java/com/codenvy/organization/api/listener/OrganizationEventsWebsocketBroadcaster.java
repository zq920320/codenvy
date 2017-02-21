/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.organization.api.listener;

import com.codenvy.organization.api.DtoConverter;
import com.codenvy.organization.shared.event.OrganizationEvent;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Broadcasts organization events through websocket connection.
 *
 * @author Anton Korneta
 */
@Singleton
public class OrganizationEventsWebsocketBroadcaster implements EventSubscriber<OrganizationEvent> {

    public static final String ORGANIZATION_CHANNEL_NAME = "organization:%s";

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationEventsWebsocketBroadcaster.class);

    @Inject
    private void subscribe(EventService eventService) {
        eventService.subscribe(this);
    }

    @Override
    public void onEvent(OrganizationEvent event) {
        try {
            final ChannelBroadcastMessage broadcastMessage = new ChannelBroadcastMessage();
            broadcastMessage.setChannel(String.format(ORGANIZATION_CHANNEL_NAME, event.getOrganizationId()));
            broadcastMessage.setBody(DtoFactory.getInstance().toJson(DtoConverter.asDto(event)));
            WSConnectionContext.sendMessage(broadcastMessage);
        } catch (Exception x) {
            LOG.error(x.getMessage(), x);
        }
    }

}
