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
package com.codenvy.service.http;

import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;
import com.codenvy.commons.env.EnvironmentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Sergii Kabashniuk
 */

public abstract class WorkspaceEnvironmentInitializationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceEnvironmentInitializationFilter.class);

    @Named("error.page.workspace_not_found_redirect_url")
    @Inject
    private String wsNotFoundRedirectUrl;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            WorkspaceDescriptor workspace = getWorkspaceFromRequest(request);
            if (workspace == null) {
                ((HttpServletResponse)response).sendRedirect(wsNotFoundRedirectUrl);
                return;
            }
            final EnvironmentContext env = EnvironmentContext.getCurrent();
            env.setWorkspaceName(workspace.getName());
            env.setWorkspaceId(workspace.getId());
            env.setAccountId(workspace.getAccountId());
            env.setWorkspaceTemporary(workspace.isTemporary());
            LOG.debug("Set context wsn:{}. wsid:{}, accountid:{} , iswstmp:{}", env.getWorkspaceName(), env.getWorkspaceId(),
                      env.getAccountId(), env.isWorkspaceTemporary());
            chain.doFilter(request, response);
        } finally {
            EnvironmentContext.reset();
        }
    }

    protected abstract WorkspaceDescriptor getWorkspaceFromRequest(ServletRequest request) throws ServletException;

    @Override
    public void destroy() {

    }
}
