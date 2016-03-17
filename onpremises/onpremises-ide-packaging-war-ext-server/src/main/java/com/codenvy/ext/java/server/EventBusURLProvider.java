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
package com.codenvy.ext.java.server;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.core.UriBuilder;

/**
 * Provides value of web socket url to set up event bus between machine and api.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
public class EventBusURLProvider implements Provider<String> {
    @Inject
    @Named("user.token")
    String token;

    @Inject
    @Named("api.endpoint")
    String apiEndpoint;

    @Override
    public String get() {
        return UriBuilder.fromUri(apiEndpoint)
                         .scheme(apiEndpoint.startsWith("https") ? "wss" : "ws")
                         .path("/eventbus/")
                         .queryParam("token", token)
                         .build()
                         .toString();
    }
}
