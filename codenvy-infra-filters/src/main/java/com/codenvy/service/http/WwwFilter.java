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

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter used to permanently redirect all www traffic to the url without www.
 * <p/>
 * https://www.mysite.com to https://www.mysite.com
 */
@Singleton
public class WwwFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(WwwFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;

        StringBuffer requestURL = httpRequest.getRequestURL();
        if (requestURL.indexOf("://www.") > 0) {
            String targetRedirect = requestURL.toString().replace("://www.", "://");
            LOG.warn("Request {} made with www . Permanent redirect to the same url without www {}", requestURL,
                     targetRedirect);
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            httpResponse.setHeader("Location", targetRedirect);
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {
    }
}
