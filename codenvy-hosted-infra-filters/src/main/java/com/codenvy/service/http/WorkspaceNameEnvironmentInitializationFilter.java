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
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.commons.env.EnvironmentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Alexander Garagatyi */
@Singleton
public class WorkspaceNameEnvironmentInitializationFilter implements Filter {
    public static final  Pattern TENANT_URL_PATTERN = Pattern.compile("^(/ide/)(?!_sso)(.+?)(/.*)?$");
    private static final Logger  LOG                =
            LoggerFactory.getLogger(WorkspaceNameEnvironmentInitializationFilter.class);
    @Inject
    private WorkspaceDao workspaceDao;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String requestUrl = httpRequest.getRequestURI();

        String workspaceName = null;
        try {
            Matcher matcher = TENANT_URL_PATTERN.matcher(requestUrl);
            if (matcher.matches()) {
                workspaceName = matcher.group(2);
                Workspace workspace = workspaceDao.getByName(workspaceName);
                if (null == workspace) {
                    ((HttpServletResponse)response).sendError(HttpServletResponse.SC_NOT_FOUND,
                                                              "Workspace with name " + workspaceName +
                                                              " is not found");
                    return;
                }
                final EnvironmentContext env = EnvironmentContext.getCurrent();
                env.setWorkspaceName(workspace.getName());
                env.setWorkspaceId(workspace.getId());
            }
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
