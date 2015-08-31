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
package com.codenvy.ide.ext.gae.server.applications.yaml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class PHPApplicationTest {
    private static final String PATH_TO_TEMPLATE_FOLDER = "/template/yaml";

    @Mock
    private File applicationDirectory;

    private PHPApplication phpApplication;

    @Before
    public void setUp() throws Exception {
        when(applicationDirectory.getAbsolutePath()).thenReturn(PHPApplicationTest.class.getResource(PATH_TO_TEMPLATE_FOLDER).getPath());

        phpApplication = new PHPApplication(applicationDirectory);
    }

    @Test
    public void sourceLanguageShouldBeReturned() throws Exception {
        assertThat(phpApplication.getSourceLanguage(), equalTo("PHP"));
    }

}