/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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

import org.eclipse.che.commons.env.EnvironmentContext;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Sets information about account in request accordingly to the account id,
 * which is located in the path in position defined by filter config.
 * For example for path /api/service/someAccountID next binding is required
 * {@code
 * filterRegex("^/api/service/.*").through(new AccountIdEnvironmentInitializationFilter(), ImmutableMap.of("accountIdConfig", "3"));
 * }
 *
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 */
public class AccountIdEnvironmentInitializationFilter implements Filter {
    private int accountIdPosition;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        accountIdPosition = Integer.parseInt(filterConfig.getInitParameter("accountIdPosition"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            String requestUrl = httpRequest.getRequestURI();
            String[] pathParts = requestUrl.split("/", accountIdPosition + 2);

            final EnvironmentContext env = EnvironmentContext.getCurrent();
            env.setAccountId(pathParts[accountIdPosition]);
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
