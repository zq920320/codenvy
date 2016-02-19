/*
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
package com.codenvy.plugin.webhooks;

import com.codenvy.plugin.webhooks.vsts.shared.VSTSDocument;
import com.codenvy.plugin.webhooks.vsts.VSTSWebhookService;
import com.google.common.collect.Lists;

import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Unit tests for TestVSTSWebhookService
 *
 * @author Stephane Tournie
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVSTSWebhookService {

    private final static String FAKE_USER_ID = "TEST_USER_ID";

    private VSTSWebhookService fakeVSTSWebhookService;

    @Before
    public void setUp() throws Exception {
        // Prepare authConnection
        Token fakeToken = DtoFactory.newDto(Token.class).withValue("fakeToken");
        AuthConnection mockAuthConnection = mock(AuthConnection.class);
        when(mockAuthConnection.authenticateUser("somebody@somemail.com", "somepwd")).thenReturn(fakeToken);

        // Prepare userConnection
        UserConnection mockUserConnection = mock(UserConnection.class);
        UserDescriptor mockUser = mock(UserDescriptor.class);
        when(mockUser.getId()).thenReturn(FAKE_USER_ID);
        when(mockUserConnection.getCurrentUser()).thenReturn(mockUser);

        // Prepare factoryConnection
        FactoryConnection mockFactoryConnection = mock(FactoryConnection.class);
        Factory VSTSfakeFactory = DtoFactory.getInstance().createDtoFromJson(resourceToString("factory-codenvy.json"), Factory.class);
        when(mockFactoryConnection.findFactory("codenvy", FAKE_USER_ID)).thenReturn(Lists.newArrayList(VSTSfakeFactory));
        when(mockFactoryConnection.saveFactory(VSTSfakeFactory)).thenReturn(VSTSfakeFactory);

        HttpJsonRequestFactory fakeHttpJsonRequestFactory = mock(HttpJsonRequestFactory.class);
        HttpJsonRequest fakeHttpJsonRequest = mock(HttpJsonRequest.class);
        when(fakeHttpJsonRequest.usePutMethod()).thenReturn(fakeHttpJsonRequest);
        when(fakeHttpJsonRequest.setBody(DtoFactory.newDto(VSTSDocument.class).withId("WI9-develop-factory").withValue(
                "http://internal.codenvycorp.com/f?name=factory-mktg-341&user=useraxi5p0fe2mlmmf3r").withEtag("-1")))
                .thenReturn(fakeHttpJsonRequest);
        when(fakeHttpJsonRequest.setBody(DtoFactory.newDto(VSTSDocument.class).withId("WI9-review-factory").withValue(
                "http://internal.codenvycorp.com/f?name=factory-mktg-341&user=useraxi5p0fe2mlmmf3r").withEtag("-1")))
                .thenReturn(fakeHttpJsonRequest);
        when(fakeHttpJsonRequest.setAuthorizationHeader(anyString())).thenReturn(fakeHttpJsonRequest);
        when(fakeHttpJsonRequest.addQueryParam(anyString(), anyString())).thenReturn(fakeHttpJsonRequest);
        HttpJsonResponse fakeHttpJsonResponse = mock(HttpJsonResponse.class);
        when(fakeHttpJsonResponse.asDto(VSTSDocument.class)).thenReturn(
                DtoFactory.newDto(VSTSDocument.class).withId("testID").withValue("testValue").withEtag("-1"));
        when(fakeHttpJsonRequest.request()).thenReturn(fakeHttpJsonResponse);
        when(fakeHttpJsonRequestFactory.fromUrl(anyString())).thenReturn(fakeHttpJsonRequest);

        // Prepare VSTSWebhookService
        fakeVSTSWebhookService =
                new VSTSWebhookService(mockAuthConnection, mockFactoryConnection, mockUserConnection, fakeHttpJsonRequestFactory);
    }

    @Test
    public void testVSTSWebhookWorkItemCreatedEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest("work_item_created");
        Response response = fakeVSTSWebhookService.handleVSTSWebhookEvent(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    protected HttpServletRequest prepareRequest(String eventType) throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        String eventMessageString = null;
        switch (eventType) {
            case "work_item_created":
                eventMessageString = resourceToString("vsts-work-item-created-event.json");
                break;
            default:
                break;
        }
        ServletInputStream fakeInputStream = null;
        if (eventMessageString != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(eventMessageString.getBytes(StandardCharsets.UTF_8));
            fakeInputStream = new ServletInputStream() {
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }
            };
        }
        when(mockRequest.getInputStream()).thenReturn(fakeInputStream);

        return mockRequest;
    }

    private String resourceToString(String resource) throws Exception {
        final Path resourcePath =
                Paths.get(Thread.currentThread().getContextClassLoader().getResource(resource).toURI());
        return new String(Files.readAllBytes(resourcePath));
    }
}
