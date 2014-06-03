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
package com.codenvy.factory;

import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Retrieve factory parameters over http connection. */
public class HttpFactoryClient implements FactoryClient {
    private static final Logger LOG = LoggerFactory.getLogger(HttpFactoryClient.class);
    private final String protocol;
    private final String host;
    private final int    port;


    public HttpFactoryClient(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    @Override
    public Factory getFactory(String factoryId) throws FactoryUrlException {
        HttpURLConnection conn = null;
        try {

            conn = (HttpURLConnection)new URL(protocol, host, port, "/api/factory/" + factoryId).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);

            int responseCode = conn.getResponseCode();
            if (responseCode / 100 != 2) {
                InputStream errorStream = conn.getErrorStream();
                String message = errorStream != null ? IoUtil.readAndCloseQuietly(errorStream) : "";

                if (String.format("Factory URL with id %s is not found.", factoryId).equals(message)) {
                    return null;
                }

                throw new FactoryUrlException(responseCode, message);
            }

            return DtoFactory.getInstance().createDtoFromJson(conn.getInputStream(), Factory.class);

        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
