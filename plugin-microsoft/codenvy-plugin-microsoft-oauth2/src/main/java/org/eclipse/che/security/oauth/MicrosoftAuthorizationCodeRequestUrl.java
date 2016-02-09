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
package org.eclipse.che.security.oauth;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.util.Key;

import java.util.Collections;

/**
 * Override to send response_type as "assertion" instead of "code" in spec.
 *
 * @author Max Shaposhnik
 */
public class MicrosoftAuthorizationCodeRequestUrl extends AuthorizationCodeRequestUrl {

    @Key
    private String assertion;

    /**
     * @param authorizationServerEncodedUrl
     *         authorization server encoded URL
     * @param clientId
     *         client identifier
     */
    public MicrosoftAuthorizationCodeRequestUrl(String authorizationServerEncodedUrl, String clientId) {
        super(authorizationServerEncodedUrl, clientId);
        setResponseTypes(Collections.singleton("assertion"));
    }
}
