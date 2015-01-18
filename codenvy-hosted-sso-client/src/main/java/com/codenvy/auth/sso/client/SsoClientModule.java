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
package com.codenvy.auth.sso.client;

import com.codenvy.auth.sso.client.token.ChainedTokenExtractor;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;

/**
 * Initialize all components necessary for sso client work inside of guice container.
 *
 * @author Sergii Kabashniuk
 */
@DynaModule
public class SsoClientModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ServerClient.class).to(HttpSsoServerClient.class);
        bind(RequestTokenExtractor.class).to(ChainedTokenExtractor.class);
        bind(SessionStore.class);
    }
}
