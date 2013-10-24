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
import com.codenvy.api.factory.SimpleFactoryUrl;
import com.codenvy.commons.lang.UrlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Servlet to handle factory URL's. */
public abstract class FactoryServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(FactoryServlet.class);

    public static final String WSO_2_URL_STRING =
            "(http|https)://((([0-9a-fA-F]{32}(:x-oauth-basic)?)|([0-9a-zA-Z-_.]+))@)?git\\.cloudpreview\\.wso2\\.com" +
            "(:[0-9]{1,5})?/.+\\.git";

    public static final Pattern WSO_2_URL_PATTERN = Pattern.compile(WSO_2_URL_STRING);

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

            SimpleFactoryUrl factory = factoryUrlFormat.parse(currentUrl);
            checkRepository(factory.getVcsurl());


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

    /**
     * Check git repository for a project existence and availability
     *
     * @param vcsUrl
     *         - git repository url
     * @throws FactoryUrlInvalidArgumentException
     *         - if repository isn't accessible
     * @throws FactoryUrlException
     *         - if other exceptions occurs
     */
    protected static void checkRepository(String vcsUrl) throws FactoryUrlException {
        try {
            //Temporary case, to check if we have git url from wso2.
            //For private repository "git ls-remote" will be frozen to prompt user credentials.
            if (WSO_2_URL_PATTERN.matcher(vcsUrl).matches()) {
                LOG.debug("WSO2 repository found. Checked finished.");
                return;
            }

            // To avoid repository cloning use git ls-remote util for repository check
            // Call git ls-remote is much faster than cloning
            Process process = Runtime.getRuntime().exec("/usr/bin/git ls-remote " + vcsUrl);

            // check return value of process.
            if (process.waitFor() != 0) {
                LOG.error("Can't check repository {}. Exit value is {}", new Object[][]{{vcsUrl, process.exitValue()}});
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    LOG.error(line);
                }
                throw new FactoryUrlInvalidArgumentException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
            } else {
                LOG.debug("Repository check finished successfully.");
            }
        } catch (InterruptedException | IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
        }
    }
}
