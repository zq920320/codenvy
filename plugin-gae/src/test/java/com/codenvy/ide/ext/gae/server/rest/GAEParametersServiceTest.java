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

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.server.VirtualFile;
import com.codenvy.ide.ext.gae.shared.GAEMavenInfo;
import com.codenvy.ide.ext.gae.shared.YamlParameterInfo;

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
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.ext.gae.TestUtil.getContent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class GAEParametersServiceTest {

    private static final String ARTIFACT_ID     = "artifactId";
    private static final String GROUP_ID        = "groupId";
    private static final String VERSION         = "1.0.0-SNAPSHOT";
    private static final String APPLICATION_ID  = "your-app-id";
    private static final String PARENT_GROUP_ID = "parentGroupId";
    private static final String PARENT_VERSION  = "parentVersion";

    private static final String PROJECT_PATH           = "pathToProject";
    private static final String PATH_TO_POM            = "/template/maven/";
    private static final String PATH_TO_WEB_ENGINE_XML = "/template/maven/webengine";

    private static final String WORKSPACE_ID = "wsId";
    private static final String BASE_URI     = "http://localhost";
    private static final String SOME_TEXT    = "someText";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private FolderEntry root;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFileEntry  projectEntry;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private Project     project;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFile pomFile;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFile yamlFile;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFile webEngineXml;

    @Mock
    private VirtualFile    projectFolder;
    @Mock
    private ProjectManager projectManager;

    private ResourceLauncher launcher;

    @InjectMocks
    private GAEParametersService service;

    @Before
    public void setUp() throws Exception {
        DependencySupplierImpl dependencies = new DependencySupplierImpl();

        dependencies.addComponent(ProjectManager.class, projectManager);

        ResourceBinder resources = new ResourceBinderImpl();
        resources.addResource(GAEParametersService.class, null);

        EverrestProcessor processor = new EverrestProcessor(resources, ProviderBinder.getInstance(), dependencies);

        launcher = new ResourceLauncher(processor);

        when(projectManager.getProject(anyString(), eq(PROJECT_PATH))).thenReturn(project);
        when(projectManager.getProjectsRoot(anyString())).thenReturn(root);
        when(root.getChild(PROJECT_PATH)).thenReturn(projectEntry);
        when(projectEntry.getVirtualFile()).thenReturn(projectFolder);
        when(project.getBaseFolder().getVirtualFile()).thenReturn(projectFolder);
        when(projectFolder.getChild("pom.xml")).thenReturn(pomFile);
        when(projectFolder.getChild("app.yaml")).thenReturn(yamlFile);
        when(projectFolder.getChild("src/main/webapp/WEB-INF/appengine-web.xml")).thenReturn(webEngineXml);
        when(webEngineXml.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, PATH_TO_WEB_ENGINE_XML));
        when(projectFolder.getPath()).thenReturn(SOME_TEXT);
    }


    private ContainerResponse sendRequest(String method, String path, byte[] data) throws Exception {
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put("Content-Type", Arrays.asList(MediaType.APPLICATION_JSON));

        return launcher.service(method, "/gae-parameters/" + WORKSPACE_ID + '/' + path, BASE_URI, headers, data, null);
    }

    @Test
    public void mavenParametersShouldNotBeReadWhenPomFileNotExist() throws Exception {
        when(projectFolder.getChild("pom.xml")).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "read/maven?projectpath=" + PROJECT_PATH, null);
        String mavenInfo = (String)response.getEntity();

        assertThat(response.getStatus(), is(500));
        assertThat(mavenInfo, equalTo("There is no pom.xml file in project: " + PROJECT_PATH));

        verify(projectManager).getProjectsRoot(WORKSPACE_ID);
        verify(root).getChild(PROJECT_PATH);
        verify(projectFolder).getChild("pom.xml");
    }

    @Test
    public void parametersShouldNotBeReadWhenAppEngineWebXmlFileNotExist() throws Exception {
        when(pomFile.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, PATH_TO_POM + "pomWithAllParameters"));
        when(projectFolder.getChild("src/main/webapp/WEB-INF/appengine-web.xml")).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "read/maven?projectpath=" + PROJECT_PATH, null);

        String mavenInfo = (String)response.getEntity();

        assertThat(response.getStatus(), is(500));
        assertThat(mavenInfo, equalTo("There is no appengine-web.xml file in project: " + SOME_TEXT));
    }

    @Test
    public void artifactIdShouldNotBeSetWhenItIsMissingInPom() throws Exception {
        when(pomFile.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, PATH_TO_POM + "pomWithoutArtifactId"));

        assertMavenGaeParameters(getGaeMavenInfoFromResponse(), null, GROUP_ID, VERSION, APPLICATION_ID);
    }

    private GAEMavenInfo getGaeMavenInfoFromResponse() throws Exception {
        ContainerResponse response = sendRequest("GET", "read/maven?projectpath=" + PROJECT_PATH, null);
        assertThat(response.getStatus(), is(200));

        return (GAEMavenInfo)response.getEntity();
    }

    private void assertMavenGaeParameters(GAEMavenInfo gaeMavenInfo,
                                          String artifactId,
                                          String groupId,
                                          String version,
                                          String applicationId) {

        assertThat(gaeMavenInfo.getArtifactId(), equalTo(artifactId));
        assertThat(gaeMavenInfo.getGroupId(), equalTo(groupId));
        assertThat(gaeMavenInfo.getVersion(), equalTo(version));
        assertThat(gaeMavenInfo.getApplicationId(), equalTo(applicationId));
    }

    @Test
    public void groupIdShouldNotBeSetWhenItIsMissingInPom() throws Exception {
        when(pomFile.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, PATH_TO_POM + "pomWithoutGroupId"));

        assertMavenGaeParameters(getGaeMavenInfoFromResponse(), ARTIFACT_ID, null, VERSION, APPLICATION_ID);
    }

    @Test
    public void versionShouldNotBeSetWhenItIsMissingInPom() throws Exception {
        when(pomFile.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, PATH_TO_POM + "pomWithoutVersion"));

        assertMavenGaeParameters(getGaeMavenInfoFromResponse(), ARTIFACT_ID, GROUP_ID, null, APPLICATION_ID);
    }


    @Test
    public void applicationIdShouldNotBeSetWhenItIsMissingInAppEngineXml() throws Exception {
        when(pomFile.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, PATH_TO_POM + "pomWithAllParameters"));
        when(webEngineXml.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, "/template/maven/webengineWithoutAppId"));

        assertMavenGaeParameters(getGaeMavenInfoFromResponse(), ARTIFACT_ID, GROUP_ID, VERSION, "");
    }

    @Test
    public void parentParametersShouldBeSetWhenParentExist() throws Exception {
        when(pomFile.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, PATH_TO_POM + "pomWithParent"));

        assertMavenGaeParameters(getGaeMavenInfoFromResponse(), ARTIFACT_ID, PARENT_GROUP_ID, PARENT_VERSION, APPLICATION_ID);
    }

    @Test
    public void applicationIdShouldNotBeReadWhenYamlFileNotExist() throws Exception {
        when(projectFolder.getChild("app.yaml")).thenReturn(null);

        ContainerResponse response = sendRequest("GET", "read/yaml?projectpath=" + PROJECT_PATH, null);
        String yamlInfo = (String)response.getEntity();

        assertThat(response.getStatus(), is(500));
        assertThat(yamlInfo, equalTo("There is no app.yaml file in project: " + PROJECT_PATH));

        verify(projectManager).getProjectsRoot(WORKSPACE_ID);
        verify(root).getChild(PROJECT_PATH);
        verify(projectFolder).getChild("app.yaml");
    }

    @Test
    public void applicationIdShouldNotBeSetWhenItIsMissingInAppYaml() throws Exception {
        when(yamlFile.getContent()).thenReturn(getContent(GAEParametersServiceTest.class, "/template/yaml/appWithoutApplicationId"));

        assertGaeYamlParameters(getGaeYamlInfoFromResponse(), null);
    }

    private void assertGaeYamlParameters(YamlParameterInfo yamlParameterInfo, String applicationId) {
        assertThat(yamlParameterInfo.getApplicationId(), equalTo(applicationId));
    }

    private YamlParameterInfo getGaeYamlInfoFromResponse() throws Exception {
        ContainerResponse response = sendRequest("GET", "read/yaml?projectpath=" + PROJECT_PATH, null);
        assertThat(response.getStatus(), is(200));

        return (YamlParameterInfo)response.getEntity();
    }

}