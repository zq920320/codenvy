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
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Unit tests for VersionControlMonitorService
 *
 * @author Stephane Tournie
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVersionControlMonitorService {

    private final static String REQUEST_HEADER_GITHUB_EVENT = "X-GitHub-Event";

    private VersionControlMonitorService fakeVersionControlMonitorService;

    @Before
    public void setUp() throws Exception {
        // Prepare authConnection
        Token fakeToken = DtoFactory.newDto(Token.class).withValue("fakeToken");
        AuthConnection mockAuthConnection = mock(AuthConnection.class);
        when(mockAuthConnection.authenticateUser("somebody@somemail.com", "somepwd")).thenReturn(fakeToken);

        // Prepare factoryConnection
        final Path factoryResourcePath =
                Paths.get(Thread.currentThread().getContextClassLoader().getResource("factory-MKTG-341.json").toURI());
        Factory
                fakeFactory = DtoFactory.getInstance().createDtoFromJson(new String(Files.readAllBytes(factoryResourcePath)),
                                                                         Factory.class);
        FactoryConnection mockFactoryConnection = mock(FactoryConnection.class);
        when(mockFactoryConnection.getFactory("fakeFactoryId", fakeToken)).thenReturn(fakeFactory);
        when(mockFactoryConnection.updateFactory(fakeFactory, fakeToken)).thenReturn(fakeFactory);

        // Prepare VersionControlMonitorService
        fakeVersionControlMonitorService =
                new VersionControlMonitorService(mockAuthConnection, mockFactoryConnection);
    }

    @Test
    public void testGithubWebhookPushEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest("push");
        Response response = fakeVersionControlMonitorService.githubWebhook(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    @Test
    public void testGithubWebhookPullRequestEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest("pull_request");
        Response response = fakeVersionControlMonitorService.githubWebhook(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    protected HttpServletRequest prepareRequest(String eventType) throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        String githubEventString = null;
        switch (eventType) {
            case "pull_request":
                final Path PRResourcePath =
                        Paths.get(Thread.currentThread().getContextClassLoader().getResource("pull_request_event.json").toURI());
                githubEventString = new String(Files.readAllBytes(PRResourcePath));
                break;
            case "push":
                final Path PushResourcePath =
                        Paths.get(Thread.currentThread().getContextClassLoader().getResource("push_event.json").toURI());
                githubEventString = new String(Files.readAllBytes(PushResourcePath));
                break;
            default:
                break;
        }
        ServletInputStream fakeInputStream = null;
        if (githubEventString != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(githubEventString.getBytes(StandardCharsets.UTF_8));
            fakeInputStream = new ServletInputStream() {
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }
            };
        }
        when(mockRequest.getHeader(REQUEST_HEADER_GITHUB_EVENT)).thenReturn(eventType);
        when(mockRequest.getInputStream()).thenReturn(fakeInputStream);

        return mockRequest;
    }
}
