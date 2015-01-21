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
import com.codenvy.inject.DynaModule;
import com.codenvy.service.http.AccountIdEnvironmentInitializationFilter;
import com.codenvy.service.http.IdeVersionInitializationFilter;
import com.google.inject.servlet.ServletModule;

import org.everrest.websockets.WSConnectionTracker;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Servlet module composer for api war.
 */
@DynaModule
public class CloudIdeApiServletModule extends ServletModule {
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
               "/runner/*",
               "/debug-java/*",
               "/async/*",
               "/git/*",
               "/invite/*",
               "/ssh-keys/*",
               "/appengine/*",
               "/gae-validator/*",
               "/gae-parameters/*")
                .through(com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter.class);
        filterRegex("^/workspace/(?!find/|find$|all/|all$|temp/|temp$).+")
                .through(com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter.class);
        filter("/workspace", "/workspace/")
                .through(com.codenvy.service.http.WorkspaceNameRequestParamInitializationFilter.class);
        filter("/factory/*")
                .through(FactoryWorkspaceIdEnvironmentInitializationFilter.class);

        filterRegex("^/account/(?!find|list|subscriptions|credit-card).+").through(AccountIdEnvironmentInitializationFilter.class);

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
               "/github/*",
               "/ssh-keys/*",
               "/async/*",
               "/internal/convert/*",
               "/profile",
               "/profile/*",
               "/analytics",
               "/oauth/token/*",
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
               "/gae-parameters/*")
                .through(com.codenvy.auth.sso.client.LoginFilter.class);
        filter("/auth/*",
               "/oauth/*",
               "/oauth",
               "/internal/token/validate")
                .through(IdeVersionInitializationFilter.class);
        filter("/*").through(com.codenvy.auth.sso.client.TemporaryTenantSharingFilter.class);
        filter("/*").through(com.codenvy.workspace.activity.LastAccessTimeFilter.class);
        filter("/resources/*").through(ResourceFilter.class);

        bind(com.codahale.metrics.servlets.ThreadDumpServlet.class).in(Singleton.class);
        bind(com.codahale.metrics.servlets.PingServlet.class).in(Singleton.class);
        serve("/metrics/ping").with(com.codahale.metrics.servlets.PingServlet.class);
        serve("/metrics/threaddump").with(com.codahale.metrics.servlets.ThreadDumpServlet.class);

        serve("/oauth").with(com.codenvy.auth.sso.oauth.OAuthLoginServlet.class);
        serve("/ws/*").with(com.codenvy.everrest.CodenvyEverrestWebSocketServlet.class);
        serve("/eventbus/*").with(com.codenvy.everrest.CodenvyEverrestWebSocketServlet.class);
        serve("/*").with(org.everrest.guice.servlet.GuiceEverrestServlet.class);

        getServletContext().addListener(new WSConnectionTracker());
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
    }

    @Singleton
    static class ResourceFilter implements Filter {
        private RequestDispatcher defaultRequestDispatcher;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            this.defaultRequestDispatcher = filterConfig.getServletContext().getNamedDispatcher("default");
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            defaultRequestDispatcher.forward(request, response);
        }

        @Override
        public void destroy() {

        }
    }
}
