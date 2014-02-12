/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.regex.Pattern;

/** @author Alexander Reshetnyak */
public class ReportsFilter implements Filter {

    private final static String ALLOWED_PRINCIPALS_PATTERN = "allowed-principals";
    private final static String ALLOWED_ALL                = ".*";

    private Pattern allowedPrincipalsPattern;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        Principal userPrincipal = httpRequest.getUserPrincipal();

        if (userPrincipal != null && isAccessPermitted(userPrincipal.getName())) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath());
        }
    }

    private boolean isAccessPermitted(String user) {
        return allowedPrincipalsPattern.matcher(user).matches();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String pattern = filterConfig.getInitParameter(ALLOWED_PRINCIPALS_PATTERN);
        allowedPrincipalsPattern = pattern != null ? Pattern.compile(pattern) : Pattern.compile(ALLOWED_ALL);
    }

    @Override
    public void destroy() {
    }
}
