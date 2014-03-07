/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics;

import com.codenvy.auth.sso.client.EmptyContextResolver;
import com.codenvy.auth.sso.client.LoginFilter;
import com.codenvy.auth.sso.client.SSOLogoutServlet;
import com.codenvy.auth.sso.client.WebAppClientUrlExtractor;
import com.codenvy.auth.sso.client.token.ChainedTokenExtractor;
import com.codenvy.inject.DynaModule;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/** @author Anatoliy Bazko */
@DynaModule
public class AnalyticsServletModule extends ServletModule {

    private static final String AUTH_SKIP_SSO = "analytics.auth.skip_sso";

    @Override
    protected void configureServlets() {
        Configurator configurator = Injector.getInstance(Configurator.class);

        if (!configurator.getBoolean(AUTH_SKIP_SSO)) {
            bindConstant().annotatedWith(Names.named("auth.sso.client_allow_anonymous")).to(false);
            bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
            bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                          .to("/site/error/error-cookies-disabled");

            bind(WebAppClientUrlExtractor.class);
            bind(EmptyContextResolver.class);
            bind(ChainedTokenExtractor.class);

            filter("/*").through(LoginFilter.class);
            serve("/_sso/client/logout").with(SSOLogoutServlet.class);
        }
    }
}
