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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestEventHolderConfiguration extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<events>\n" +
                                                "    <event name=\"ide_usage\">\n" +
                                                "        <description>desc</description>\n" +
                                                "        <parameters>\n" +
                                                "            <param>WS</param>\n" +
                                                "            <param allow-empty-value=\"true\">USER</param>\n" +
                                                "        </parameters>\n" +
                                                "    </event>\n" +
                                                "</events>";

    private XmlConfigurationManager configurationManager;

    @BeforeClass
    public void prepare() throws Exception {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
            out.write(CONFIGURATION);
        }

        configurationManager = new XmlConfigurationManager();
    }


    @Test
    public void testParsingConfig() throws Exception {
        EventHolderConfiguration configuration =
                configurationManager.loadConfiguration(EventHolderConfiguration.class, FILE);

        assertNotNull(configuration);
        assertEquals(1, configuration.getEvents().size());

        EventConfiguration eventConfiguration = configuration.getEvents().get(0);
        assertEquals("ide_usage", eventConfiguration.getName());
        assertEquals("desc", eventConfiguration.getDescription());

        ParametersConfiguration parameters = eventConfiguration.getParameters();
        assertEquals(2, parameters.getParams().size());
        assertEquals("WS", parameters.getParams().get(0).getName());
        assertEquals(false, parameters.getParams().get(0).isAllowEmptyValue());
        assertEquals(true, parameters.getParams().get(1).isAllowEmptyValue());
    }
}
