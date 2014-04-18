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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestFileStoredUsersGroup extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<recipients>\n" +
                                                "    <group name=\"group\">\n" +
                                                "        <description>the description</description>\n" +
                                                "        <initializer>\n" +
                                                "            <class>com.codenvy.analytics.services.reports" +
                                                ".FileStoredRecipientGroup</class>\n" +
                                                "            <parameters>\n" +
                                                "                <parameter key=\"file\" value=\"target/file1\"/>\n" +
                                                "                <parameter key=\"file\" value=\"target/file2\"/>\n" +
                                                "            </parameters>\n" +
                                                "        </initializer>\n" +
                                                "    </group>\n" +
                                                "</recipients>";

    private RecipientsHolder recipientsHolder;

    @BeforeClass
    public void prepare() throws Exception {
        XmlConfigurationManager configurationManager = mock(XmlConfigurationManager.class);
        when(configurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
                    out.write(CONFIGURATION);
                }

                try (BufferedWriter out = new BufferedWriter(new FileWriter("target/file1"))) {
                    out.write("test1@gmail.com");
                }

                try (BufferedWriter out = new BufferedWriter(new FileWriter("target/file2"))) {
                    out.write("test2@gmail.com");
                }

                XmlConfigurationManager manager = new XmlConfigurationManager();
                return manager.loadConfiguration(RecipientsHolderConfiguration.class, FILE);
            }
        });

        recipientsHolder = new RecipientsHolder(configurator, configurationManager);
    }

    @Test
    public void testInitializationItemizedUsersGroup() throws Exception {
        Set<String> emails = recipientsHolder.getEmails("group", Context.EMPTY);

        assertNotNull(emails);
        assertEquals(2, emails.size());
        assertTrue(emails.contains("test1@gmail.com"));
        assertTrue(emails.contains("test2@gmail.com"));
    }
}
