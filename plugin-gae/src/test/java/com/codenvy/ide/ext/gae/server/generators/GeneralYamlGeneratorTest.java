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
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneralYamlGeneratorTest {
    private static final String DUMMY_ID                = "dummy-id";
    private static final String SOME_TEXT               = "some-text";
    private static final String YAML_NAME               = "app.yaml";
    private static final String NEW_APPLICATION_ID      = "newApplicationId";
    private static final String PATH_TO_TEMPLATE_FOLDER = "/template/yaml/appYaml";

    @Mock
    private FolderEntry baseFolder;

    @Mock
    protected GAEServerUtil    gaeUtil;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private   VirtualFileEntry yaml;

    private GeneralYamlProjectGenerator generalGenerator;

    private Map<String, AttributeValue> attributes;

    @Before
    public void setUp() throws Exception {
        generalGenerator = new DummyGenerator(DUMMY_ID, gaeUtil);
        attributes = new HashMap<>();

        attributes.put(APPLICATION_ID, new AttributeValue(NEW_APPLICATION_ID));

        when(baseFolder.getChild(anyString())).thenReturn(yaml);
    }

    @Test
    public void projectTypeIdShouldBeReturned() throws Exception {
        assertThat(generalGenerator.getProjectType(), equalTo(DUMMY_ID));
    }

    @Test
    public void applicationIdShouldBeApplied() throws Exception {
        generalGenerator.applyYamlApplicationId(baseFolder, attributes);

        verify(baseFolder).getChild(YAML_NAME);
        verify(baseFolder.getChild(anyString())).getVirtualFile();
        verify(gaeUtil).setApplicationIdToAppYaml(yaml.getVirtualFile(), NEW_APPLICATION_ID);
    }

    @Test
    public void applicationIdShouldNotBeUpdatedIfItIsNull() throws Exception {
        attributes.put(SOME_TEXT, new AttributeValue(SOME_TEXT));
        attributes.remove(APPLICATION_ID);

        generalGenerator.applyYamlApplicationId(baseFolder, attributes);

        verify(baseFolder, never()).getChild(anyString());
        verify(baseFolder.getChild(anyString()), never()).getVirtualFile();
        verify(gaeUtil, never()).setApplicationIdToAppYaml(Matchers.<VirtualFile>anyObject(), anyString());
    }

    @Test
    public void fileShouldBeCreated() throws Exception {
        generalGenerator.createFile(baseFolder, YAML_NAME, PATH_TO_TEMPLATE_FOLDER, SOME_TEXT);

        verify(baseFolder).createFile(eq(YAML_NAME), Matchers.<byte[]>anyObject(), eq(SOME_TEXT));
    }

    @Test(expected = ServerException.class)
    public void fileShouldNotBeCreatedIfForbiddenExceptionThrows() throws Exception {
        when(baseFolder.createFile(anyString(), Matchers.<byte[]>anyObject(), anyString())).thenThrow(new ForbiddenException(SOME_TEXT));

        generalGenerator.createFile(baseFolder, YAML_NAME, PATH_TO_TEMPLATE_FOLDER, SOME_TEXT);
    }

    @Test(expected = ServerException.class)
    public void fileShouldNotBeCreatedIfConflictExceptionThrows() throws Exception {
        when(baseFolder.createFile(anyString(), Matchers.<byte[]>anyObject(), anyString())).thenThrow(new ConflictException(SOME_TEXT));

        generalGenerator.createFile(baseFolder, YAML_NAME, PATH_TO_TEMPLATE_FOLDER, SOME_TEXT);
    }

    @Test(expected = ServerException.class)
    public void fileShouldNotBeCreatedIfServerExceptionThrows() throws Exception {
        when(baseFolder.createFile(anyString(), Matchers.<byte[]>anyObject(), anyString())).thenThrow(new ServerException(SOME_TEXT));

        generalGenerator.createFile(baseFolder, YAML_NAME, PATH_TO_TEMPLATE_FOLDER, SOME_TEXT);
    }

    private class DummyGenerator extends GeneralYamlProjectGenerator {

        public DummyGenerator(@NotNull String projectId,
                              @NotNull GAEServerUtil gaeUtil) {
            super(projectId, gaeUtil);
        }

        @Override
        public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
                throws ForbiddenException, ConflictException, ServerException {

        }
    }

}