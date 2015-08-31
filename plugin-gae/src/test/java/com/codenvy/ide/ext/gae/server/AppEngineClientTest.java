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
package com.codenvy.ide.ext.gae.server;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import com.codenvy.ide.ext.gae.server.applications.JavaApplication;
import com.codenvy.ide.ext.gae.server.applications.yaml.PHPApplication;
import com.codenvy.ide.ext.gae.server.applications.yaml.PythonApplication;
import com.codenvy.ide.ext.gae.server.inject.factories.ApplicationFactory;
import com.codenvy.ide.ext.gae.server.inject.factories.IdeAppAdminFactory;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;
import com.google.appengine.tools.admin.GenericApplication;
import com.google.appengine.tools.admin.IdeAppAdmin;
import com.google.appengine.tools.admin.UpdateFailureEvent;
import com.google.appengine.tools.admin.UpdateProgressEvent;
import com.google.appengine.tools.admin.UpdateSuccessEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.eclipse.che.api.project.shared.Constants.BLANK_PROJECT_TYPE;
import static com.codenvy.ide.ext.gae.TestUtil.getContent;
import static com.codenvy.ide.ext.gae.server.AppEngineClient.DUMMY_UPDATE_LISTENER;
import static com.codenvy.ide.ext.gae.server.AppEngineClient.DummyUpdateListener;
import static com.google.appengine.tools.admin.AppAdminFactory.ConnectOptions;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class AppEngineClientTest {

    private static final String SOME_TEXT = "someText";
    private static final String WEB_URL   = "http://someText.appspot.com";

    private static final String PATH_TO_YAML_CONTENT = "/template/yaml/";

    private static final String WORKSPACE_ID = "workspaceId";
    private static final String PROJECT_PATH = "projectPath";
    private static final String USER_ID      = "userId";

    @Captor
    private ArgumentCaptor<JavaApplication>     javaApplicationCaptor;
    @Captor
    private ArgumentCaptor<ConnectOptions>      connectOptionsCaptor;
    @Captor
    private ArgumentCaptor<DummyUpdateListener> dummyUpdateListenerCaptor;

    //constructor mocks
    @Mock
    private OAuthTokenProvider oauthTokenProvider;
    @Mock
    private ProjectManager     projectManager;
    @Mock
    private ApplicationFactory applicationFactory;
    @Mock
    private IdeAppAdminFactory appAdminFactory;

    //additional mocks
    @Mock(answer = RETURNS_DEEP_STUBS)
    private Project            project;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFileEntry   fileEntry;
    @Mock
    private VirtualFile        virtualFile;
    @Mock
    private InputStream        inputStream;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private IdeAppAdmin        admin;
    @Mock
    private OAuthToken         token;
    @Mock
    private GenericApplication genericApplication;
    @Mock
    private PHPApplication     phpApplication;
    @Mock
    private PythonApplication  pythonApplication;
    @Mock
    private JavaApplication    javaApplication;

    @InjectMocks
    private AppEngineClient appEngineClient;

    @Before
    public void setUp() throws Exception {
        when(projectManager.getProject(WORKSPACE_ID, PROJECT_PATH)).thenReturn(project);
        when(appAdminFactory.createIdeAppAdmin(any(ConnectOptions.class), any(GenericApplication.class))).thenReturn(admin);
        when(admin.getApplication()).thenReturn(genericApplication);
        when(genericApplication.getAppId()).thenReturn(SOME_TEXT);
        when(oauthTokenProvider.getToken("google", USER_ID)).thenReturn(token);
        when(token.getToken()).thenReturn(SOME_TEXT);
        when(project.getConfig().getTypeId()).thenReturn(BLANK_PROJECT_TYPE);
    }

    @Test
    public void pythonApplicationShouldBeUpdated() throws Exception {
        prepareGeneralMocksForProjectWithoutBinaries();
        when(fileEntry.getVirtualFile().getContent()).thenReturn(getContent(AppEngineClientTest.class, PATH_TO_YAML_CONTENT + "appYaml"));
        when(virtualFile.zip(VirtualFileFilter.ALL)).thenReturn(getContent(AppEngineClientTest.class, PATH_TO_YAML_CONTENT + "appYaml"));
        when(applicationFactory.createPythonApplication(any(File.class))).thenReturn(pythonApplication);

        ApplicationInfo applicationInfo = appEngineClient.update(WORKSPACE_ID, PROJECT_PATH, null, USER_ID);

        verifyGeneralMocksForProjectWithoutBinaries();
        verify(appAdminFactory).createIdeAppAdmin(any(ConnectOptions.class), eq(pythonApplication));
        verify(applicationFactory).createPythonApplication(any(File.class));

        assertThat(applicationInfo.getApplicationId(), equalTo(SOME_TEXT));
        assertThat(applicationInfo.getWebURL(), equalTo(WEB_URL));
    }

    private void prepareGeneralMocksForProjectWithoutBinaries() throws Exception {
        when(project.getBaseFolder().getChild("app.yaml")).thenReturn(fileEntry);
        when(project.getBaseFolder().getVirtualFile()).thenReturn(virtualFile);

    }

    private void verifyGeneralMocksForProjectWithoutBinaries() throws Exception {
        verify(projectManager, times(2)).getProject(WORKSPACE_ID, PROJECT_PATH);
        verify(project.getBaseFolder()).getChild("app.yaml");
        verify(fileEntry.getVirtualFile()).getContent();
        verify(project.getBaseFolder()).getVirtualFile();
        verify(virtualFile).zip(VirtualFileFilter.ALL);
        verify(oauthTokenProvider).getToken("google", USER_ID);
        verify(token).getToken();
        verify(admin).update(DUMMY_UPDATE_LISTENER);
        verify(admin.getApplication()).getAppId();
    }

    @Test(expected = RuntimeException.class)
    public void runtimeExceptionShouldBeThrownWhenAppTypeIsUnknown() throws Exception {
        when(project.getBaseFolder().getChild("app.yaml")).thenReturn(fileEntry);
        when(fileEntry.getVirtualFile().getContent()).thenReturn(getContent(AppEngineClientTest.class,
                                                                            PATH_TO_YAML_CONTENT + "unknownYaml"));

        appEngineClient.update(WORKSPACE_ID, PROJECT_PATH, null, USER_ID);
    }

    @Test(expected = RuntimeException.class)
    public void runtimeExceptionShouldBeThrownWhenYamlNotExist() throws Exception {
        when(project.getBaseFolder().getChild("app.yaml")).thenReturn(null);

        appEngineClient.update(WORKSPACE_ID, PROJECT_PATH, null, USER_ID);
    }

    @Test
    public void phpApplicationShouldBeUpdated() throws Exception {
        prepareGeneralMocksForProjectWithoutBinaries();
        when(fileEntry.getVirtualFile().getContent()).thenReturn(getContent(AppEngineClientTest.class, PATH_TO_YAML_CONTENT + "phpYaml"));
        when(virtualFile.zip(VirtualFileFilter.ALL)).thenReturn(getContent(AppEngineClientTest.class, PATH_TO_YAML_CONTENT + "phpYaml"));
        when(applicationFactory.createPHPApplication(any(File.class))).thenReturn(phpApplication);

        appEngineClient.update(WORKSPACE_ID, PROJECT_PATH, null, USER_ID);

        verifyGeneralMocksForProjectWithoutBinaries();
        verify(appAdminFactory).createIdeAppAdmin(any(ConnectOptions.class), eq(phpApplication));
        verify(applicationFactory).createPHPApplication(any(File.class));
    }

    @Test
    public void javaApplicationShouldBeUpdated() throws Exception {
        when(applicationFactory.createJavaApplication(any(URL.class))).thenReturn(javaApplication);

        ApplicationInfo applicationInfo = appEngineClient.update(WORKSPACE_ID,
                                                                 PROJECT_PATH,
                                                                 AppEngineClientTest.class.getResource("/template/java.war"),
                                                                 USER_ID);

        verify(appAdminFactory).createIdeAppAdmin(connectOptionsCaptor.capture(), javaApplicationCaptor.capture());

        ConnectOptions options = connectOptionsCaptor.getValue();

        assertThat(options.getOauthToken(), equalTo(SOME_TEXT));
        assertThat(javaApplicationCaptor.getValue(), equalTo(javaApplication));

        verify(admin).update(DUMMY_UPDATE_LISTENER);
        verify(admin.getApplication()).cleanStagingDirectory();
        verify(projectManager).getProject(WORKSPACE_ID, PROJECT_PATH);

        assertThat(applicationInfo.getWebURL(), equalTo(WEB_URL));
        assertThat(applicationInfo.getApplicationId(), equalTo(SOME_TEXT));
    }

    @Test
    public void tokenShouldNotBeSetToOptionsWhenItIsNull() throws Exception {
        when(applicationFactory.createJavaApplication(any(URL.class))).thenReturn(javaApplication);
        when(oauthTokenProvider.getToken("google", USER_ID)).thenReturn(null);

        appEngineClient.update(WORKSPACE_ID, PROJECT_PATH, AppEngineClientTest.class.getResource("/template/java.war"), USER_ID);

        verify(appAdminFactory).createIdeAppAdmin(connectOptionsCaptor.capture(), javaApplicationCaptor.capture());

        ConnectOptions options = connectOptionsCaptor.getValue();

        assertThat(options.getOauthToken(), nullValue());
        assertThat(javaApplicationCaptor.getValue(), equalTo(javaApplication));
    }

    @Test
    public void dummyUpdateListenerShouldBeTested() throws Exception {
        UpdateSuccessEvent updateSuccessEvent = mock(UpdateSuccessEvent.class);
        UpdateProgressEvent updateProgressEvent = mock(UpdateProgressEvent.class);
        UpdateFailureEvent updateFailureEvent = mock(UpdateFailureEvent.class);

        appEngineClient.update(WORKSPACE_ID, PROJECT_PATH, AppEngineClientTest.class.getResource("/template/java.war"), USER_ID);

        verify(admin).update(dummyUpdateListenerCaptor.capture());

        DummyUpdateListener listener = dummyUpdateListenerCaptor.getValue();

        listener.onSuccess(updateSuccessEvent);

        verify(updateSuccessEvent, never()).getDetails();

        listener.onProgress(updateProgressEvent);

        verify(updateProgressEvent, never()).cancel();
        verify(updateProgressEvent, never()).getMessage();
        verify(updateProgressEvent, never()).getPercentageComplete();

        listener.onFailure(updateFailureEvent);

        verify(updateFailureEvent, never()).getDetails();
        verify(updateFailureEvent, never()).getFailureMessage();
    }

}