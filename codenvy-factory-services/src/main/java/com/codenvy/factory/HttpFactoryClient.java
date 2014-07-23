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

import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    public Factory getFactory(String factoryId) throws ApiException {
        try {
            Link link = DtoFactory.getInstance().createDto(Link.class)
                                  .withHref(protocol + host + port + "/api/factory/" + factoryId)
                                  .withMethod("GET");

            return HttpJsonHelper.request(Factory.class, link);

        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ConflictException(e.getLocalizedMessage());
        }
    }
}
