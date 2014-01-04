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
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;

import static org.testng.Assert.*;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestItemizedUsersGroup extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<recipients>\n" +
                                                "    <group name=\"group\">\n" +
                                                "        <description>the description</description>\n" +
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
                                                "</recipients>";

    private XmlConfigurationManager<RecipientsHolderConfiguration> configurationManager;

    @BeforeClass
    public void prepare() throws Exception {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
            out.write(CONFIGURATION);
        }

        configurationManager = new XmlConfigurationManager<>(RecipientsHolderConfiguration.class, FILE);
    }


    @Test
    public void testInitializationItemizedUsersGroup() throws Exception {
        RecipientsHolder recipientsHolder = new RecipientsHolder(configurationManager);

        Set<String> emails = recipientsHolder.getEmails("group");
        assertNotNull(emails);
        assertEquals(2, emails.size());
        assertTrue(emails.contains("test1@codenvy.com"));
        assertTrue(emails.contains("test2@codenvy.com"));
    }
}
