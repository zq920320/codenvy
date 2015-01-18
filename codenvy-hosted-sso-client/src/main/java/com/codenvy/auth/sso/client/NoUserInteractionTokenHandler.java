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

import com.codenvy.api.core.UnauthorizedException;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.user.User;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * InvalidTokenHandler that not supposed any user interaction action.
 *
 * @author Sergii Kabashniuk
 */
public class NoUserInteractionTokenHandler extends DefaultTokenHandler {

    @Inject
    public NoUserInteractionTokenHandler(RequestWrapper requestWrapper) {
        super(requestWrapper);
    }

    @Override
    public void handleBadToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token)
            throws IOException,
                   ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON);
        try (PrintWriter writer = response.getWriter();) {
            writer.write(DtoFactory.getInstance()
                                   .toJson(new UnauthorizedException("Provided " + token + " is invalid").getServiceError()));
        }
    }

    @Override
    public void handleMissingToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //go with anonymous
        EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
        environmentContext.setUser(User.ANONYMOUS);
        chain.doFilter(requestWrapper.wrapRequest(request.getSession(), request, User.ANONYMOUS), response);
    }
}
