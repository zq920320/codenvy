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
package com.codenvy.factory;

import com.codenvy.commons.lang.UrlUtils;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Convert bitbucket edit repo endpoint to factory url.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class BitbucketIntegrationFactoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        URL currentUrl =
                UriBuilder.fromUri(req.getRequestURL().toString()).replaceQuery(req.getQueryString()).build().toURL();

        Map<String, List<String>> bitbucketParams = UrlUtils.getQueryParameters(currentUrl);

        List<String> urls = bitbucketParams.get("url");
        if (urls != null && !urls.isEmpty()) {
            String bitbucketUrl = urls.iterator().next();
            UriBuilder factoryUrl = UriBuilder.fromPath("/factory");
            factoryUrl.queryParam("v", "1.0");
            factoryUrl.queryParam("vcs", "git");
            factoryUrl.queryParam("vcsurl", URLEncoder.encode(bitbucketUrl, "UTF-8"));
            factoryUrl.queryParam("vcsinfo", "true");
            factoryUrl.queryParam("pname", bitbucketUrl.substring(bitbucketUrl.lastIndexOf('/') + 1, bitbucketUrl.lastIndexOf('.')));

            resp.sendRedirect(factoryUrl.build().toString());
        } else {
            resp.sendRedirect(UriBuilder.fromUri("/site/error/error-invalid-factory-url").build().toString()); // To error page
        }
    }
}
