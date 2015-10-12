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
package com.codenvy.api.deploy;

import com.codenvy.api.filter.FactoryWorkspaceIdEnvironmentInitializationFilter;
import com.codenvy.service.http.AccountIdEnvironmentInitializationFilter;
import com.codenvy.service.http.ContinuousWorkspaceIdEnvInitFilter;
import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;
import org.everrest.websockets.WSConnectionTracker;

import javax.inject.Singleton;

/**
 * Servlet module composer for api war.
 */
@DynaModule
public class OnPremisesIdeApiServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        filter("/vfs/*",
               "/project/*",
               "/maven/*",
               "/ws/*",
               "/code-assistant-java/*",
               "/java-name-environment/*",
               "/project-template/*",
               "/builder/*",
               "/debug-java/*",
               "/async/*",
               "/git/*",
               "/svn/*",
               "/invite/*",
               "/ssh-keys/*",
               "/appengine/*",
               "/gae-validator/*",
               "/gae-parameters/*")
                .through(com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter.class);
        filter("/command/*").through(ContinuousWorkspaceIdEnvInitFilter.class);
        filterRegex("^/runner/(?!processes$).+")
                .through(com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter.class);
        filterRegex("^/workspace/(?!find/|find$|all/|all$|temp/|temp$|config/|config$).+")
                .through(com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter.class);
        filter("/workspace", "/workspace/")
                .through(com.codenvy.service.http.WorkspaceNameRequestParamInitializationFilter.class);
        filter("/factory/*")
                .through(FactoryWorkspaceIdEnvironmentInitializationFilter.class);

        filterRegex("^/(account|creditcard)/(?!find|list).+").through(new AccountIdEnvironmentInitializationFilter(),
                                                                      ImmutableMap.of("accountIdPosition", "3"));
        filterRegex("^/subscription/find/account/.*").through(new AccountIdEnvironmentInitializationFilter(),
                                                              ImmutableMap.of("accountIdPosition", "5"));
        filterRegex("/resources/.*").through(new AccountIdEnvironmentInitializationFilter(), ImmutableMap.of("accountIdPosition", "3"));
        filterRegex("^/saas/resources/[\\w-]*/provided").through(new AccountIdEnvironmentInitializationFilter(),
                                                                     ImmutableMap.of("accountIdPosition", "4"));

        filter("/factory/*",
               "/workspace/*",
               "/account/*",
               "/java-name-environment/*",
               "/user/*",
               "/analytics/*",
               "/invite/*",
               "/factory",
               "/workspace",
               "/account",
               "/user",
               "/git/*",
               "/svn/*",
               "/github/*",
               "/bitbucket/*",
               "/ssh-keys/*",
               "/async/*",
               "/internal/convert/*",
               "/profile",
               "/profile/*",
               "/analytics",
               "/oauth/token",
               "/oauth/authenticate",
               "/oauth1/token/*",
               "/password/change",
               "/runner/*",
               "/builder/*",
               "/admin/runner/*",
               "/admin/builder/*",
               "/admin/plan",
               "/project/*",
               "/maven/*",
               "/vfs/*",
               "/ws/*",
               "/appengine/*",
               "/gae-validator/*",
               "/gae-parameters/*",
               "/billing/*",
               "/creditcard/*",
               "/invoice/*",
               "/billing/*",
               "/machine/*",
               "/machine",
               "/recipe",
               "/recipe/*",
               "/command",
               "/command/*",
               "/subscription/*",
               "/subscription",
               "/saas/*",
               "/promotion/*",
               "/resources/*",
               "/ext/*")
                .through(com.codenvy.auth.sso.client.LoginFilter.class);
        filter("/*").through(com.codenvy.auth.sso.client.TemporaryTenantSharingFilter.class);
        filter("/*").through(com.codenvy.workspace.activity.LastAccessTimeFilter.class);

        bind(com.codahale.metrics.servlets.ThreadDumpServlet.class).in(Singleton.class);
        bind(com.codahale.metrics.servlets.PingServlet.class).in(Singleton.class);
        serve("/metrics/ping").with(com.codahale.metrics.servlets.PingServlet.class);
        serve("/metrics/threaddump").with(com.codahale.metrics.servlets.ThreadDumpServlet.class);

        bind(org.eclipse.che.api.machine.server.proxy.MachineExtensionProxyServlet.class);
//                .to(com.codenvy.router.RouterExtServerProxyServlet.class);
        serve("/ext/*").with(org.eclipse.che.api.machine.server.proxy.MachineExtensionProxyServlet.class);


        serve("/oauth").with(com.codenvy.auth.sso.oauth.OAuthLoginServlet.class);
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        serveRegex("^((?!(\\/(ws|eventbus)($|\\/.*)))\\/.*)").with(org.everrest.guice.servlet.GuiceEverrestServlet.class);

        getServletContext().addListener(new WSConnectionTracker());
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
    }
}
