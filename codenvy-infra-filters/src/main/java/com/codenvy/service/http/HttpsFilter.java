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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/** Redirect all unsecured GET request to https. */
public class HttpsFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(HttpsFilter.class);

    /** To be able to configure same war in different env we configure this property over system parameter. */
    private final boolean ensureSecureConnection;

    public HttpsFilter() {
        this.ensureSecureConnection = "https".equals(System.getProperty("tenant.masterhost.protocol", "http"));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;

        StringBuffer requestURL = httpRequest.getRequestURL();
        if (ensureSecureConnection && !request.isSecure()) {
            if (httpRequest.getMethod().equals("GET")) {
                LOG.warn("Request made to insecure url {} redirect it to secure", requestURL);
                String targetRedirect =
                        UriBuilder.fromUri(requestURL.toString()).replaceQuery(httpRequest.getQueryString()).scheme("https").build()
                                  .toString();
                HttpServletResponse httpResponse = (HttpServletResponse)response;
                httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                httpResponse.setHeader("Location", targetRedirect);
                return;
            } else {
                LOG.warn("Unable to redirect insecure POST request to {}. Continue processing to insecure channel ", requestURL);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {


    }
}