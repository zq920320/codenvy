/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.github.server;

import org.eclipse.che.api.auth.shared.dto.Token;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Wrapper class for calls to Codenvy factory REST API
 *
 * @author Stephane Tournie
 */
public class FactoryConnection {

    private static final Logger LOG = LoggerFactory.getLogger(FactoryConnection.class);

    private final String baseUrl;

    @Inject
    public FactoryConnection(@Named("api.endpoint") String baseUrl) {
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
        try {
            if (userToken != null) {
                Pair tokenParam = Pair.of("token", userToken.getValue());
                factory = HttpJsonHelper.get(Factory.class, url, tokenParam);
            } else {
                factory = HttpJsonHelper.get(Factory.class, url);
            }
        } catch (IOException | ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException e) {
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
        try {
            if (userToken != null) {
                final Pair tokenParam = Pair.of("token", userToken.getValue());
                newFactory = HttpJsonHelper.put(Factory.class, url, factory, tokenParam);
            } else {
                newFactory = HttpJsonHelper.put(Factory.class, url, factory);
            }
        } catch (IOException | ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return newFactory;
    }

    /**
     * Get value of a given factory link
     *
     * @param factoryLinks
     *         the factory links
     * @param rel
     *         the name of the link to get
     * @return the value of given factory link if exist, an empty {@link Optional} otherwise
     */
    public static Optional<String> getFactoryUrl(final List<Link> factoryLinks, String rel) {
        List<Link> createProjectLinks = factoryLinks.stream()
                                                    .filter(link -> rel.equals(link.getRel())).collect(Collectors.toList());
        if (!createProjectLinks.isEmpty()) {
            return Optional.of(createProjectLinks.get(0).getHref());
        }
        return Optional.empty();
    }
}
