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
package com.codenvy.ide.ext.gae.server.rest;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import com.codenvy.ide.ext.gae.server.AppEngineClient;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;

import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APP_ENGINE_WEB_XML_PATH;
import static java.io.File.separator;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class AppEngineServiceTest {

    private static final String WORKSPACE_ID     = "workspaceId";
    private static final String APP_ID_PARAMETER = "s-app-id";
    private static final String APP_ID           = "app-id";
    private static final String BASE_URI         = "http://localhost";
    private static final String USER_ID          = "userId";
    private static final String SOME_TEXT        = "some text";
    private static final String PROJECT_PATH     = "projectPath";
    private static final String APP_YAML         = "app.yaml";
    public static final  String CREATED_MESSAGE  = "Your application has been created.";
    public static final  String ERROR_MESSAGE    = "Unable to modify App Engine application settings.";

    @Mock
    private AppEngineClient           client;
    @Mock
    private VirtualFileSystemRegistry vfsRegistry;
    @Mock
    private OAuthTokenProvider        oauthTokenProvider;
    @Mock
    private VirtualFileSystemProvider vfsProvider;
    @Mock
    private MountPoint                mountPoint;
    @Mock
    private GAEServerUtil             gaeServerUtil;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VirtualFile               virtualFile;

    @InjectMocks
    private AppEngineService service;

    private ResourceLauncher launcher;

    @Before
    public void setUp() throws Exception {
        DependencySupplierImpl dependencies = new DependencySupplierImpl();

        dependencies.addComponent(AppEngineClient.class, client);
        dependencies.addComponent(VirtualFileSystemRegistry.class, vfsRegistry);
        dependencies.addComponent(OAuthTokenProvider.class, oauthTokenProvider);
        dependencies.addComponent(GAEServerUtil.class, gaeServerUtil);

        ResourceBinder resources = new ResourceBinderImpl();
        resources.addResource(AppEngineService.class, null);

        EverrestProcessor processor = new EverrestProcessor(resources, ProviderBinder.getInstance(), dependencies);

        launcher = new ResourceLauncher(processor);

        EnvironmentContext environmentContext = mock(EnvironmentContext.class, RETURNS_DEEP_STUBS);
        EnvironmentContext.setCurrent(environmentContext);

        when(environmentContext.getUser().getId()).thenReturn(USER_ID);
        when(vfsRegistry.getProvider(WORKSPACE_ID)).thenReturn(vfsProvider);
        when(vfsProvider.getMountPoint(false)).thenReturn(mountPoint);
    }

    private ContainerResponse sendRequest(String method, String path, byte[] data) throws Exception {
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put("Content-Type", Arrays.asList(MediaType.APPLICATION_JSON));

        return launcher.service(method, "/appengine/" + WORKSPACE_ID + '/' + path, BASE_URI, headers, data, null);
    }

    @Test
    public void getUserRequestShouldReturnUser() throws Exception {
        OAuthToken authToken = DtoFactory.getInstance().createDto(OAuthToken.class).withScope(SOME_TEXT).withToken(SOME_TEXT);
        when(oauthTokenProvider.getToken(anyString(), anyString())).thenReturn(authToken);

        ContainerResponse response = sendRequest("GET", "user", null);

        verify(oauthTokenProvider).getToken("google", USER_ID);

        assertThat(response.getStatus(), is(200));

        OAuthToken token = (OAuthToken)response.getEntity();
        assertThat(token.getToken(), equalTo(SOME_TEXT));
        assertThat(token.getScope(), equalTo(SOME_TEXT));
    }

    @Test
    public void getUserRequestShouldReturnEmptyUser() throws Exception {
        //noinspection unchecked
        when(oauthTokenProvider.getToken(anyString(), anyString())).thenThrow(IOException.class);

        ContainerResponse response = sendRequest("GET", "user", null);

        verify(oauthTokenProvider).getToken("google", USER_ID);

        assertThat(response.getStatus(), is(200));

        OAuthToken token = (OAuthToken)response.getEntity();

        assertThat(token.getToken(), nullValue());
        assertThat(token.getScope(), nullValue());
    }

    @Test
    public void getUserRequestShouldReturnEmptyUserIfTokenIsNull() throws Exception {
        when(oauthTokenProvider.getToken(anyString(), anyString())).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "user", null);

        verify(oauthTokenProvider).getToken("google", USER_ID);

        assertThat(response.getStatus(), is(200));

        OAuthToken token = (OAuthToken)response.getEntity();
        assertThat(token.getToken(), nullValue());
        assertThat(token.getScope(), nullValue());
    }

    @Test
    public void updateApplicationRequestShouldBeSent() throws Exception {
        ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);

        ApplicationInfo applicationInfo =
                DtoFactory.getInstance().createDto(ApplicationInfo.class).withApplicationId(SOME_TEXT).withWebURL(BASE_URI);

        when(client.update(anyString(), anyString(), any(URL.class), anyString())).thenReturn(applicationInfo);

        ContainerResponse response = sendRequest("GET", "update?projectpath=" + PROJECT_PATH + "&bin=" + BASE_URI, null);

        verify(client).update(eq(WORKSPACE_ID), eq(PROJECT_PATH), urlCaptor.capture(), eq(USER_ID));

        URL bin = urlCaptor.getValue();
        assertThat(bin.toString(), equalTo(BASE_URI));

        ApplicationInfo entity = (ApplicationInfo)response.getEntity();
        assertThat(entity.getApplicationId(), equalTo(SOME_TEXT));
        assertThat(entity.getWebURL(), equalTo(BASE_URI));
    }

    @Test
    public void appIdShouldBeChangedIntoWebXml() throws Exception {
        when(mountPoint.getVirtualFile(PROJECT_PATH + separator + APP_ENGINE_WEB_XML_PATH)).thenReturn(virtualFile);

        ContainerResponse response = sendRequest("GET", "change-appid?app_id=" + APP_ID_PARAMETER + "&projectpath=" + PROJECT_PATH, null);

        verify(gaeServerUtil).setApplicationIdToWebAppEngine(virtualFile, APP_ID);
        verify(gaeServerUtil, never()).setApplicationIdToAppYaml(virtualFile, APP_ID);

        assertThat(response.getStatus(), is(200));
        assertThat(response.getEntity().toString(), containsString(CREATED_MESSAGE));
    }

    @Test
    public void appIdShouldBeChangedIntoYaml() throws Exception {
        when(mountPoint.getVirtualFile(PROJECT_PATH + separator + APP_ENGINE_WEB_XML_PATH)).thenThrow(new NotFoundException(""));
        when(mountPoint.getVirtualFile(PROJECT_PATH + separator + APP_YAML)).thenReturn(virtualFile);

        ContainerResponse response = sendRequest("GET", "change-appid?app_id=" + APP_ID_PARAMETER + "&projectpath=" + PROJECT_PATH, null);

        verify(gaeServerUtil).setApplicationIdToAppYaml(virtualFile, APP_ID);

        assertThat(response.getStatus(), is(200));
        assertThat(response.getEntity().toString(), containsString(CREATED_MESSAGE));
    }

    @Test
    public void responseShouldBeContainedServerErrorIfAppIdDoesNotUpdated() throws Exception {
        when(mountPoint.getVirtualFile(PROJECT_PATH + separator + APP_ENGINE_WEB_XML_PATH)).thenThrow(new NotFoundException(""));
        when(mountPoint.getVirtualFile(PROJECT_PATH + separator + APP_YAML)).thenThrow(new NotFoundException(""));

        ContainerResponse response = sendRequest("GET", "change-appid?app_id=" + APP_ID_PARAMETER + "&projectpath=" + PROJECT_PATH, null);

        assertThat(response.getStatus(), is(500));
        assertThat(response.getEntity().toString(), equalTo(ERROR_MESSAGE));
    }

}