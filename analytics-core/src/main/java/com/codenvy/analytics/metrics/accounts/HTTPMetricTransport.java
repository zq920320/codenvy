/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.Configurator;
import com.codenvy.api.core.util.Pair;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.user.User;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class HTTPMetricTransport implements MetricTransport {

    public static final String API_ENDPOINT = "api.endpoint";

    private String codenvyApiUrl;

    @Inject
    public HTTPMetricTransport(Configurator configurator) {
        this.codenvyApiUrl = configurator.getString(API_ENDPOINT);
        if (this.codenvyApiUrl == null) {
            throw new IllegalArgumentException("Not defined mandatory property " + API_ENDPOINT);
        }
    }

    @Override
    public <DTO> DTO getResource(Class<DTO> dtoInterface,
                                 String method,
                                 String path) throws IOException {
        return this.getResource(dtoInterface, method, path, null, new Pair[0]);
    }

    @Override
    public <DTO> List<DTO> getResources(Class<DTO> dtoInterface,
                                        String method,
                                        String path) throws IOException {
        return this.getResources(dtoInterface, method, path, null, new Pair[0]);
    }

    @Override
    public <DTO> DTO getResource(Class<DTO> dtoInterface,
                                 String method,
                                 String path,
                                 Object body,
                                 Pair... parameters) throws IOException {
        List<Pair<String, String>> pairs = copyParisList(parameters);

        Pair[] p = new Pair[pairs.size()];
        pairs.toArray(p);

        return getObject(dtoInterface,
                         codenvyApiUrl,
                         path,
                         method,
                         body,
                         p);
    }

    @Override
    public <DTO> List<DTO> getResources(Class<DTO> dtoInterface,
                                        String method,
                                        String path,
                                        Object body,
                                        Pair... parameters) throws IOException {
        List<Pair<String, String>> pairs = copyParisList(parameters);

        Pair[] p = new Pair[pairs.size()];
        pairs.toArray(p);

        return getObjects(dtoInterface,
                          codenvyApiUrl,
                          path,
                          method,
                          body,
                          p);
    }

    private List<Pair<String, String>> copyParisList(Pair<String, ?>... parameters) {
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (Pair<String, ?> pair : parameters) {
            pairs.add(new Pair<>(pair.first, (String)pair.second));
        }

        putAuthenticationToken(pairs);
        return pairs;
    }

    private void putAuthenticationToken(List<Pair<String, String>> pairs) {
        User user = EnvironmentContext.getCurrent().getUser();
        if (user != null) {
            String authToken = user.getToken();
            if (authToken != null) {
                pairs.add(new Pair<>("token", authToken));
            }
        }
    }

    private <DTO> DTO getObject(Class<DTO> dtoInterface,
                                String url,
                                String path,
                                String method,
                                Object body,
                                Pair... parameters) throws IOException {
        String json = request(url,
                              path,
                              method,
                              body,
                              parameters);

        return DtoFactory.getInstance().createDtoFromJson(json, dtoInterface);
    }

    private <DTO> List<DTO> getObjects(Class<DTO> dtoInterface,
                                       String url,
                                       String path,
                                       String method,
                                       Object body,
                                       Pair... parameters) throws IOException {
        String json = request(url,
                              path,
                              method,
                              body,
                              parameters);

        return DtoFactory.getInstance().createListDtoFromJson(json, dtoInterface);
    }


    private String request(String url,
                           String path,
                           String method,
                           Object body,
                           Pair... parameters) throws IOException {

        String resourceUrl = url + path;

        if (parameters != null && parameters.length > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(resourceUrl);
            sb.append('?');
            for (int i = 0, l = parameters.length; i < l; i++) {
                String name = URLEncoder.encode(parameters[i].first.toString(), "UTF-8");
                String value = parameters[i].second == null ? null : URLEncoder
                        .encode(String.valueOf(parameters[i].second), "UTF-8");
                if (i > 0) {
                    sb.append('&');
                }
                sb.append(name);
                if (value != null) {
                    sb.append('=');
                    sb.append(value);
                }
            }
            resourceUrl = sb.toString();
        }
        final HttpURLConnection conn = (HttpURLConnection)new URL(resourceUrl).openConnection();

        conn.setConnectTimeout(30 * 1000);
        try {
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
                throw new IOException(IoUtil.readAndCloseQuietly(in));
            }
            final String contentType = conn.getContentType();
            if (!contentType.startsWith("application/json")) {
                throw new IOException("Unsupported type of response from remote server. ");
            }

            try (InputStream input = conn.getInputStream()) {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } finally {
            conn.disconnect();
        }
    }
}
