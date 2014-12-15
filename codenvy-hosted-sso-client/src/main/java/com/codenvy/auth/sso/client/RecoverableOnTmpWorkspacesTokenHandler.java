/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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

import com.codenvy.commons.env.EnvironmentContext;
import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

import static java.net.URLEncoder.encode;

/**
 * This error handler is used to recover user only on temporary workspaces.
 * On regular workspaces user will be treated as anonymous.
 *
 * @author Sergii Kabashniuk
 */
public class RecoverableOnTmpWorkspacesTokenHandler extends NoUserInteractionTokenHandler {


    @Inject
    @Named("auth.sso.client_allow_anonymous")
    protected boolean allowAnonymous;

    @Inject
    protected ClientUrlExtractor clientUrlExtractor;

    @Inject
    public RecoverableOnTmpWorkspacesTokenHandler(RequestWrapper requestWrapper,
                                                  ClientUrlExtractor clientUrlExtractor,
                                                  @Named("auth.sso.client_allow_anonymous") boolean allowAnonymous) {
        super(requestWrapper);
        this.allowAnonymous = allowAnonymous;
        this.clientUrlExtractor = clientUrlExtractor;
    }

    @Override
    public void handleBadToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token)
            throws IOException, ServletException {

        doHandle(request, response, chain);
    }


    @Override
    public void handleMissingToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doHandle(request, response, chain);
    }

    private void doHandle(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        EnvironmentContext context = EnvironmentContext.getCurrent();
        if ("GET".equals(request.getMethod()) && context.getWorkspaceId() != null && context.isWorkspaceTemporary()) {
            UriBuilder redirectUrl = UriBuilder.fromPath("/api/internal/sso/server/refresh");
            redirectUrl.queryParam("redirect_url", encode(UriBuilder.fromUri(request.getRequestURL().toString())
                                                                    .replaceQuery(request.getQueryString())
                                                                    .replaceQueryParam("token")
                                                                    .build()
                                                                    .toString(), "UTF-8"));
            redirectUrl.queryParam("client_url", encode(clientUrlExtractor.getClientUrl(request), "UTF-8"));
            redirectUrl.queryParam("allowAnonymous", allowAnonymous);
            response.sendRedirect(redirectUrl.build().toString());
        } else {
            super.handleMissingToken(request, response, chain);
        }
    }


}
