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
package com.codenvy.factory.commons;

import com.codenvy.commons.json.JsonHelper;
import com.codenvy.commons.json.JsonParseException;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.commons.lang.Strings;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.client.UserManager;
import com.codenvy.organization.client.WorkspaceManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.ItemReference;
import com.codenvy.organization.model.User;
import com.codenvy.organization.model.Workspace;
import com.exoplatform.cloud.status.TenantInfo;

import org.everrest.core.impl.uri.UriBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;

/** Servlet to handle factory URL's. */
public class FactoryServlet extends HttpServlet {

    private UserManager      userManager;
    private AccountManager   accountManager;
    private WorkspaceManager workspaceManager;

    private static final Logger LOG = LoggerFactory.getLogger(FactoryServlet.class);

    public FactoryServlet() throws OrganizationServiceException {
        this.userManager = new UserManager();
        this.accountManager = new AccountManager();
        this.workspaceManager = new WorkspaceManager();
    }

    // For tests only
    public FactoryServlet(UserManager userManager, AccountManager accountManager, WorkspaceManager workspaceManager)
            throws OrganizationServiceException {
        this.userManager = userManager;
        this.accountManager = accountManager;
        this.workspaceManager = workspaceManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String username;
        String factory_url = UriBuilder.fromUri(req.getRequestURL().toString()).replaceQuery(req.getQueryString()).build().toString();
        String tenantName = NameGenerator.generate("tmp-", 14);

        if (req.getUserPrincipal() != null) {
            username = req.getUserPrincipal().getName();
        } else {
            LOG.error("Factory creation failed: user unknown. Please contact support.");
            sendErrorRedirect(resp, CommonFactoryUrlFormat.DEFAULT_MESSAGE);
            return;
        }

        User user;
        try {
            user = userManager.getUserByAlias(username);
            for (ItemReference accRef : user.getAccounts()) {
                Account acc = accountManager.getAccountById(accRef.getId());
                for (ItemReference wsRef : acc.getWorkspaces()) {
                    Workspace ws = workspaceManager.getWorkspaceById(wsRef.getId());
                    if (ws.getAttributes().containsKey("factoryURL") && ws.getAttributes().get("factoryURL").equals(factory_url)) {
                        UriBuilder redirectIfExists = new UriBuilderImpl();
                        redirectIfExists.path("/ide").path(ws.getName()).path(req.getParameter("pname"));
                        resp.sendRedirect(redirectIfExists.build().toString());// To already existed project
                        return;
                    }

                }
            }
        } catch (OrganizationServiceException e) {
            LOG.error(e.getLocalizedMessage(), e);
            sendErrorRedirect(resp, CommonFactoryUrlFormat.DEFAULT_MESSAGE);
        }


        // validate query and send tenant creation request to cloud-admin
        HttpURLConnection connection = null;
        try {
            FactoryUrlParser.parse(factory_url);

            UriBuilder createUrl = UriBuilder.fromUri(req.getRequestURL().toString());
            createUrl.replacePath("/cloud-admin/rest/cloud-admin/tenant-service/create-temporary");
            createUrl.queryParam("tenant", tenantName);
            createUrl.queryParam("user", username);
            createUrl.queryParam("factory-url", URLEncoder.encode(factory_url, "UTF-8"));

            connection = (HttpURLConnection)createUrl.build().toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try {
                    TenantInfo tenantInfo = JsonHelper.fromJson(connection.getInputStream(), TenantInfo.class, null);

                    // header name misspelling is NOT a mistake. Do not change this.
                    String referrer = Strings.nullToEmpty(req.getHeader("Referer"));

                    // DO NOT REMOVE! This log will be used in statistic analyzing
                    LOG.info("EVENT#factory-url-accepted# WS#{}# REFERRER#{}# FACTORY-URL#{}#",
                             new String[]{tenantInfo.getTenantName(), referrer, factory_url});
                } catch (JsonParseException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }

                UriBuilder redirectAfterStateOnline = new UriBuilderImpl();
                redirectAfterStateOnline.path("/ide").path(tenantName);
                redirectAfterStateOnline.replaceQuery(req.getQueryString());

                UriBuilder redirectUrl = new UriBuilderImpl();
                redirectUrl.path("/wait-for-tenant");
                redirectUrl.queryParam("type", "factory");
                redirectUrl.queryParam("username", username);
                redirectUrl.queryParam("tenantName", tenantName);
                redirectUrl.queryParam("redirect_url", URLEncoder.encode(redirectAfterStateOnline.build().toString(), "UTF-8"));

                resp.sendRedirect(redirectUrl.build().toString());

            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    LOG.error(IoUtil.readAndCloseQuietly(errorStream));
                }
                sendErrorRedirect(resp, CommonFactoryUrlFormat.DEFAULT_MESSAGE);
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            // load error page
            sendErrorRedirect(resp, CommonFactoryUrlFormat.DEFAULT_MESSAGE);
        } catch (FactoryUrlException e) {
            sendErrorRedirect(resp, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Send to error page. Message will be encoded as query parameter.
     *
     * @param res
     *         servlet response
     * @param message
     *         error message
     * @throws IOException
     */
    protected void sendErrorRedirect(HttpServletResponse res, String message) throws IOException {
        UriBuilder errorConfirmation = new UriBuilderImpl();
        errorConfirmation.path("/error/error-invalid-factory-url");
        errorConfirmation.queryParam("message", URLEncoder.encode(message, "UTF-8"));

        URI errorConfirmationURI = errorConfirmation.build();

        LOG.warn("Error tenant creation. redirect after : {}", errorConfirmationURI.toString());
        res.sendRedirect(errorConfirmationURI.toString());
    }
}
