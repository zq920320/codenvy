/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
