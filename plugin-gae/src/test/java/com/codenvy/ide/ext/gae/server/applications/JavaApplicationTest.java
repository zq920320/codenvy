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
package com.codenvy.ide.ext.gae.server.applications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static com.google.appengine.tools.admin.GenericApplication.ErrorHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaApplicationTest {

    private static final String PATH_TO_RESOURCE = "/template/java.war";

    private JavaApplication javaApplication;

    @Before
    public void setUp() throws Exception {
        javaApplication = new JavaApplication(JavaApplicationTest.class.getResource(PATH_TO_RESOURCE));
    }

    @Test
    public void applicationIdShouldBeReturned() {
        assertThat(javaApplication.getAppId(), equalTo("sd-solver-java"));
    }

    @Test
    public void versionShouldBeReturned() throws Exception {
        assertThat(javaApplication.getVersion(), equalTo("1"));
    }

    @Test
    public void sourceLanguageShouldBeReturned() throws Exception {
        assertThat(javaApplication.getSourceLanguage(), equalTo("Java"));
    }

    @Test
    public void moduleShouldBeReturned() throws Exception {
        assertThat(javaApplication.getModule(), equalTo("default"));
    }

    @Test
    public void preCompilationStateShouldBeReturned() throws Exception {
        assertThat(javaApplication.isPrecompilationEnabled(), is(true));
    }

    @Test
    public void errorHandlersShouldBeReturned() throws Exception {
        List<ErrorHandler> errorHandlers = javaApplication.getErrorHandlers();

        assertThat(errorHandlers.isEmpty(), is(true));
    }

    @Test
    public void appengineWebXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getAppEngineWebXml(), notNullValue());
    }

    @Test
    public void cronXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getCronXml(), nullValue());
    }

    @Test
    public void queueXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getQueueXml(), nullValue());
    }

    @Test
    public void dispatchXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getDispatchXml(), nullValue());
    }

    @Test
    public void dosXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getDosXml(), nullValue());
    }

    @Test
    public void pageSpeedYamlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getPagespeedYaml(), nullValue());
    }

    @Test
    public void indexesXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getIndexesXml(), notNullValue());
    }

    @Test
    public void webXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getWebXml(), notNullValue());
    }

    @Test
    public void backendsXmlShouldBeReturned() throws Exception {
        assertThat(javaApplication.getBackendsXml(), nullValue());
    }

    @Test
    public void pathShouldBeReturned() throws Exception {
        String applicationPath = javaApplication.getPath();
        String tmpPath = System.getProperty("java.io.tmpdir");
        if (!tmpPath.endsWith(File.separator)) {
            tmpPath = tmpPath.concat(File.separator);
        }

        assertThat(applicationPath.startsWith(tmpPath + "ide-appengine"), is(true));
        assertThat(applicationPath.endsWith(".tmp_dir"), is(true));
    }

    @Test
    public void staginDirShouldBeReturned() throws Exception {
        assertThat(javaApplication.getStagingDir(), nullValue());
    }

    @Test
    public void instanceClassShouldBeReturned() throws Exception {
        assertThat(javaApplication.getInstanceClass(), nullValue());
    }

}

