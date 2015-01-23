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
package com.codenvy.analytics;

import com.codenvy.auth.sso.client.LoginFilter;
import com.codenvy.auth.sso.client.deploy.SsoClientServletModule;
import com.codenvy.inject.DynaModule;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/** @author Anatoliy Bazko */
@DynaModule
public class AnalyticsServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        if (!Injector.isLocal()) {
            bindConstant().annotatedWith(Names.named("auth.sso.client_allow_anonymous")).to(false);
            bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
            bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url")).to("/site/error/error-cookies-disabled");
            bindConstant().annotatedWith(Names.named("auth.sso.client_skip_filter_regexp")).to(".*/analytics-private/public-metric/.*");

            bind(com.codenvy.analytics.filter.AnalyticsFilter.class);
            bind(com.codenvy.auth.sso.client.WebAppClientUrlExtractor.class);
            bind(com.codenvy.auth.sso.client.EmptyContextResolver.class);
            bind(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);
            bind(com.codenvy.auth.sso.client.filter.RequestFilter.class).to(com.codenvy.auth.sso.client.filter.RegexpRequestFilter.class);
            bind(com.codenvy.auth.sso.client.TokenHandler.class).to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

            filterRegex("/(?!_sso/).*$").through(LoginFilter.class);
            install(new SsoClientServletModule());
        }

        bind(com.codenvy.analytics.filter.ReportsFilter.class);
        filter("/reports/*").through(com.codenvy.analytics.filter.ReportsFilter.class);
    }
}
