/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.service.http;

import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.server.exception.WorkspaceException;
import com.codenvy.api.workspace.server.exception.WorkspaceNotFoundException;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.commons.env.EnvironmentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceIdEnvironmentInitializationFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceIdEnvironmentInitializationFilter.class);

    @Inject
    private WorkspaceDao workspaceDao;

    @Named("error.page.workspace_not_found_redirect_url")
    @Inject
    private String wsNotFoundRedirectUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String requestUrl = httpRequest.getRequestURI();

        String[] pathParts = requestUrl.split("/", 5);

        try {
            Workspace workspace = workspaceDao.getById(pathParts[3]);
            if (null == workspace) {
                ((HttpServletResponse)response).sendRedirect(wsNotFoundRedirectUrl);
                return;
            }
            final EnvironmentContext env = EnvironmentContext.getCurrent();
            env.setWorkspaceName(workspace.getName());
            env.setWorkspaceId(workspace.getId());
            env.setAccountId(workspace.getOrganizationId());

            chain.doFilter(request, response);
        } catch (WorkspaceException e) {
            throw new ServletException(e.getLocalizedMessage(), e);
        } finally {
            EnvironmentContext.reset();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
