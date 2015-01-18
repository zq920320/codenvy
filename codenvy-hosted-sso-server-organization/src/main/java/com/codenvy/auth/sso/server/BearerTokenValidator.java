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
package com.codenvy.auth.sso.server;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.user.server.TokenValidator;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;

import javax.inject.Inject;

/**
 * Token validator implementation.
 */
public class BearerTokenValidator implements TokenValidator {

    @Inject
    private BearerTokenAuthenticationHandler handler;

    @Override
    public String validateToken(String token) throws ConflictException {
        String username =  handler.getPayload(token).get("userName");
        if (username == null || !handler.isValid(token))
            throw new ConflictException("Cannot create user - authentication token is invalid. Request a new one.");
        return username;
    }
}
