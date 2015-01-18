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
package com.codenvy.workspace.activity.websocket;

import org.everrest.websockets.WSConnectionContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Register listener in WebSocket connection context.
 */
@Singleton
public class WebsocketListenerInitializer {

    private WorkspaceWebsocketConnectionListener connectionListener;

    @Inject
    public WebsocketListenerInitializer(WorkspaceWebsocketConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    @PostConstruct
    public void start() {
        WSConnectionContext.registerConnectionListener(connectionListener);
    }
}
