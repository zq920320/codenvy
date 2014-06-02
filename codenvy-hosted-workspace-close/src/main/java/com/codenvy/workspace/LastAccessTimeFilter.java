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
package com.codenvy.workspace;

import com.codenvy.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import java.io.IOException;

/**
 * Renewing last workspace access time on http requests.
 */
@Singleton
public class LastAccessTimeFilter implements Filter {

    private final WsActivitySender wsActivitySender;

    @Inject
    public LastAccessTimeFilter(WsActivitySender wsActivitySender) {
        this.wsActivitySender = wsActivitySender;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            EnvironmentContext context = EnvironmentContext.getCurrent();
            // check for null to be able map filter to root
            if (null != context.getWorkspaceId()) {
                wsActivitySender.onMessage(context.getWorkspaceId(), context.isWorkspaceTemporary());
            }
        } finally {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
