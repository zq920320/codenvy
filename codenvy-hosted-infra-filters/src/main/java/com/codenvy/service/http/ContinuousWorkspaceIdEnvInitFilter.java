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
package com.codenvy.service.http;


import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Retrieves workspace from request and returns its {@link org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor}
 * when possible, otherwise returns {@code null}. Unlike {@link WorkspaceIdEnvironmentInitializationFilter} this filter
 * continues request processing instead of responding to client.
 *
 * @author Eugene Voevodin
 */
@Singleton
public class ContinuousWorkspaceIdEnvInitFilter extends WorkspaceIdEnvironmentInitializationFilter {

    @Override
    protected void workspaceNotFoundHandler(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                                                        ServletException {
        chain.doFilter(request, response);
    }
}
