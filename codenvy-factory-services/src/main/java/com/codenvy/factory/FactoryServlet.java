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

import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.commons.lang.UrlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/** Servlet to handle factory URL's. */
public abstract class FactoryServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(FactoryServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            URL currentUrl = UriBuilder.fromUri(req.getRequestURL().toString()).replaceQuery(req.getQueryString()).build().toURL();
            Map<String, List<String>> params = UrlUtils.getQueryParameters(currentUrl);

            FactoryUrlFormat factoryUrlFormat;
            if (params.get("id") != null) {
                factoryUrlFormat = new AdvancedFactoryUrlFormat();
            } else {
                factoryUrlFormat = new SimpleFactoryUrlFormat();
            }

            factoryUrlFormat.parse(currentUrl);

            createTempWorkspaceAndRedirect(req, resp);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServletException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE, e);
        } catch (FactoryUrlException e) {
            throw new ServletException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Create temporary workspace for current factory URL and redirect user to this workspace
     *
     * @param req
     *         - an HttpServletRequest object that contains the request the client has made of the servlet
     * @param resp
     *         - an HttpServletResponse object that contains the response the servlet sends to the client
     * @throws ServletException
     * @throws IOException
     */
    protected abstract void createTempWorkspaceAndRedirect(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException;
}
