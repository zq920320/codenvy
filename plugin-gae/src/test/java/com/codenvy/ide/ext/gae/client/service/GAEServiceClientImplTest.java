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
import com.codenvy.ide.ext.gae.shared.GAEMavenInfo;
import com.codenvy.ide.ext.gae.shared.YamlParameterInfo;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.websocket.MessageBus;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class GAEServiceClientImplTest {

    private static final String WORKSPACE_ID = "wsID";
    private static final String REST_CONTEXT = "restContext";
    private static final String SOME_TEXT    = "someText";

    @Mock
    private MessageBus          messageBus;
    @Mock
    private AsyncRequestFactory asyncRequestFactory;
    @Mock
    private AsyncRequestLoader  loader;

    @Mock
    private AsyncRequest                               request;
    @Mock
    private GAEAsyncRequestCallback<OAuthToken>        callback;
    @Mock
    private GAEAsyncRequestCallback<Void>              validateCallback;
    @Mock
    private GAEAsyncRequestCallback<GAEMavenInfo>      gaeMavenInfoCallback;
    @Mock
    private GAEAsyncRequestCallback<YamlParameterInfo> yamlParameterInfoCallback;

    private GAEServiceClient service;

    @Before
    public void setUp() throws Exception {
        service = new GAEServiceClientImpl(WORKSPACE_ID, REST_CONTEXT, messageBus, asyncRequestFactory, loader);

        when(asyncRequestFactory.createGetRequest(anyString())).thenReturn(request);

        when(request.loader(loader)).thenReturn(request);
        when(request.header(ACCEPT, APPLICATION_JSON)).thenReturn(request);
    }

    @Test
    public void logUserRequestShouldBeExecuted() throws Exception {
        service.getLoggedUser(callback);

        verify(asyncRequestFactory).createGetRequest("restContext/appengine/wsID/user");
        verify(request).loader(loader);
        verify(request).send(callback);
    }

    @Test
    public void requestToReadGaeMavenParametersShouldBeSent() throws Exception {
        final String requestUrl = REST_CONTEXT + "/gae-parameters/" + WORKSPACE_ID + "/read/maven?projectpath=" + SOME_TEXT;

        service.readGAEMavenParameters(SOME_TEXT, gaeMavenInfoCallback);

        verify(asyncRequestFactory).createGetRequest(requestUrl);
        verify(request).header(ACCEPT, APPLICATION_JSON);
        verify(request).loader(loader);
        verify(request).send(gaeMavenInfoCallback);
    }

    @Test
    public void requestToReadGaeYamlParametersShouldBeSent() throws Exception {
        final String requestUrl = REST_CONTEXT + "/gae-parameters/" + WORKSPACE_ID + "/read/yaml?projectpath=" + SOME_TEXT;

        service.readGAEYamlParameters(SOME_TEXT, yamlParameterInfoCallback);

        verify(asyncRequestFactory).createGetRequest(requestUrl);
        verify(request).header(ACCEPT, APPLICATION_JSON);
        verify(request).loader(loader);
        verify(request).send(yamlParameterInfoCallback);
    }

    @Test
    public void requestToValidateProjectShouldBeSet() throws Exception {
        final String url = REST_CONTEXT + "/gae-validator/" + WORKSPACE_ID + "/validate?projectpath=" + SOME_TEXT;

        service.validateProject(SOME_TEXT, validateCallback);

        verify(asyncRequestFactory).createGetRequest(url);
        verify(request).header(ACCEPT, APPLICATION_JSON);
        verify(request).loader(loader);
        verify(request).send(validateCallback);
    }

}