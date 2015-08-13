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

import com.google.apphosting.utils.config.AppEngineConfigException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.codenvy.ide.ext.gae.TestUtil.getContent;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class YamlAppInfoTest {
    private static final String PATH_TO_YAML               = "/template/yaml/app.yaml";
    private static final String PATH_TO_SIMPLE_YAML        = "/template/yaml/appYaml";
    private static final String PATH_TO_APP_YAML_TO_STRING = "/template/yaml/appYamlToString";
    private static final String PATH_TO_DEFAULT_SKIP_FILES = "/template/yaml/defaultSkipFiles";

    private YamlAppInfo yamlAppInfo;

    @Test
    public void fileShouldBeParsed() throws Exception {
        try (InputStream in = getContent(YamlAppInfoTest.class, PATH_TO_YAML).getStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in))) {

            yamlAppInfo = YamlAppInfo.parse(r);
        }

        assertThat(yamlAppInfo.application, equalTo("codenvy2012"));
        assertThat(yamlAppInfo.version, equalTo("1"));
        assertThat(yamlAppInfo.runtime, equalTo("python27"));
        assertThat(yamlAppInfo.api_version, equalTo("1"));

        assertThat(yamlAppInfo.builtins.size(), is(2));
        Map<String, String> builtin1 = yamlAppInfo.builtins.get(0);
        Map<String, String> builtin2 = yamlAppInfo.builtins.get(1);
        assertThat(builtin1.containsKey("deferred"), is(true));
        assertThat(builtin1.containsValue("on"), is(true));
        assertThat(builtin2.containsKey("appstats"), is(true));
        assertThat(builtin2.containsValue("on"), is(true));

        assertThat(yamlAppInfo.includes, hasItem("cloud_endpoints.yaml"));
        assertThat(yamlAppInfo.includes, hasItem("web_interface.yaml"));
        assertThat(yamlAppInfo.includes, hasItem("admin_interface.yaml"));

        assertThat(yamlAppInfo.handlers.size(), is(3));
        Map<String, String> handler1 = yamlAppInfo.handlers.get(0);
        Map<String, String> handler2 = yamlAppInfo.handlers.get(1);
        Map<String, String> handler3 = yamlAppInfo.handlers.get(2);

        assertThat(handler1.containsKey("static_files"), is(true));
        assertThat(handler1.containsKey("upload"), is(true));
        assertThat(handler1.containsKey("url"), is(true));
        assertThat(handler1.containsValue("favicon.ico"), is(true));
        assertThat(handler1.containsValue("favicon\\.ico"), is(true));
        assertThat(handler1.containsValue("/favicon\\.ico"), is(true));

        assertThat(handler2.containsKey("static_dir"), is(true));
        assertThat(handler2.containsKey("url"), is(true));
        assertThat(handler2.containsValue("bootstrap"), is(true));
        assertThat(handler2.containsValue("/bootstrap"), is(true));

        assertThat(handler3.containsKey("script"), is(true));
        assertThat(handler3.containsKey("url"), is(true));
        assertThat(handler3.containsValue("guestbook.application"), is(true));
        assertThat(handler3.containsValue("/.*"), is(true));

        assertThat(yamlAppInfo.libraries.size(), is(2));
        Map<String, String> lib1 = yamlAppInfo.libraries.get(0);
        Map<String, String> lib2 = yamlAppInfo.libraries.get(1);

        assertThat(lib1.containsKey("name"), is(true));
        assertThat(lib1.containsKey("version"), is(true));
        assertThat(lib1.containsValue("PIL"), is(true));
        assertThat(lib1.containsValue("1.1.7"), is(true));

        assertThat(lib2.containsKey("name"), is(true));
        assertThat(lib2.containsKey("version"), is(true));
        assertThat(lib2.containsValue("webob"), is(true));
        assertThat(lib2.containsValue("1.1.1"), is(true));

        assertThat(yamlAppInfo.inbound_services, hasItem("mail"));
        assertThat(yamlAppInfo.inbound_services, hasItem("warmup"));

        assertThat(yamlAppInfo.default_expiration, equalTo("4d 5h"));
    }

    @Test(expected = AppEngineConfigException.class)
    public void parserShouldBeThrowExceptionIfReaderHasSomeProblems() throws Exception {
        try (InputStream in = getContent(YamlAppInfoTest.class, PATH_TO_YAML).getStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in))) {

            r.close();
            yamlAppInfo = YamlAppInfo.parse(r);
        }
    }

    @Test
    public void parametersShouldBeConvertedToString() throws Exception {
        try (InputStream in = getContent(YamlAppInfoTest.class, PATH_TO_YAML).getStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in))) {

            yamlAppInfo = YamlAppInfo.parse(r);
        }

        String yaml = yamlAppInfo.toYaml();
        String expectedResult = IOUtils.toString(getContent(YamlAppInfoTest.class, PATH_TO_APP_YAML_TO_STRING).getStream());

        assertThat(yaml, notNullValue());
        assertThat(yaml, equalTo(expectedResult));
    }

    @Test
    public void defaultSkipFilesShouldBeAdded() throws Exception {
        try (InputStream in = getContent(YamlAppInfoTest.class, PATH_TO_SIMPLE_YAML).getStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in))) {

            yamlAppInfo = YamlAppInfo.parse(r);
        }

        String defaultSkipFiles = IOUtils.toString(getContent(YamlAppInfoTest.class, PATH_TO_DEFAULT_SKIP_FILES).getStream());

        assertThat(yamlAppInfo.toYaml(), containsString(defaultSkipFiles));
    }

}