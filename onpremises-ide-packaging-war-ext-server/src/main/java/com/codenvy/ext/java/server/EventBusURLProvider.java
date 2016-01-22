/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
import java.util.regex.Pattern;

/**
 * Provides value of web socket url to set up event bus between machine and api.
 *
 * @author Anton Korneta
 */
public class EventBusURLProvider extends ApiEndpointProvider {
    /** changes url protocol to web socket e.g https: to ws: */
    private static final Pattern URL_PROTOCOL_PATTERN = Pattern.compile("([A-Za-z]{3,9}:)");

    @Inject
    @Named("user.token")
    String token;

    @Override
    public String get() {
        return URL_PROTOCOL_PATTERN.matcher(super.get()).replaceFirst("ws:") + "/eventbus/?token=" + token;
    }
}
