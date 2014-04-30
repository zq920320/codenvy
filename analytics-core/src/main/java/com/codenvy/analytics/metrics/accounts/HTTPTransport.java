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
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.user.User;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class HTTPTransport implements MetricTransport {

    public static final String API_ENDPOINT = "api.endpoint";

    private final String baseUrl;

    @Inject
    public HTTPTransport(Configurator configurator) {
        if (!configurator.exists(API_ENDPOINT)) {
            throw new IllegalArgumentException("Not defined mandatory property " + API_ENDPOINT);
        }
        this.baseUrl = configurator.getString(API_ENDPOINT);
    }

    @Override
    public <DTO> DTO getResource(Class<DTO> dtoInterface,
                                 String method,
                                 String path) throws IOException {
        String json = request(path, method);
        return DtoFactory.getInstance().createDtoFromJson(json, dtoInterface);
    }

    @Override
    public <DTO> List<DTO> getResources(Class<DTO> dtoInterface,
                                        String method,
                                        String path) throws IOException {
        String json = request(path, method);
        return DtoFactory.getInstance().createListDtoFromJson(json, dtoInterface);
    }

    private String addAuthenticationToken(String baseUrl) throws IOException {
        User user = EnvironmentContext.getCurrent().getUser();
        if (user != null) {
            return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "token=" + user.getToken();
        } else {
            throw new IOException("Authentication token not found");
        }
    }

    private String request(String path, String method) throws IOException {
        String resourceUrl = addAuthenticationToken(baseUrl + path);
        final HttpURLConnection conn = (HttpURLConnection)new URL(resourceUrl).openConnection();

        //TODO remove
        conn.setRequestProperty("Cookie",
                                "session-access-key=TOrraXObKjTKS4Gr48SS40Pbr8eOPmLCK4zWGDOnDGXDm1iLXTfOaPfXvKiySWTWu90S0OfaDDLiKz50SeuD4ayGDDTyqmyLbO4P95rPTuHeDaH1bSy95HuivLT1WnuGTzaDKfePrOCTOyCO9DPDr8Of09C9ff1vuzby8aCeqyj1KLrmrWH14uXOznz8yii8rW94rOW1SSfKDeC0Gy549L0yzfjfyyuf49imuHvODniPWOWKLLqaXirD4C0ebPrD0zK5r1mXqnGOji5jbD9T8KaTyuOWjmzDjyLneyL8PrbS0GyLTGviTuaHDPOKXeT4WOerSeLu1y9H4Lnfy9OHernqC1LujGzHaiCKPyG9vG5jPfKOHfyb8DOqTmOmu9b8OXeO8qHu09Kb4SrnSGajf8yHqWK1vaCOPTDzvfDyKyLq4CKzbmmbz4XmCXGDPriDjzavWq0iDbaSvO8quiCinzbHrbPTjz4SbT5v8eziP1DTyzn9yjOOP1LH9fGWqeuO;");
        //TODO ------


        conn.setConnectTimeout(30 * 1000);
        try {
            conn.setRequestMethod(method);
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

            return IoUtil.readAndCloseQuietly(conn.getInputStream());
        } finally {
            conn.disconnect();
        }
    }
}
