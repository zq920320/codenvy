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

import com.codenvy.commons.env.EnvironmentContext;

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Set information about account in request by following path:
 * <p/>
 * /{war}/{service}/{account-id}
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class AccountIdEnvironmentInitializationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            String requestUrl = httpRequest.getRequestURI();
            String[] pathParts = requestUrl.split("/", 5);

            final EnvironmentContext env = EnvironmentContext.getCurrent();
            env.setAccountId(pathParts[3]);
            chain.doFilter(request, response);
        } finally {
            final EnvironmentContext context = EnvironmentContext.getCurrent();
            context.setAccountId(null);
        }
    }

    @Override
    public void destroy() {
    }
}
