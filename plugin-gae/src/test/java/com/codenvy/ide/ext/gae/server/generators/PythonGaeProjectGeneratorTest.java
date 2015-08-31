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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.TEXT_X_PYTHON;
import static org.eclipse.che.ide.MimeType.TEXT_YAML;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PYTHON_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class PythonGaeProjectGeneratorTest {
    private static final String CSS_FOLDER         = "css";
    private static final String JS_FOLDER          = "js";
    private static final String TEMPLATES_FOLDER   = "templates";
    private static final String SCRIPT             = "main.py";
    private static final String SOME_TEXT          = "some-text";
    private static final String YAML_NAME          = "app.yaml";
    private static final String NEW_APPLICATION_ID = "newApplicationId";

    @Mock
    private GAEServerUtil gaeUtil;
    @Mock
    private FolderEntry   baseFolder;

    private PythonGaeProjectGenerator generalGenerator;

    @Before
    public void setUp() throws Exception {
        generalGenerator = new PythonGaeProjectGenerator(gaeUtil);
    }

    @Test
    public void projectTypeIdShouldBeReturned() throws Exception {
        assertThat(generalGenerator.getProjectType(), equalTo(GAE_PYTHON_ID));
    }

    @Test
    /**
     *   Here tested only creation project structure
     *   Adding applicationId to the app.yaml tested in GeneralYamlGeneratorTest
     */
    public void projectShouldBeGenerated() throws Exception {
        Map<String, AttributeValue> attributes = new HashMap<>();
        generalGenerator.onCreateProject(baseFolder, attributes, Collections.<String, String>emptyMap());

        verify(baseFolder).createFolder(eq(CSS_FOLDER));
        verify(baseFolder).createFolder(eq(JS_FOLDER));
        verify(baseFolder).createFolder(eq(TEMPLATES_FOLDER));
        verify(baseFolder).createFile(eq(YAML_NAME), Matchers.<byte[]>anyObject(), eq(TEXT_YAML));
        verify(baseFolder).createFile(eq(SCRIPT), Matchers.<byte[]>anyObject(), eq(TEXT_X_PYTHON));
    }

    @Test(expected = ApiException.class)
    public void projectShouldNotBeGeneratedIfApiExceptionThrows() throws Exception {
        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(APPLICATION_ID, new AttributeValue(NEW_APPLICATION_ID));//Add applicationId attribute for initiate ServerException

        when(baseFolder.getChild(anyString())).thenThrow(new ServerException(SOME_TEXT));

        generalGenerator.onCreateProject(baseFolder, attributes, Collections.<String, String>emptyMap());
    }

}