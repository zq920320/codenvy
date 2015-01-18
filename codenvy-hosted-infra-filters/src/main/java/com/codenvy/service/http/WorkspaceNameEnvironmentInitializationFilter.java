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
package com.codenvy.service.http;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Set information about workspace in request by following path:
 * <p/>
 * /{war}/{service}/{ws-name}
 *
 * @author Alexander Garagatyi
 * @author Sergii Kabashniuk
 */
@Singleton
public class WorkspaceNameEnvironmentInitializationFilter extends WorkspaceEnvironmentInitializationFilter {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceIdEnvironmentInitializationFilter.class);

    @Inject
    private WorkspaceInfoCache cache;


    @Override
    protected WorkspaceDescriptor getWorkspaceFromRequest(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String requestUrl = httpRequest.getRequestURI();
        String[] pathParts = requestUrl.split("/", 5);
        try {
            return cache.getByName(pathParts[3]);
        } catch (NotFoundException e) {
            return null;
        } catch (ServerException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }
}
