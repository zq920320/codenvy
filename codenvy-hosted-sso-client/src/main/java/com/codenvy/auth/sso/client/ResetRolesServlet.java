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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Invalidate roles of user in given workspace and account.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class ResetRolesServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ResetRolesServlet.class);

    @Inject
    protected SessionStore sessionStore;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String token = req.getParameter("authToken");
        if (token == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token is not set");
            return;
        }

        HttpSession session = sessionStore.removeSessionByToken(token);
        if (session != null) {
            SsoClientPrincipal principal = (SsoClientPrincipal)session.getAttribute("principal");
            if (principal != null) {
                principal.invialidateRoles(new RolesContext(req.getParameter("workspaceId"), req.getParameter("accountId")));
                LOG.info("Roles invalidated for {} in ws:{} account{}", principal.getName(), req.getParameter("workspaceId"),
                         req.getParameter("accountId"));
            } else {
                LOG.warn("Principal not found in session");
            }
        } else {
            LOG.warn("Not found session associated to {}", token);
        }
    }
}
