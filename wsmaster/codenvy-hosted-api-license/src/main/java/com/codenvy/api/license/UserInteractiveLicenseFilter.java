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
package com.codenvy.api.license;

import com.codenvy.api.license.server.CodenvyLicenseManager;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * @author Dmytro Nochevov
 */
@Singleton
public class UserInteractiveLicenseFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(UserInteractiveLicenseFilter.class);
    @Inject
    protected RequestFilter          requestFilter;
    @Inject
    protected HttpJsonRequestFactory requestFactory;
    @Inject
    @Named("che.api")
    protected String                 apiEndpoint;

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        final HttpServletResponse httpResponse = (HttpServletResponse)response;
        if (requestFilter.shouldSkip(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

//        final String clientUrl = clientUrlExtractor.getClientUrl(httpRequest);
//
//        String token = tokenExtractor.getToken(httpRequest);
//
//        HttpSession session;
//        if (token != null) {
//            //TODO thread safety
//            session = sessionStore.getSession(token);
//            if (session == null) {
//                session = httpRequest.getSession();
//                sessionStore.saveSession(token, session);
//            }

//            final SsoClientPrincipal principal = getPrincipal(session, token, clientUrl);
//            if (principal == null) {
//                tokenHandler.handleBadToken(httpRequest, httpResponse, chain, token);
//                return;
//            } else {
            try {
                if (NoUserInteratctionLicenseFilter.fairSourceLicenseIsNotAccepted(requestFactory, apiEndpoint)) {
                    if (isAdmin()) {
                        sendUserToAcceptFairSourceLicensePage(httpRequest, httpResponse);
                    } else {
                        createForbiddenAccessResponse(httpResponse, CodenvyLicenseManager.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE);
                    }
                } else {
                    chain.doFilter(request, response);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
//            }
//        }
    }

    private void createForbiddenAccessResponse(HttpServletResponse response, String fairSourceLicenseIsNotAcceptedMessage) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, fairSourceLicenseIsNotAcceptedMessage);
    }

    private boolean isAdmin() {
//        return permissionChecker.hasPermission(principal.getUser().getUserId(), "system", null, "manageCodenvy");
        return EnvironmentContext.getCurrent().getSubject().hasPermission("system", null, "managerUsers");
    }

//    private SsoClientPrincipal getPrincipal(HttpSession session, String token, String clientUrl) {
//        SsoClientPrincipal principal = (SsoClientPrincipal)session.getAttribute("principal");
//        if (principal == null || !principal.getToken().equals(token)) {
//            // Case when same client use same http session but different authentication token
//            if (principal != null) {
//                sessionStore.removeSessionByToken(principal.getToken());
//                sessionStore.saveSession(token, session);
//            }
//            Subject subject = ssoServerClient.getSubject(token, clientUrl);
//            if (subject != null) {
//                principal = new SsoClientPrincipal(token, clientUrl, subject);
//                session.setAttribute("principal", principal);
//            }
//        }
//        return principal;
//    }

    private void sendUserToAcceptFairSourceLicensePage(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        UriBuilder redirectUrl = UriBuilder.fromPath("/site/auth/accept-fair-source-licence");
//        redirectUrl.queryParam("redirect_url", encode(UriBuilder.fromUri(request.getRequestURL().toString())
//                                                                .replaceQuery(request.getQueryString())
//                                                                .replaceQueryParam("token")
//                                                                .build()
//                                                                .toString(), "UTF-8"));
//        redirectUrl.queryParam("client_url", encode(clientUrlExtractor.getClientUrl(request), "UTF-8"));
        response.sendRedirect(redirectUrl.build().toString());
    }

    @Override
    public void destroy() {
    }
}
