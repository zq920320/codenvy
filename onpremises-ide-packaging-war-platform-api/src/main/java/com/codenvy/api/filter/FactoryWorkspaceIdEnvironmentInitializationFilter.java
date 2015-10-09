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
package com.codenvy.api.filter;

import com.codenvy.auth.sso.client.filter.ConjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.DisjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentNumberFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentValueFilter;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.filter.RequestMethodFilter;
import com.codenvy.auth.sso.client.filter.UriStartFromRequestFilter;
import com.codenvy.service.http.WorkspaceIdEnvironmentInitializationFilter;

import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Set workspace id in environment context for factory export config method.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class FactoryWorkspaceIdEnvironmentInitializationFilter extends WorkspaceIdEnvironmentInitializationFilter {

    static RequestFilter REQUEST_FILTER = new DisjunctionRequestFilter(new RequestMethodFilter("POST"),
                                                                       new ConjunctionRequestFilter(
                                                                               new UriStartFromRequestFilter("/api/factory"),
                                                                               new RequestMethodFilter("DELETE")),
                                                                       new ConjunctionRequestFilter(
                                                                               new UriStartFromRequestFilter("/api/factory"),
                                                                               new RequestMethodFilter("PUT")),
                                                                       new ConjunctionRequestFilter(
                                                                               new UriStartFromRequestFilter("/api/factory"),
                                                                               new RequestMethodFilter("GET"),
                                                                               new DisjunctionRequestFilter(
                                                                                       new PathSegmentValueFilter(4, "image"),
                                                                                       new PathSegmentValueFilter(4, "snippet"),
                                                                                       new PathSegmentNumberFilter(3)
                                                                               )));
    ;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        if (REQUEST_FILTER.shouldSkip(httpServletRequest)) {
            chain.doFilter(request, response);
        } else {
            super.doFilter(request, response, chain);
        }


    }
}
