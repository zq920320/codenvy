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
package com.codenvy.ide.ext.gae.server.generators;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.commons.xml.XMLTree;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.TEXT_XML;
import static com.codenvy.ide.ext.gae.TestUtil.getContent;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_JAVA_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.SOURCE_FOLDER;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.TEST_SOURCE_FOLDER;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.WEB_INF_FOLDER;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
* @author Andrey Plotnikov
* @author Valeriy Svydenko
* @author Dmitry Shnurenko
*/
@RunWith(MockitoJUnitRunner.class)
public class JavaGaeProjectGeneratorTest {
    public static final  String POM                     = "pom.xml";
    public static final  String LOGGING                 = "logging.properties";
    public static final  String APP_ENGINE_WEB          = "appengine-web.xml";
    public static final  String WEB                     = "web.xml";
    private static final String NEW_APPLICATION_ID      = "newApplicationId";
    private static final String NEW_ARTIFACT_ID         = "newArtifactId";
    private static final String NEW_GROUP_ID            = "newGroupId";
    private static final String NEW_VERSION             = "newVersion";
    private static final String PATH_TO_TEMPLATE_FOLDER = "/template/java";
    private static final String PATH_TO_POM             = PATH_TO_TEMPLATE_FOLDER + "/pom";
    private static final String PATH_TO_LOGGING         = PATH_TO_TEMPLATE_FOLDER + "/logging";
    private static final String PATH_TO_APP_ENGINE_WEB  = PATH_TO_TEMPLATE_FOLDER + "/appengine-web";
    private static final String PATH_TO_WEB             = PATH_TO_TEMPLATE_FOLDER + "/web";

    @Captor
    private ArgumentCaptor<ByteArrayInputStream> byteArrayInputStreamArgumentCaptor;

    @Mock
    private FolderEntry      baseFolder;
    @Mock
    private GAEServerUtil    gaeUtil;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFileEntry pomFile;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFileEntry webXmlFile;

    @InjectMocks
    private JavaGaeProjectGenerator generator;

    private Map<String, AttributeValue> attributes;

    @Before
    public void setUp() throws Exception {
        attributes = new HashMap<>();

        attributes.put(MavenAttributes.ARTIFACT_ID, new AttributeValue(NEW_ARTIFACT_ID));
        attributes.put(MavenAttributes.GROUP_ID, new AttributeValue(NEW_GROUP_ID));
        attributes.put(MavenAttributes.VERSION, new AttributeValue(NEW_VERSION));
        attributes.put(APPLICATION_ID, new AttributeValue(NEW_APPLICATION_ID));
    }


    @Test
    public void projectTypeIdShouldBeReturned() throws Exception {
        assertThat(generator.getProjectType(), is(GAE_JAVA_ID));
    }

