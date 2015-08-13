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

import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.api.vfs.server.VirtualFile;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
* @author Valeriy Svydenko
*/
@RunWith(MockitoJUnitRunner.class)
public class GAEValidateServiceTest {
    private static final String PROJECT_PATH = "pathToProject";
    private static final String PROJECT_ID   = "projectId";
    private static final String WORKSPACE_ID = "wsId";
    private static final String BASE_URI     = "http://localhost";

    @Mock
    private ProjectManager     projectManager;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private Project            project;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFile        webXml;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFile        yamlFile;
    @Mock
    private VirtualFile        projectFolder;


    @InjectMocks
    private GAEValidateService service;

    private ResourceLauncher launcher;

    @Before
    public void setUp() throws Exception {
        DependencySupplierImpl dependencies = new DependencySupplierImpl();

        dependencies.addComponent(ProjectManager.class, projectManager);

        ResourceBinder resources = new ResourceBinderImpl();
        resources.addResource(GAEValidateService.class, null);

        EverrestProcessor processor = new EverrestProcessor(resources, ProviderBinder.getInstance(), dependencies);
        launcher = new ResourceLauncher(processor);

        when(projectManager.getProject(anyString(), eq(PROJECT_PATH))).thenReturn(project);
        when(project.getBaseFolder().getVirtualFile()).thenReturn(projectFolder);
        when(projectFolder.getChild(APP_ENGINE_WEB_XML_PATH)).thenReturn(webXml);
        when(projectFolder.getChild("app.yaml")).thenReturn(yamlFile);
    }

    @Test
    public void projectShouldBeValidIfBothConfigurationFilesExist() throws Exception {
        ContainerResponse response = sendRequest("GET", "validate?projectpath=" + PROJECT_PATH, null);

        assertThat(response.getStatus(), is(204));
    }

    @Test
    public void projectShouldBeValidIfYamlFileExists() throws Exception {
        when(projectFolder.getChild(APP_ENGINE_WEB_XML_PATH)).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "validate?projectpath=" + PROJECT_PATH, null);

        assertThat(response.getStatus(), is(204));
    }

    @Test
    public void projectShouldBeValidIfWebXmlFileExists() throws Exception {
        when(projectFolder.getChild("app.yaml")).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "validate?projectpath=" + PROJECT_PATH, null);

        assertThat(response.getStatus(), is(204));
    }

    @Test
    public void javaProjectIsNotValidIfWebXmlNotExists() throws Exception {
        when(project.getConfig().getTypeId()).thenReturn(GAE_JAVA_ID);
        when(projectFolder.getChild("app.yaml")).thenReturn(null);
        when(projectFolder.getChild(APP_ENGINE_WEB_XML_PATH)).thenReturn(null);
        ProjectType projectType = Mockito.mock(ProjectType.class);
        when(projectType.getId()).thenReturn(GAE_JAVA_ID);

        ContainerResponse response = sendRequest("GET", "validate?projectpath=" + PROJECT_PATH, null);

        assertThat(response.getStatus(), is(500));
        assertThat(response.getEntity().toString(), equalTo(ERROR_WEB_ENGINE_VALIDATE));
    }

    @Test
    public void pythonProjectIsNotValidIfYamlNotExists() throws Exception {
        when(project.getConfig().getTypeId()).thenReturn(GAE_PYTHON_ID);
        when(projectFolder.getChild("app.yaml")).thenReturn(null);
        when(projectFolder.getChild(APP_ENGINE_WEB_XML_PATH)).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "validate?projectpath=" + PROJECT_PATH, null);

        assertThat(response.getStatus(), is(500));
        assertThat(response.getEntity().toString(), equalTo(ERROR_YAML_VALIDATE));
    }


    @Test
    public void phpProjectIsNotValidIfYamlNotExists() throws Exception {
        when(project.getConfig().getTypeId()).thenReturn(GAE_PHP_ID);
        when(projectFolder.getChild("app.yaml")).thenReturn(null);
        when(projectFolder.getChild(APP_ENGINE_WEB_XML_PATH)).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "validate?projectpath=" + PROJECT_PATH, null);

        assertThat(response.getStatus(), is(500));
        assertThat(response.getEntity().toString(), equalTo(ERROR_YAML_VALIDATE));
    }

    private ContainerResponse sendRequest(String method, String path, byte[] data) throws Exception {
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put("Content-Type", Arrays.asList(MediaType.APPLICATION_JSON));

        return launcher.service(method, "/gae-validator/" + WORKSPACE_ID + '/' + path, BASE_URI, headers, data, null);
    }

}