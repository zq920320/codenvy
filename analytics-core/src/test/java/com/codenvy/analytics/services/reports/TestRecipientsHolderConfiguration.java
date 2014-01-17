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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestRecipientsHolderConfiguration extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<recipients>\n" +
                                                "    <group name=\"group1\">\n" +
                                                "        <description>the description 1</description>\n" +
                                                "        <initializer>\n" +
                                                "            <class>com.codenvy.analytics.services.reports" +
                                                ".ItemizedRecipientGroup</class>\n" +
                                                "            <parameters>\n" +
                                                "                <parameter key=\"e-mail\" value=\"test1@codenvy" +
                                                ".com\"/>\n" +
                                                "                <parameter key=\"e-mail\" value=\"test2@codenvy" +
                                                ".com\"/>\n" +
                                                "            </parameters>\n" +
                                                "        </initializer>\n" +
                                                "    </group>\n" +
                                                "    <group name=\"group2\">\n" +
                                                "        <description>the description 2</description>\n" +
                                                "        <initializer>\n" +
                                                "            <class>com.codenvy.analytics.services.reports" +
                                                ".FileStoredRecipientGroup</class>\n" +
                                                "            <parameters>\n" +
                                                "                <parameter key=\"file\" value=\"file1\"/>\n" +
                                                "                <parameter key=\"file\" value=\"file2\"/>\n" +
                                                "            </parameters>\n" +
                                                "        </initializer>\n" +
                                                "    </group>\n" +
                                                "</recipients>\n";

    private XmlConfigurationManager configurationManager;

    @BeforeClass
    public void prepare() throws Exception {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
            out.write(CONFIGURATION);
        }

        configurationManager = new XmlConfigurationManager();
    }

    @Test
    public void testParsingConfiguration() throws Exception {
        RecipientsHolderConfiguration configuration =
                configurationManager.loadConfiguration(RecipientsHolderConfiguration.class, FILE);

        assertNotNull(configuration.getGroups());
        assertEquals(configuration.getGroups().size(), 2);

        GroupConfiguration groupConfiguration = configuration.getGroups().get(0);
        assertEquals(groupConfiguration.getDescription(), "the description 1");

        InitializerConfiguration initializer = groupConfiguration.getInitializer();
        assertEquals(initializer.getClazz(), "com.codenvy.analytics.services.reports.ItemizedRecipientGroup");
        assertNotNull(initializer.getParametersConfiguration());
        assertEquals(initializer.getParametersConfiguration().getParameters().size(), 2);

        List<ParameterConfiguration> parameters = initializer.getParametersConfiguration().getParameters();
        assertEquals(parameters.get(0).getKey(), "e-mail");
        assertEquals(parameters.get(0).getValue(), "test1@codenvy.com");
        assertEquals(parameters.get(1).getKey(), "e-mail");
        assertEquals(parameters.get(1).getValue(), "test2@codenvy.com");

        groupConfiguration = configuration.getGroups().get(1);
        assertEquals(groupConfiguration.getDescription(), "the description 2");

        initializer = groupConfiguration.getInitializer();
        assertEquals(initializer.getClazz(), "com.codenvy.analytics.services.reports.FileStoredRecipientGroup");
        assertNotNull(initializer.getParametersConfiguration());
        assertEquals(initializer.getParametersConfiguration().getParameters().size(), 2);

        parameters = initializer.getParametersConfiguration().getParameters();
        assertEquals(parameters.get(0).getKey(), "file");
        assertEquals(parameters.get(0).getValue(), "file1");
        assertEquals(parameters.get(1).getKey(), "file");
        assertEquals(parameters.get(1).getValue(), "file2");
    }
}
