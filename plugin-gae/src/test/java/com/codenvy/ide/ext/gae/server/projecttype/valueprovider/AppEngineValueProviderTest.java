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
package com.codenvy.ide.ext.gae.server.projecttype.valueprovider;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.vfs.server.VirtualFile;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.codenvy.ide.ext.gae.TestUtil.getContent;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.WEB_INF_FOLDER;
import static java.io.File.separator;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class AppEngineValueProviderTest {
    private static final String PATH_TO_APP_ENGINE_WEB = WEB_INF_FOLDER + separator + "appengine-web.xml";
    private static final String SOME_TEXT              = "someText";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private FolderEntry            project;
    @Mock
    private VirtualFile            projectFolder;
    @Mock
    private VirtualFile            webEngineXml;
    @Mock
    private VirtualFile            yaml;
    @Mock
    private List<String>           value;
    @Mock
    private GAEServerUtil          gaeServerUtil;
    @InjectMocks
    private AppEngineValueProvider valueProvider;

    @Before
    public void setUp() throws Exception {
        when(project.getVirtualFile()).thenReturn(projectFolder);
        when(projectFolder.getChild(PATH_TO_APP_ENGINE_WEB)).thenReturn(webEngineXml);
        when(projectFolder.getChild("app.yaml")).thenReturn(yaml);
        when(webEngineXml.getContent()).thenReturn(getContent(AppEngineValueProviderTest.class, "/template/maven/webengine"));
    }

    @Test
    public void listWithEmptyStringShouldBeReturnedWhenApplicationIdParameterIsMissing() throws Exception {
        when(webEngineXml.getContent()).thenReturn(getContent(AppEngineValueProviderTest.class, "/template/maven/webengineWithoutAppId"));
        when(gaeServerUtil.getApplicationIdFromWebAppEngine(webEngineXml)).thenReturn("");

        List<String> values = valueProvider.getValues(APPLICATION_ID);

        assertThat(values, notNullValue());
        assertThat(values.get(0), equalTo(""));
    }

    @Test(expected = ValueStorageException.class)
    public void valueStorageExceptionShouldBeThrown() throws Exception {
        when(projectFolder.getChild(anyString())).thenThrow(new ForbiddenException(SOME_TEXT));

        valueProvider.getValues(APPLICATION_ID);
    }

    @Test
    public void applicationIdShouldBeReturnedFromWebAppEngineFile() throws Exception {
        when(gaeServerUtil.getApplicationIdFromWebAppEngine(webEngineXml)).thenReturn("your-app-id");
        List<String> values = valueProvider.getValues(APPLICATION_ID);

        assertThat(values, notNullValue());
        assertThat(values.get(0), equalTo("your-app-id"));
    }

    @Test
    public void applicationIdShouldBeReturnedFromYamlFile() throws Exception {
        when(projectFolder.getChild(PATH_TO_APP_ENGINE_WEB)).thenReturn(null);
        when(gaeServerUtil.getApplicationIdFromAppYaml(yaml)).thenReturn("your-app-id");
        List<String> values = valueProvider.getValues(APPLICATION_ID);

        assertThat(values, notNullValue());
        assertThat(values.get(0), equalTo("your-app-id"));
    }

    @Test
    public void applicationIdShouldNotBeSetWhenListIsEmpty() throws Exception {
        reset(project);

        valueProvider.setValues(APPLICATION_ID, Collections.<String>emptyList());

        verify(project, never()).getVirtualFile();
    }

    @Test
    public void applicationIdShouldNotBeSetWhenAttributeListContainsNullValue() throws Exception {
        List<String> attributeList = new ArrayList<>();
        attributeList.add(null);
        reset(project);

        valueProvider.setValues(APPLICATION_ID,attributeList);

        verify(project, never()).getVirtualFile();
    }

    @Test(expected = ValueStorageException.class)
    public void exceptionShouldBeThrownWhenListContainsMoreThenOneElement() throws Exception {
        valueProvider.setValues(APPLICATION_ID, Arrays.asList("", ""));

        verify(project, never()).getVirtualFile();
    }

    @Test
    public void applicationIdShouldNotBeUpdatedWhenWebAppEngineAndYamlAreNull() throws Exception {
        when(projectFolder.getChild(PATH_TO_APP_ENGINE_WEB)).thenReturn(null);
        when(projectFolder.getChild("app.yaml")).thenReturn(null);

        valueProvider.setValues(APPLICATION_ID, Arrays.asList(SOME_TEXT));

        verify(gaeServerUtil, never()).setApplicationIdToWebAppEngine(Matchers.<VirtualFile>anyObject(), anyString());
        verify(gaeServerUtil, never()).setApplicationIdToAppYaml(Matchers.<VirtualFile>anyObject(), anyString());
    }

    @Test
    public void applicationIdShouldBeUpdatedWhenWebAppEngineIsNotNull() throws Exception {
        valueProvider.setValues(APPLICATION_ID, Arrays.asList(SOME_TEXT));

        verify(gaeServerUtil).setApplicationIdToWebAppEngine(webEngineXml, SOME_TEXT);
        verify(gaeServerUtil, never()).setApplicationIdToAppYaml(Matchers.<VirtualFile>anyObject(), anyString());
    }

    @Test
    public void listWithEmptyStringShouldBeReturnedWhenWebEngineAndYamlAreNull() throws Exception {
        when(projectFolder.getChild(PATH_TO_APP_ENGINE_WEB)).thenReturn(null);
        when(projectFolder.getChild("app.yaml")).thenReturn(null);

        List<String> attributes = valueProvider.getValues(APPLICATION_ID);

        verify(gaeServerUtil, never()).getApplicationIdFromWebAppEngine(webEngineXml);
        verify(gaeServerUtil, never()).getApplicationIdFromAppYaml(yaml);

        assertThat(attributes, notNullValue());
        assertThat(attributes.get(0), equalTo(""));
    }

    @Test
    public void applicationIdShouldBeUpdatedWhenWebAppEngineIsNullAndYamlIsNotNull() throws Exception {
        when(projectFolder.getChild(PATH_TO_APP_ENGINE_WEB)).thenReturn(null);

        valueProvider.setValues(APPLICATION_ID, Arrays.asList(SOME_TEXT));

        verify(gaeServerUtil, never()).setApplicationIdToWebAppEngine(Matchers.<VirtualFile>anyObject(), anyString());
        verify(gaeServerUtil).setApplicationIdToAppYaml(yaml, SOME_TEXT);
    }

    @Test(expected = ValueStorageException.class)
    public void applicationIdShouldNotBeUpdatedWhenApiExceptionThrows() throws Exception {
        when(projectFolder.getChild(PATH_TO_APP_ENGINE_WEB)).thenThrow(new ForbiddenException(SOME_TEXT));

        valueProvider.setValues(APPLICATION_ID, Arrays.asList(SOME_TEXT));

        verify(gaeServerUtil, never()).setApplicationIdToWebAppEngine(Matchers.<VirtualFile>anyObject(), anyString());
        verify(gaeServerUtil, never()).setApplicationIdToAppYaml(yaml, SOME_TEXT);
    }

}