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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestXmlReaderConfiguration {

    private static final String RESOURCE = "<scripts>" +
                                           "    <script name=\"test1.pig\">" +
                                           "        <parameters>" +
                                           "            <entry>" +
                                           "                <key>USER</key>" +
                                           "                <value>ANY</value>" +
                                           "            </entry>" +
//                                           "            <parameter name=\"USER\" value=\"ANY\"/>" +
//                                           "            <parameter name=\"WS\" value=\"ANY\"/>" +
                                           "        </parameters>" +
                                           "    </script>" +
//                                           "    <script name=\"test2.pig\">" +
//                                           "        <parameters>" +
//                                           "            <parameter name=\"USER\" value=\"PERSISTENT\"/>" +
//                                           "            <parameter name=\"WS\" value=\"REGISTERED\"/>" +
//                                           "            <parameter name=\"EVENT\" value=\"event_value\"/>" +
//                                           "            <parameter name=\"FIELD\" value=\"filed_value\"/>" +
//                                           "        </parameters>" +
//                                           "    </script>" +
                                           "</scripts>";

    @Test
    public void testParsingConfig() throws Exception {
        XmlConfigurationManager confReader = new XmlConfigurationManager();
        XmlConfigurationManager spyService = spy(confReader);

        doReturn(new ByteArrayInputStream(RESOURCE.getBytes("UTF-8"))).when(spyService).openResource();

        PigRunnerConfiguration configuration = spyService.loadConfiguration();


//        assertNotNull(conf);
//        assertEquals(2, conf.getScripts().size());
//
//        ExecutionEntry executionEntry = conf.getScripts().get(0);
//        assertNotNull(executionEntry);
//        assertEquals("0/10 * * * * ?", executionEntry.getSchedule());
//        assertEquals(2, executionEntry.getScripts().size());
//
//        ScriptConfiguration scriptEntry = executionEntry.getScripts().get(0);
//        assertNotNull(scriptEntry);
//        assertEquals("action.pig", scriptEntry.getName());
//        assertEquals(5, scriptEntry.getParameters().size());
//        assertEquals("LOG", scriptEntry.getParameters().get(0).getName());
//        assertEquals("...", scriptEntry.getParameters().get(0).getValue());
//        assertEquals("FROM_DATE", scriptEntry.getParameters().get(1).getName());
//        assertEquals("...", scriptEntry.getParameters().get(1).getValue());
//        assertEquals("TO_DATE", scriptEntry.getParameters().get(2).getName());
//        assertEquals("...", scriptEntry.getParameters().get(2).getValue());
//        assertEquals("USER", scriptEntry.getParameters().get(3).getName());
//        assertEquals("*", scriptEntry.getParameters().get(3).getValue());
//        assertEquals("WS", scriptEntry.getParameters().get(4).getName());
//        assertEquals("*", scriptEntry.getParameters().get(4).getValue());
//
//
//        scriptEntry = executionEntry.getScripts().get(1);
//        assertNotNull(scriptEntry);
//        assertEquals("set_active_by_users.pig", scriptEntry.getName());
//        assertEquals(7, scriptEntry.getParameters().size());
//        assertEquals("LOG", scriptEntry.getParameters().get(0).getName());
//        assertEquals("...", scriptEntry.getParameters().get(0).getValue());
//        assertEquals("FROM_DATE", scriptEntry.getParameters().get(1).getName());
//        assertEquals("...", scriptEntry.getParameters().get(1).getValue());
//        assertEquals("TO_DATE", scriptEntry.getParameters().get(2).getName());
//        assertEquals("...", scriptEntry.getParameters().get(2).getValue());
//        assertEquals("USER", scriptEntry.getParameters().get(3).getName());
//        assertEquals("*", scriptEntry.getParameters().get(3).getValue());
//        assertEquals("WS", scriptEntry.getParameters().get(4).getName());
//        assertEquals("*", scriptEntry.getParameters().get(4).getValue());
//        assertEquals("EVENT", scriptEntry.getParameters().get(5).getName());
//        assertEquals("build", scriptEntry.getParameters().get(5).getValue());
//        assertEquals("FIELD", scriptEntry.getParameters().get(6).getName());
//        assertEquals("field_value", scriptEntry.getParameters().get(6).getValue());
//
//        executionEntry = conf.getScripts().get(1);
//        assertNotNull(executionEntry);
//        assertEquals("0/5 * * * * ?", executionEntry.getSchedule());
//        assertEquals(1, executionEntry.getScripts().size());
//
//        scriptEntry = executionEntry.getScripts().get(0);
//        assertNotNull(scriptEntry);
//        assertEquals("product_usage_time_domains.pig", scriptEntry.getName());
//        assertEquals(5, scriptEntry.getParameters().size());
//        assertEquals("LOG", scriptEntry.getParameters().get(0).getName());
//        assertEquals("...", scriptEntry.getParameters().get(0).getValue());
//        assertEquals("FROM_DATE", scriptEntry.getParameters().get(1).getName());
//        assertEquals("...", scriptEntry.getParameters().get(1).getValue());
//        assertEquals("TO_DATE", scriptEntry.getParameters().get(2).getName());
//        assertEquals("...", scriptEntry.getParameters().get(2).getValue());
//        assertEquals("USER", scriptEntry.getParameters().get(3).getName());
//        assertEquals("*", scriptEntry.getParameters().get(3).getValue());
//        assertEquals("WS", scriptEntry.getParameters().get(4).getName());
//        assertEquals("*", scriptEntry.getParameters().get(4).getValue());
    }
}
