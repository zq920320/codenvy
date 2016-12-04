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
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.env.EnvironmentContext;

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
 * TODO
 * @author Dmytro Nochevov
 */
@Singleton
public class UserInteractionLicenseFilter implements Filter {
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

        try {
            if (NoUserInteractionLicenseFilter.fairSourceLicenseIsNotAccepted(requestFactory, apiEndpoint)) {
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
    }

    private void createForbiddenAccessResponse(HttpServletResponse response, String fairSourceLicenseIsNotAcceptedMessage) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, fairSourceLicenseIsNotAcceptedMessage);
    }

    private boolean isAdmin() {
        return EnvironmentContext.getCurrent().getSubject().hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_CODENVY_ACTION);
    }

    private void sendUserToAcceptFairSourceLicensePage(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        UriBuilder redirectUrl = UriBuilder.fromPath("/site/auth/accept-fair-source-license");
        response.sendRedirect(redirectUrl.build().toString());
    }

    @Override
    public void destroy() {
    }
}
