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

import com.google.appengine.tools.admin.UpdateListener;
import com.google.apphosting.utils.config.AppEngineConfigException;
import com.google.apphosting.utils.config.IndexesXml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import static com.codenvy.ide.ext.gae.server.applications.yaml.YamlApplication.readAppYaml;
import static com.codenvy.ide.ext.gae.server.applications.yaml.YamlApplication.readIndexYaml;
import static com.google.appengine.tools.admin.GenericApplication.ErrorHandler;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class YamlApplicationTest {
    private static final String PATH_TO_TEMPLATE_FOLDER = "/template/yaml";
    private static final String SOURCE_LANGUAGE         = "DUMMY";

    public static final String APPLICATION_ID     = "codenvy2012";
    public static final String VERSION            = "1";
    public static final String MIME_TYPE          = "text/html";
    public static final String DEFAULT_ERROR_FILE = "default_error.html";
    public static final String OVER_QUOTA_FILE    = "over_quota.html";

    @Mock
    private File applicationDirectory;

    private YamlApplication yamlApplication;

    @Before
    public void setUp() throws Exception {
        when(applicationDirectory.getAbsolutePath()).thenReturn(getClass().getResource(PATH_TO_TEMPLATE_FOLDER).getPath());

        yamlApplication = new DummyYamlApplication(applicationDirectory, SOURCE_LANGUAGE);
    }

    @Test
    public void appIdShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getAppId(), equalTo(APPLICATION_ID));
    }

    @Test
    public void versionShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getVersion(), equalTo(VERSION));
    }

    @Test
    public void setSourceLanguageShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getSourceLanguage(), equalTo(SOURCE_LANGUAGE));
    }

    @Test
    public void moduleShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getModule(), nullValue());
    }

    @Test
    public void precompilationEnableValueShouldBeFalse() throws Exception {
        assertThat(yamlApplication.isPrecompilationEnabled(), is(false));
    }

    @Test
    public void errorHandlersShouldBeReturned() throws Exception {
        List<ErrorHandler> handlers = yamlApplication.getErrorHandlers();

        assertThat(handlers.size(), is(2));

        ErrorHandler handler1 = handlers.get(0);
        ErrorHandler handler2 = handlers.get(1);

        assertThat(handler1.getFile(), equalTo(DEFAULT_ERROR_FILE));
        assertThat(handler1.getErrorCode(), equalTo("default"));
        assertThat(handler1.getMimeType(), equalTo(MIME_TYPE));

        assertThat(handler2.getFile(), equalTo(OVER_QUOTA_FILE));
        assertThat(handler2.getErrorCode(), equalTo("over_quota"));
        assertThat(handler2.getMimeType(), equalTo(MIME_TYPE));
    }

    @Test
    public void mimeTypeShouldBeNull() throws Exception {
        assertThat(yamlApplication.getMimeTypeIfStatic(""), nullValue());
    }

    @Test
    public void mimeTypeShouldBeNotNull() throws Exception {
        assertThat(yamlApplication.getMimeTypeIfStatic("favicon.ico"), equalTo("image/x-icon"));
    }

    @Test
    public void cronXmlShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getCronXml(), nullValue());
    }

    @Test
    public void queueXmlShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getQueueXml(), nullValue());
    }

    @Test
    public void dispatchXmlShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getDispatchXml(), nullValue());
    }

    @Test
    public void dosXmlShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getDosXml(), nullValue());
    }

    @Test
    public void pagespeedYamlShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getPagespeedYaml(), nullValue());
    }

    @Test
    public void indexesXmlShouldBeReturned() throws Exception {
        IndexesXml indexesXml = yamlApplication.getIndexesXml();

        assertThat(indexesXml, notNullValue());
        assertThat(indexesXml.size(), is(3));
    }

    @Test
    public void indexXmlShouldBeNullIfFileNotExists() throws Exception {
        assertThat(readIndexYaml("path"), nullValue());
    }

    @Test
    public void indexXmlShouldBeNullIfFileIsEmpty() throws Exception {
        assertThat(readIndexYaml(getClass().getResource(PATH_TO_TEMPLATE_FOLDER + "/emptyIndex.yaml").getPath()), nullValue());
    }

    @Test(expected = AppEngineConfigException.class)
    public void appEngineConfigExceptionShouldBeThrowsIfIndexXmlIsNotValid() throws Exception {
        readIndexYaml(getClass().getResource(PATH_TO_TEMPLATE_FOLDER + "/notValidIndex.yaml").getPath());
    }

    @Test
    public void appYamlShouldBeReadIfErrorHandlersNotExist() throws Exception {
        YamlAppInfo appInfo = readAppYaml(getClass().getResource(PATH_TO_TEMPLATE_FOLDER + "/appWithoutErrorHandlers.yaml").getPath());

        assertThat(appInfo, notNullValue());
        assertThat(appInfo.application, equalTo("codenvy2012"));
    }

    @Test
    public void backendsXmlShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getBackendsXml(), nullValue());
    }

    @Test
    public void apiVersionShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getApiVersion(), equalTo(VERSION));
    }

    @Test
    public void pathShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getPath(), equalTo(getClass().getResource(PATH_TO_TEMPLATE_FOLDER).getPath()));
    }

    @Test
    public void stagingDirShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getStagingDir(), nullValue());
    }

    @Test
    public void stagingDirectoryShouldBeCleaned() throws Exception {
        yamlApplication.cleanStagingDirectory();

        when(applicationDirectory.isDirectory()).thenReturn(false);
        when(applicationDirectory.delete()).thenReturn(true);

        //noinspection ResultOfMethodCallIgnored
        verify(applicationDirectory).isDirectory();
        //noinspection ResultOfMethodCallIgnored
        verify(applicationDirectory).delete();
    }

    @Test
    public void listenerShouldBeSet() throws Exception {
        UpdateListener listener = mock(UpdateListener.class);

        yamlApplication.setListener(listener);

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void detailsWriterShouldBeSet() throws Exception {
        PrintWriter writer = mock(PrintWriter.class);

        yamlApplication.setDetailsWriter(writer);

        verifyNoMoreInteractions(writer);
    }

    @Test
    public void appYamlShouldBeReturned() throws Exception {
        String yaml = yamlApplication.getAppYaml();

        assertThat(yaml, notNullValue());
        assertThat(yaml, containsString("application: codenvy2012"));
    }

    @Test
    public void instanceClassShouldBeReturned() throws Exception {
        assertThat(yamlApplication.getInstanceClass(), nullValue());
    }

    private class DummyYamlApplication extends YamlApplication {
        public DummyYamlApplication(@NotNull File applicationDirectory, @NotNull String sourceLanguage) {
            super(applicationDirectory, sourceLanguage);
        }
    }

}