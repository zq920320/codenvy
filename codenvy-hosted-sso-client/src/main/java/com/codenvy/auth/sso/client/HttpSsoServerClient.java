/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.auth.sso.server.SsoUser;
import com.codenvy.commons.json.JsonHelper;
import com.codenvy.commons.json.JsonParseException;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.user.User;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Communicates with sso server by http calls.
 *
 * @author Sergii Kabashniuk
 */
public class HttpSsoServerClient implements ServerClient {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSsoServerClient.class);
    private final String apiEndpoint;

    @Inject
    public HttpSsoServerClient(@Named("api.endpoint") String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public User getUser(String token, String clientUrl, String workspaceId, String accountId) {
        try {

            StringBuilder requestBuilder = new StringBuilder(apiEndpoint);
            requestBuilder.append("/internal/sso/server");
            requestBuilder.append("/").append(token);
            requestBuilder.append("?").append("clienturl=").append(URLEncoder.encode(clientUrl, "UTF-8"));
            if (workspaceId != null) {
                requestBuilder.append("&").append("workspaceid=").append(URLEncoder.encode(workspaceId, "UTF-8"));
            }

            if (accountId != null) {
                requestBuilder.append("&").append("accountid=").append(URLEncoder.encode(accountId, "UTF-8"));
            }

            HttpURLConnection conn = (HttpURLConnection)new URL(requestBuilder.toString()).openConnection();
            try {

                conn.setRequestMethod("GET");
                conn.setDoOutput(true);


                final int responseCode = conn.getResponseCode();
                if (responseCode == 400) {
                    return null;
                } else if (responseCode != 200) {

                    throw new IOException(
                            "Error response with status " + responseCode + " for sso client  " + token + ". Message " +
                            IoUtil.readAndCloseQuietly(conn.getErrorStream()));
                }

                try (InputStream in = conn.getInputStream()) {
                    return JsonHelper.fromJson(in, SsoUser.class, null);
                }

            } finally {
                conn.disconnect();
            }
        } catch (IOException | JsonParseException e) {
            LOG.warn(e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public void unregisterClient(String token, String clientUrl) {
        try {
            StringBuilder requestBuilder = new StringBuilder(apiEndpoint);
            requestBuilder.append("/internal/sso/server");
            requestBuilder.append("/").append(token);
            requestBuilder.append("?").append("clienturl=").append(URLEncoder.encode(clientUrl, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection)new URL(requestBuilder.toString()).openConnection();
            try {

                conn.setRequestMethod("DELETE");
                conn.setDoOutput(false);
                conn.setConnectTimeout(5 * 1000);
                conn.setReadTimeout(5 * 1000);

                final int responseCode = conn.getResponseCode();
                if (responseCode != 204) {
                    throw new IOException(
                            "Error response with status " + responseCode + " for sso client  " + token + ". Message " +
                            IoUtil.readAndCloseQuietly(conn.getErrorStream()));
                }

            } finally {
                conn.disconnect();
            }

        } catch (IOException e) {
            LOG.warn(e.getLocalizedMessage());
        }

    }
}