    @Test
    public void artifactIdShouldBeUpdated() throws Exception {
        generateProjectStructure();

        verify(pomFile.getVirtualFile()).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        XMLTree pomTree = XMLTree.from(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(pomTree.getSingleText("project/artifactId"), equalTo(NEW_ARTIFACT_ID));
    }

    @Test
    public void artifactIdNotUpdateWhenAttributeHasNotArtifactId() throws Exception {
        attributes.remove(MavenAttributes.ARTIFACT_ID);
        generateProjectStructure();

        verify(pomFile.getVirtualFile()).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        XMLTree pomTree = XMLTree.from(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(pomTree.getSingleText("project/artifactId"), not(equalTo(NEW_ARTIFACT_ID)));
    }

    @Test
    public void groupIdShouldBeUpdated() throws Exception {
        generateProjectStructure();

        verify(pomFile.getVirtualFile()).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        XMLTree pomTree = XMLTree.from(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(pomTree.getSingleText("project/groupId"), equalTo(NEW_GROUP_ID));
    }

    @Test
    public void groupIdNotUpdateWhenAttributeHasNotArtifactId() throws Exception {
        attributes.remove(MavenAttributes.GROUP_ID);
        generateProjectStructure();

        verify(pomFile.getVirtualFile()).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        XMLTree pomTree = XMLTree.from(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(pomTree.getSingleText("project/groupId"), not(equalTo(NEW_GROUP_ID)));
    }

    @Test
    public void versionShouldBeUpdated() throws Exception {
        generateProjectStructure();

        verify(pomFile.getVirtualFile()).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        XMLTree pomTree = XMLTree.from(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(pomTree.getSingleText("project/version"), equalTo(NEW_VERSION));
    }

    @Test
    public void versionNotUpdateWhenAttributeHasNotArtifactId() throws Exception {
        attributes.remove(MavenAttributes.VERSION);
        generateProjectStructure();

        verify(pomFile.getVirtualFile()).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        XMLTree pomTree = XMLTree.from(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(pomTree.getSingleText("project/version"), not(equalTo(NEW_VERSION)));
    }

    @Test
    public void applicationIdShouldBeUpdated() throws Exception {
        generateProjectStructure();

        verify(gaeUtil).setApplicationIdToWebAppEngine(any(VirtualFile.class), anyString());
    }

    @Test
    public void applicationIdShouldBeNotUpdatedIfAttributeHasNotApplicationIdParameter() throws Exception {
        attributes.remove(APPLICATION_ID);

        generateProjectStructure();

        verify(webXmlFile.getVirtualFile(), never()).updateContent(any(InputStream.class), anyString());
    }

    private void generateProjectStructure() throws Exception {
        FolderEntry webInfFolder = mock(FolderEntry.class);

        when(baseFolder.createFolder(eq(WEB_INF_FOLDER))).thenReturn(webInfFolder);
        when(baseFolder.getChild(eq(POM))).thenReturn(pomFile);

        when(webInfFolder.getChild(eq(APP_ENGINE_WEB))).thenReturn(webXmlFile);
        when(pomFile.getVirtualFile().getContent()).thenReturn(getContent(JavaGaeProjectGeneratorTest.class, PATH_TO_POM));
        when(webXmlFile.getVirtualFile().getContent()).thenReturn(getContent(JavaGaeProjectGeneratorTest.class, PATH_TO_APP_ENGINE_WEB));

        generator.onCreateProject(baseFolder, attributes, Collections.<String, String>emptyMap());

        verify(baseFolder, times(3)).createFolder(anyString());
        verify(baseFolder).createFolder(eq(SOURCE_FOLDER));
        verify(baseFolder).createFolder(eq(TEST_SOURCE_FOLDER));
        verify(baseFolder).createFolder(eq(WEB_INF_FOLDER));

        verify(baseFolder).createFile(eq(POM), eq(toByteArray(getClass().getResourceAsStream(PATH_TO_POM))), eq(TEXT_XML));

        verify(webInfFolder).createFile(eq(WEB), eq(toByteArray(getClass().getResourceAsStream(PATH_TO_WEB))), eq(TEXT_XML));
        verify(webInfFolder).createFile(eq(LOGGING), eq(toByteArray(getClass().getResourceAsStream(PATH_TO_LOGGING))), eq(TEXT_XML));
        verify(webInfFolder).createFile(eq(APP_ENGINE_WEB), eq(toByteArray(getClass().getResourceAsStream(PATH_TO_APP_ENGINE_WEB))),
                                        eq(TEXT_XML));

        verify(pomFile.getVirtualFile()).getContent();
    }

    @Test(expected = ServerException.class)
    public void projectNotGeneratesWhenForbiddenExceptionThrows() throws Exception {
        when(baseFolder.createFile(anyString(), any(byte[].class), anyString())).thenThrow(new ForbiddenException(POM));

        generator.onCreateProject(baseFolder, attributes, Collections.<String, String>emptyMap());
    }

    @Test(expected = ServerException.class)
    public void projectNotGeneratesWhenConflictExceptionThrows() throws Exception {
        when(baseFolder.createFile(anyString(), any(byte[].class), anyString())).thenThrow(new ConflictException(POM));
        generator.onCreateProject(baseFolder, attributes, Collections.<String, String>emptyMap());
    }

    @Test(expected = ServerException.class)
    public void projectNotGeneratesWhenServerExceptionThrows() throws Exception {
        when(baseFolder.createFile(anyString(), any(byte[].class), anyString())).thenThrow(new ServerException(POM));

        generator.onCreateProject(baseFolder, attributes, Collections.<String, String>emptyMap());
    }

}