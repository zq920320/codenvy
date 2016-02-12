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
package com.codenvy.auth.sso.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Set user token to authorization header to each machine request.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineRequestTokenInjectFilter implements Filter {

    @Inject
    @Named("user.token")
    private String userToken;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        final AddAuthorizationHeaderRequestWrapper modifiedRequest = new AddAuthorizationHeaderRequestWrapper(((HttpServletRequest) servletRequest));
        filterChain.doFilter(modifiedRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }

    private class AddAuthorizationHeaderRequestWrapper extends HttpServletRequestWrapper {
        public AddAuthorizationHeaderRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (HttpHeaders.AUTHORIZATION.equals(name)) {
                return userToken;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            final List<String> names = Collections.list(super.getHeaderNames());
            if (!names.contains(HttpHeaders.AUTHORIZATION)) {
                names.add(HttpHeaders.AUTHORIZATION);
            }
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            final List<String> values = Collections.list(super.getHeaders(name));
            if (HttpHeaders.AUTHORIZATION.equals(name)) {
                values.add(userToken);
            }
            return Collections.enumeration(values);
        }
    }
}
