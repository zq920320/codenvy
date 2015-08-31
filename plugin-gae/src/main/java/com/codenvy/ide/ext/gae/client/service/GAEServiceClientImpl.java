/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.client.service;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncRequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAERequestCallback;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;
import com.codenvy.ide.ext.gae.shared.GAEMavenInfo;
import com.codenvy.ide.ext.gae.shared.YamlParameterInfo;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static com.google.gwt.http.client.RequestBuilder.GET;

/**
 * Contains methods which contains business logic to send requests on server and updates or changes state of project. Also the
 * class contains methods which allow send request on server to login or logout user.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 * @author Andrey Plotnikov
 */
@Singleton
public class GAEServiceClientImpl implements GAEServiceClient {

    private final String restContext;
    private final String pathToWorkSpace;
    private final String pathToGAEMavenService;
    private final String pathToUser;
    private final String pathToValidator;

    private final MessageBus          messageBus;
    private final AsyncRequestFactory asyncRequestFactory;
    private final AsyncRequestLoader  loader;

    @Inject
    public GAEServiceClientImpl(@Named("workspaceId") String wsId,
                                @RestContext String restContext,
                                MessageBus messageBus,
                                AsyncRequestFactory asyncRequestFactory,
                                AsyncRequestLoader loader) {

        this.restContext = restContext;
        this.messageBus = messageBus;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loader = loader;

        pathToUser = restContext + "/" + "appengine/" + wsId + "/user";
        pathToWorkSpace = "/appengine/" + wsId;
        pathToGAEMavenService = "/gae-parameters/" + wsId;
        pathToValidator = "/gae-validator/" + wsId;
    }

    /** {@inheritDoc} */
    @Override
    public void update(@Nonnull String projectPath, @Nullable String bin, @Nonnull GAERequestCallback<ApplicationInfo> callback) {
        String url = pathToWorkSpace + "/update";

        boolean isBinExist = bin != null && !bin.isEmpty();

        String urlWithParams = url + "?projectpath=" + projectPath + (isBinExist ? "&bin=" + bin : "");

        Message message = new MessageBuilder(GET, urlWithParams).header(ACCEPT, APPLICATION_JSON).build();

        try {
            messageBus.send(message, callback);
        } catch (WebSocketException e) {
            callback.onFailure(e);
        }
        //TODO we can't test this method, because WebSocket classes have many native JavaScript code we have many problem with this code.
        //TODO It is reference to issue https://jira.codenvycorp.com/browse/IDEX-1670
    }

    /** {@inheritDoc} */
    @Override
    public void getLoggedUser(@Nonnull GAEAsyncRequestCallback<OAuthToken> callback) {
        String url = pathToUser;

        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void readGAEMavenParameters(@Nonnull String projectPath, @Nonnull GAEAsyncRequestCallback<GAEMavenInfo> callback) {
        String requestUrl = restContext + pathToGAEMavenService + "/read/maven?projectpath=" + projectPath;

        asyncRequestFactory.createGetRequest(requestUrl).header(ACCEPT, APPLICATION_JSON).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void readGAEYamlParameters(@Nonnull String projectPath, @Nonnull GAEAsyncRequestCallback<YamlParameterInfo> callback) {
        String requestUrl = restContext + pathToGAEMavenService + "/read/yaml?projectpath=" + projectPath;

        asyncRequestFactory.createGetRequest(requestUrl).header(ACCEPT, APPLICATION_JSON).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void validateProject(@Nonnull String projectPath, @Nonnull GAEAsyncRequestCallback<Void> callback) {
        String requestUrl = restContext + pathToValidator + "/validate?projectpath=" + projectPath;

        asyncRequestFactory.createGetRequest(requestUrl).header(ACCEPT, APPLICATION_JSON).loader(loader).send(callback);
    }

}