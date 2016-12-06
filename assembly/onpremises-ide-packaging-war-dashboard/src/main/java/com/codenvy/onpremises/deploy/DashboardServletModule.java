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
package com.codenvy.onpremises.deploy;

import com.codenvy.api.license.LicenseFilter;
import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.onpremises.maintenance.MaintenanceStatusServlet;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import org.eclipse.che.inject.DynaModule;

/** Servlet module composer for user dashboard war. */
@DynaModule
public class DashboardServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(PermissionChecker.class).to(com.codenvy.api.permission.server.HttpPermissionCheckerImpl.class);
        bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
        bind(TokenHandler.class).annotatedWith(Names.named("delegated.handler"))
                                .to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                      .to("/site/error/error-cookies-disabled");

        bindConstant().annotatedWith(Names.named("no.user.interaction")).to(false);

        filterRegex("/(?!_sso/).*$").through(com.codenvy.servlet.CacheDisablingFilter.class);

        filterRegex("/(?!_sso/).*$").through(com.codenvy.auth.sso.client.LoginFilter.class);

        filterRegex("/(?!_sso/).*$").through(LicenseFilter.class);

        serve("/scheduled").with(MaintenanceStatusServlet.class);

        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
    }
}
