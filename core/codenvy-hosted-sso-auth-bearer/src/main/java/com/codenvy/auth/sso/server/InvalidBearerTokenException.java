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
package com.codenvy.auth.sso.server;

/**
 * Exception used in BearerTokenManager to indicate that provided
 * bearer token doesn't exist or expired.
 * @author Sergii Kabashniuk
 */
public class InvalidBearerTokenException extends Exception {
    private final String token;


    public InvalidBearerTokenException(String message, String token) {
        super(message);
        this.token = token;
    }


    public String getToken() {
        return token;
    }
}
