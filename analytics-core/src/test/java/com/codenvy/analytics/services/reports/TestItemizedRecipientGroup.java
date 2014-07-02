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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestItemizedRecipientGroup extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION =
            "<recipients>" +
            "    <group name=\"group\">" +
            "        <description>the description</description>" +
            "        <initializer>" +
            "            <class>com.codenvy.analytics.services.reports.ItemizedRecipientGroup</class>" +
            "            <parameters>" +
            "                <parameter key=\"e-mail\" value=\"test1@codenvy.com\"/>" +
            "                <parameter key=\"e-mail\" value=\"test2@codenvy.com\"/>" +
            "            </parameters>" +
            "        </initializer>" +
            "    </group>" +
            "</recipients>";

    private RecipientsHolder recipientsHolder;

    @BeforeClass
    public void prepare() throws Exception {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
            out.write(CONFIGURATION);
        }

        XmlConfigurationManager xmlConfigurationManager = mock(XmlConfigurationManager.class);
        when(xmlConfigurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager configurationManager = new XmlConfigurationManager();
                return configurationManager.loadConfiguration(RecipientsHolderConfiguration.class, FILE);
            }
        });

        recipientsHolder = new RecipientsHolder(configurator, xmlConfigurationManager);
    }


    @Test
    public void testInitializationItemizedUsersGroup() throws Exception {
        Set<String> emails = recipientsHolder.getEmails("group", Context.EMPTY);

        assertNotNull(emails);
        assertEquals(2, emails.size());
        assertTrue(emails.contains("test1@codenvy.com"));
        assertTrue(emails.contains("test2@codenvy.com"));
    }
}
