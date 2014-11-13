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

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Initialization filter takes workspace name from query param
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class WorkspaceNameRequestParamInitializationFilter extends WorkspaceEnvironmentInitializationFilter {

    @Inject
    private WorkspaceInfoCache cache;

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceNameRequestParamInitializationFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getParameter("name") != null && !request.getParameter("name").isEmpty() && getWorkspaceFromRequest(request) != null) {
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }


    @Override
    public void destroy() {
    }


    protected WorkspaceDescriptor getWorkspaceFromRequest(ServletRequest request) {
        String workspaceName = request.getParameter("name");
        try {
            return cache.getByName(workspaceName);
        } catch (NotFoundException e) {
        } catch (ServerException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        return null;

    }
}
