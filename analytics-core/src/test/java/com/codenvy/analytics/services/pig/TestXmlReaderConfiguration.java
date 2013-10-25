/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.services.pig;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestXmlReaderConfiguration {

    private static final String RESOURCE = "<scripts>" +
                                           "    <script name=\"test1\">" +
                                           "        <description>desc</description>" +
                                           "        <parameters>" +
                                           "            <entry>" +
                                           "                <key>USER</key>" +
                                           "                <value>REGISTERED</value>" +
                                           "            </entry>" +
                                           "            <entry>" +
                                           "                <key>WS</key>" +
                                           "                <value>PERSISTENT</value>" +
                                           "            </entry>" +
                                           "        </parameters>" +
                                           "    </script>" +
                                           "</scripts>";

    @Test
    public void testParsingConfig() throws Exception {
        XmlConfigurationManager confReader = new XmlConfigurationManager();
        XmlConfigurationManager spyService = spy(confReader);

        doReturn(new ByteArrayInputStream(RESOURCE.getBytes("UTF-8"))).when(spyService).openResource();

        PigRunnerConfiguration configuration = spyService.loadConfiguration();

        assertNotNull(configuration);
        assertEquals(1, configuration.getScripts().size());

        ScriptConfiguration scriptConfiguration = configuration.getScripts().get(0);
        assertEquals("test1", scriptConfiguration.getName());
        assertEquals("desc", scriptConfiguration.getDescription());

        Map<String, String> parameters = scriptConfiguration.getParameters();
        assertEquals(2, parameters.size());
        assertEquals("REGISTERED", parameters.get("USER"));
        assertEquals("PERSISTENT", parameters.get("WS"));
    }
}
