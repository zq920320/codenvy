/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.im.config;

import com.codenvy.im.utils.HttpTransport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class TestConfig {

    private ConfigUtil    configUtil;
    private HttpTransport transport;

    @BeforeMethod
    public void setUp() throws Exception {
        transport = mock(HttpTransport.class);
        configUtil = spy(new ConfigUtil("", transport));
    }

    @Test
    public void testConfigProperties() throws Exception {
        Path conf = Paths.get("target", "conf.properties");

        FileUtils.write(conf.toFile(), "user=1\npwd=2\n");

        Map<String, String> m = configUtil.loadConfigProperties(conf);
        assertEquals(m.size(), 2);
        assertEquals(m.get("user"), "1");
        assertEquals(m.get("pwd"), "2");
    }

    @Test
    public void testIsEmpty() throws Exception {
        assertTrue(Config.isEmpty(null));
        assertTrue(Config.isEmpty("MANDATORY"));

        assertFalse(Config.isEmpty(""));
        assertFalse(Config.isEmpty("value"));
    }

    @Test
    public void testIsValidDefaultConfig() throws Exception {
        Config config = new Config(Collections.<String, String>emptyMap());
        assertTrue(config.isValid());
    }

    @Test
    public void testIsValidCSSConfig() throws Exception {
        Map<String, String> properties = ImmutableMap.of("some property", "some value");

        Config config = new Config(properties);
        assertTrue(config.isValid());
    }

    @Test
    public void testIsValidCSSConfigError() throws Exception {
        Map<String, String> properties = ImmutableMap.of("some property", "MANDATORY");

        Config config = new Config(properties);
        assertFalse(config.isValid());
    }

    @Test(expectedExceptions = FileNotFoundException.class, expectedExceptionsMessageRegExp = "Configuration file 'non-existed' not found")
    public void testLoadNonExistedConfigFile() throws IOException {
        configUtil.loadConfigProperties("non-existed");
    }

    @Test
    public void testLoadDefaultCdecConfig() throws Exception {
        Path properties = Paths.get("target/test.properties");
        FileUtils.write(properties.toFile(), "a=1\n" +
                                             "b=2\n");
        doReturn(properties).when(transport).download(endsWith("codenvy-single-server-properties/3.1.0"), any(Path.class));

        Map<String, String> m = configUtil.loadCdecDefaultProperties("3.1.0");
        assertEquals(m.size(), 2);
        assertEquals(m.get("a"), "1");
        assertEquals(m.get("b"), "2");
    }

    @Test
    public void testMerge() throws Exception {
        Map<String, String> properties1 = ImmutableMap.of("a", "1", "b", "2", "version", "1");
        Map<String, String> properties2 = ImmutableMap.of("a", "2", "c", "3", "version", "2");
        Map<String, String> m = configUtil.merge(properties1, properties2);

        assertEquals(m.size(), 4);
        assertEquals(m.get("a"), "1");
        assertEquals(m.get("b"), "2");
        assertEquals(m.get("c"), "3");
        assertEquals(m.get("version"), "2");
    }

    @Test
    public void testLoadInstalledCssProperties() throws Exception {
        Path properties = Paths.get("target/test.properties");
        FileUtils.write(properties.toFile(), "#\n" +
                                             "# Please finalize configurations by entering required values below:\n" +
                                             "#\n" +
                                             "# replace test.com placeholder with dns name of your single server installation.\n" +
                                             "# Note: DNS name that you configure must be later used as DNS name of the server where a single " +
                                             "server Codenvy Enterprise will be\n" +
                                             "# installed\n" +
                                             "node \"test.com\" inherits \"base_config\" {\n" +
                                             "  # enter dns name of your single server installation, same as above.\n" +
                                             "  $aio_host_url = \"test.com\"\n" +
                                             "\n" +
                                             "  ###############################\n" +
                                             "  # Codenvy Builder configurations\n" +
                                             "  #\n" +
                                             "  # (Mandatory) builder_max_execution_time -  max execution time in seconds for build process.\n" +
                                             "  # If process doesn't end before this time it may be terminated forcibly.\n" +
                                             "  $builder_max_execution_time = \"600\"\n" +
                                             "\n");

        doReturn(ImmutableList.of(properties).iterator()).when(configUtil).getCssPropertiesFiles();
        Map<String, String> m = configUtil.loadInstalledCssProperties();
        assertEquals(m.size(), 2);
        assertEquals(m.get("aio_host_url"), "test.com");
        assertEquals(m.get("builder_max_execution_time"), "600");
    }

    @Test
    public void testGetHostUrl() throws Exception {
        Config config = new Config(ImmutableMap.of(Config.HOST_URL, "host"));
        assertEquals(config.getHostUrl(), "host");

        config = new Config(ImmutableMap.of(Config.AIO_HOST_URL, "host"));
        assertEquals(config.getHostUrl(), "host");

        config = new Config(ImmutableMap.of(Config.HOST_URL, "host1", Config.AIO_HOST_URL, "host2"));
        assertEquals(config.getHostUrl(), "host1");
    }
}
