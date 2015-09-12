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
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.Configurator;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@Singleton
public class HTTPTransport implements RemoteResourceFetcher {

    private final String apiEndpoint;

    @Inject
    public HTTPTransport(Configurator configurator) {
        this.apiEndpoint = configurator.getString("api.endpoint");
    }

    /** {@inheritDoc} */
    @Override
    public <DTO> DTO fetchResource(Class<DTO> dtoInterface,
                                   String method,
                                   String relPath) throws IOException {
        String json = doRequest(relPath, method, null);
        return DtoFactory.getInstance().createDtoFromJson(json, dtoInterface);
    }

    /** {@inheritDoc} */
    @Override
    public <DTO> List<DTO> fetchResources(Class<DTO> dtoClass,
                                          String method,
                                          String relPath) throws IOException {
        String json = doRequest(relPath, method, null);
        return DtoFactory.getInstance().createListDtoFromJson(json, dtoClass);
    }

    /** {@inheritDoc} */
    @Override
    public <DTO> List<DTO> fetchResources(Class<DTO> dtoClass,
                                          String method,
                                          String relPath,
                                          Object body) throws IOException {
        String json = doRequest(relPath, method, body);
        return DtoFactory.getInstance().createListDtoFromJson(json, dtoClass);
    }

    @Nullable
    private String getAccessToken() throws IOException {
        User user = EnvironmentContext.getCurrent().getUser();
        return user == null ? null : user.getToken();
    }

    private String doRequest(String relPath,
                             String method,
                             @Nullable Object body) throws IOException {
        final String accessToken = getAccessToken();
        final String requestUrl = apiEndpoint + relPath;

        HttpURLConnection conn = null;
        try {
            conn = openConnection(requestUrl, accessToken);
            doRequest(conn, method, body);
            return readAndCloseQuietly(conn.getInputStream());
        } catch (SocketTimeoutException e) { // catch exception and throw a new one with proper message
            URL url = new URL(requestUrl);
            throw new IOException(format("Can't establish connection with %s://%s", url.getProtocol(), url.getHost()));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void doRequest(HttpURLConnection conn,
                           String method,
                           @Nullable Object body) throws IOException {
        conn.setConnectTimeout(30 * 1000);
        conn.setRequestMethod(method);
        if (body != null) {
            conn.addRequestProperty("content-type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream output = conn.getOutputStream()) {
                output.write(DtoFactory.getInstance().toJson(body).getBytes());
            }
        }
        final int responseCode = conn.getResponseCode();

        if ((responseCode / 100) != 2) {
            InputStream in = conn.getErrorStream();
            if (in == null) {
                in = conn.getInputStream();
            }

            throw new IOException("Can't perform request, response from the server: " + readAndCloseQuietly(in));
        }

        final String contentType = conn.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/json")) {
            throw new IOException("Unsupported type of response from remote server.");
        }
    }

    protected HttpURLConnection openConnection(String path, @Nullable String accessToken) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)new URL(path).openConnection();

        if (accessToken != null) {
            String accessTokenCookie = format("session-access-key=%s;", accessToken);
            connection.addRequestProperty("Cookie", accessTokenCookie);
        }

        return connection;
    }
}
