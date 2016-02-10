/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.github.server;

import org.eclipse.che.api.auth.shared.dto.Token;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Wrapper class for calls to Codenvy factory REST API
 *
 * @author Stephane Tournie
 */
public class FactoryConnection {

    private static final Logger LOG = LoggerFactory.getLogger(FactoryConnection.class);

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 baseUrl;

    @Inject
    public FactoryConnection(HttpJsonRequestFactory httpJsonRequestFactory, @Named("api.endpoint") String baseUrl) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.baseUrl = baseUrl;
    }

    /**
     * Get a given factory
     *
     * @param factoryId
     *         the id of the factory
     * @param userToken
     *         the authentication token to use against the Codenvy API
     * @return the expected factory or null if an error occurred during the call to 'getFactory'
     * @throws ServerException
     */
    public Factory getFactory(String factoryId, Token userToken) throws ServerException {
        String url = fromUri(baseUrl).path(FactoryService.class).path(FactoryService.class, "getFactory")
                                     .build(factoryId).toString();
        Factory factory;
        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url).useGetMethod();
        try {
            if (userToken != null) {
                httpJsonRequest.addQueryParam("token", userToken.getValue());
            }
            HttpJsonResponse response = httpJsonRequest.request();
            factory = response.asDto(Factory.class);

        } catch (IOException | ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException | BadRequestException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return factory;
    }

    /**
     * Update a given factory
     *
     * @param factory
     *         the factory to update
     * @param userToken
     *         the authentication token to use against the Codenvy API
     * @return the updated factory or null if an error occurred during the call to 'updateFactory'
     * @throws ServerException
     */
    public Factory updateFactory(Factory factory, Token userToken) throws ServerException {
        final String factoryId = factory.getId();
        final String url = fromUri(baseUrl).path(FactoryService.class).path(FactoryService.class, "updateFactory")
                                           .build(factoryId).toString();

        Factory newFactory;
        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url).usePutMethod().setBody(factory);
        try {
            if (userToken != null) {
                httpJsonRequest.addQueryParam("token", userToken.getValue());
            }
            HttpJsonResponse response = httpJsonRequest.request();
            newFactory = response.asDto(Factory.class);

        } catch (IOException | ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException | BadRequestException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return newFactory;
    }
}
