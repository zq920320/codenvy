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
import com.codenvy.ide.ext.gae.server.rest.GAEParametersService;
import com.codenvy.ide.ext.gae.server.rest.GAEValidateService;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;
import com.codenvy.ide.ext.gae.shared.GAEMavenInfo;
import com.codenvy.ide.ext.gae.shared.YamlParameterInfo;
import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Client service for managing Google App Engine.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Andrey Plotnikov
 */
@ImplementedBy(GAEServiceClientImpl.class)
public interface GAEServiceClient {

    /**
     * Creates message with specific url and sends GET request using {@link com.codenvy.ide.websocket.MessageBus} to update.
     *
     * @param projectPath
     *         path to active project
     * @param bin
     *         url where binaries of project is located
     * @param callback
     *         callback which need send in request
     */
    void update(@NotNull String projectPath, @Nullable String bin, @NotNull GAERequestCallback<ApplicationInfo> callback);

    /**
     * Creates and sends GET request on server to get info about current user.If user is not logged we redirect on special
     * page to login or create account.
     *
     * @param callback
     *         callback which need send in request
     */
    void getLoggedUser(@NotNull GAEAsyncRequestCallback<OAuthToken> callback);

    /**
     * Method send request to special service {@link GAEParametersService} to get parameters
     * (maven parameters: groupId, artifactId, version, packaging; app engine parameters:applicationId) about GAE project.
     *
     * @param projectPath
     *         path to needed project
     * @param callback
     *         need to send response with parameters
     */
    void readGAEMavenParameters(@NotNull String projectPath, @NotNull GAEAsyncRequestCallback<GAEMavenInfo> callback);

    /**
     * Method send request to special service {@link GAEParametersService} to get parameters from .yaml file (applicationId) about GAE
     * project.
     *
     * @param projectPath
     *         path to needed project
     * @param callback
     *         need to send response with parameters
     */
    void readGAEYamlParameters(@NotNull String projectPath, @NotNull GAEAsyncRequestCallback<YamlParameterInfo> callback);

    /**
     * Method send request to special service {@link GAEValidateService} to validate current project.
     *
     * @param projectPath
     *         path to needed project
     * @param callback
     *         need to send response with validation info
     */
    void validateProject(@NotNull String projectPath, @NotNull GAEAsyncRequestCallback<Void> callback);

}