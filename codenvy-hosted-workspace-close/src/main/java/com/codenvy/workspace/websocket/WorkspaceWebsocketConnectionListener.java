/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.workspace.websocket;

import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.workspace.WsActivitySender;

import org.everrest.websockets.WSConnection;
import org.everrest.websockets.WSConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Listens for opened WebSocket connections and registering message receiver.
 */
public class WorkspaceWebsocketConnectionListener implements WSConnectionListener {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceWebsocketConnectionListener.class);

    private final WsActivitySender wsActivitySender;

    @Inject
    public WorkspaceWebsocketConnectionListener(WsActivitySender wsActivitySender) {
        this.wsActivitySender = wsActivitySender;
    }

    @Override
    public void onOpen(WSConnection connection) {
        EnvironmentContext context = (EnvironmentContext)connection.getHttpSession().getAttribute(
                "ide.websocket." + EnvironmentContext.class.getName());
        if (null != context && null != context.getWorkspaceId()) {
            connection.registerMessageReceiver(
                    new WorkspaceWebsocketMessageReceiver(wsActivitySender, context.getWorkspaceId(), context.isWorkspaceTemporary()));
        } else {
            LOG.warn("Workspace id is not set in environment context of WS connection, last access time will not be updated.");
        }
    }

    @Override
    public void onClose(WSConnection connection) {
    }
}
