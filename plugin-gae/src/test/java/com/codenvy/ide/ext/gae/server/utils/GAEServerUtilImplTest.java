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
package com.codenvy.ide.ext.gae.server.utils;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.commons.xml.XMLTree;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static com.codenvy.ide.ext.gae.TestUtil.getContent;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Valeriy Svydenko */
@RunWith(MockitoJUnitRunner.class)
public class GAEServerUtilImplTest {
    public static final  String SOME_TEXT                             = "some-text";
    private static final String APPLICATION_ID                        = "your-app-id";
    private static final String APPLICATION_ID_XPATH                  = "appengine-web-app/application";
    private static final String NEW_APPLICATION_ID                    = "new-app-id";
    private static final String PATH_TO_WEB_ENGINE_XML                = "/template/maven/webengine";
    private static final String PATH_TO_WEB_ENGINE_XML_WITHOUT_APP_ID = "/template/maven/webengineWithoutAppId";
    private static final String PATH_TO_APP_YAML                      = "/template/yaml/appYaml";
    private static final String PATH_TO_APP_YAML_WITHOUT_APP_ID       = "/template/yaml/appWithoutApplicationId";

    @Captor
    private ArgumentCaptor<ByteArrayInputStream> byteArrayInputStreamArgumentCaptor;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFile applicationWebApp;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private VirtualFile yaml;

    @InjectMocks
    private GAEServerUtilImpl gaeUtil;

    @Before
    public void setUp() throws Exception {
        when(applicationWebApp.getContent()).thenReturn(getContent(GAEServerUtilImplTest.class, PATH_TO_WEB_ENGINE_XML));
        when(yaml.getContent()).thenReturn(getContent(GAEServerUtilImplTest.class, PATH_TO_APP_YAML));
    }

    @Test
    public void appIdShouldBeSetIntoWebAppEngineFile() throws Exception {
        XMLTree webXmlTree = XMLTree.from(getContent(GAEServerUtilImplTest.class, PATH_TO_WEB_ENGINE_XML).getStream());
        assertThat(webXmlTree.getSingleText(APPLICATION_ID_XPATH), equalTo(APPLICATION_ID));

        gaeUtil.setApplicationIdToWebAppEngine(applicationWebApp, NEW_APPLICATION_ID);

        verify(applicationWebApp).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        webXmlTree = XMLTree.from(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(webXmlTree.getSingleText(APPLICATION_ID_XPATH), equalTo(NEW_APPLICATION_ID));
    }

    @Test(expected = ServerException.class)
    public void appIdShouldNotBeSetWhenIOExceptionThrows() throws Exception {
        InputStream inputStream = getContent(GAEServerUtilImplTest.class, PATH_TO_WEB_ENGINE_XML).getStream();
        inputStream.close();

        when(applicationWebApp.getContent()).thenReturn(new ContentStream(SOME_TEXT, inputStream, TEXT_PLAIN));

        gaeUtil.setApplicationIdToWebAppEngine(applicationWebApp, NEW_APPLICATION_ID);

        verify(applicationWebApp, never()).updateContent(Matchers.<ByteArrayInputStream>anyObject(), isNull(String.class));
    }

    @Test
    public void appIdShouldBeReturnedFromWebAppEngineFile() throws Exception {
        assertThat(gaeUtil.getApplicationIdFromWebAppEngine(applicationWebApp), equalTo(APPLICATION_ID));
    }

    @Test
    public void appIdShouldBeEmptyIfAppIdDoesNotExistIntoWebAppEngine() throws Exception {
        when(applicationWebApp.getContent()).thenReturn(getContent(GAEServerUtilImplTest.class, PATH_TO_WEB_ENGINE_XML_WITHOUT_APP_ID));

        assertThat(gaeUtil.getApplicationIdFromWebAppEngine(applicationWebApp), equalTo(""));
    }

    @Test(expected = ServerException.class)
    public void appIdShouldNotBeGetWhenIOExceptionThrows() throws Exception {
        InputStream inputStream = getContent(GAEServerUtilImplTest.class, PATH_TO_WEB_ENGINE_XML).getStream();
        inputStream.close();

        when(applicationWebApp.getContent()).thenReturn(new ContentStream(SOME_TEXT, inputStream, TEXT_PLAIN));

        gaeUtil.getApplicationIdFromWebAppEngine(applicationWebApp);

        verify(applicationWebApp, never()).updateContent(Matchers.<ByteArrayInputStream>anyObject(), isNull(String.class));
    }

    @Test
    public void appIdShouldBeSetIntoAppYaml() throws Exception {
        gaeUtil.setApplicationIdToAppYaml(yaml, NEW_APPLICATION_ID);

        verify(yaml).updateContent(byteArrayInputStreamArgumentCaptor.capture(), isNull(String.class));

        String content = IOUtils.toString(byteArrayInputStreamArgumentCaptor.getValue());
        assertThat(content, containsString("application: " + NEW_APPLICATION_ID));
    }

    @Test(expected = ServerException.class)
    public void appIdShouldBeSetFromAppYamlIfIOExceptionThrows() throws Exception {
        InputStream inputStream = getContent(GAEServerUtilImplTest.class, PATH_TO_WEB_ENGINE_XML).getStream();
        inputStream.close();

        when(yaml.getContent()).thenReturn(new ContentStream(SOME_TEXT, inputStream, TEXT_PLAIN));

        gaeUtil.setApplicationIdToAppYaml(yaml, NEW_APPLICATION_ID);

        verify(yaml, never()).updateContent(Matchers.<ByteArrayInputStream>anyObject(), isNull(String.class));
    }

    @Test
    public void appIdShouldBeReturnedFromAppYaml() throws Exception {
        assertThat(gaeUtil.getApplicationIdFromAppYaml(yaml), equalTo(APPLICATION_ID));
    }

    @Test
    public void appIdShouldBeEmptyIfAppIdDoesNotExistIntoAppYaml() throws Exception {
        when(yaml.getContent()).thenReturn(getContent(GAEServerUtilImplTest.class, PATH_TO_APP_YAML_WITHOUT_APP_ID));

        assertThat(gaeUtil.getApplicationIdFromAppYaml(yaml), equalTo(""));
    }

}