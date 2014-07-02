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
package com.codenvy.analytics.services.configuration;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.services.view.DisplayConfiguration;
import com.codenvy.analytics.services.view.RowConfiguration;
import com.codenvy.analytics.services.view.SectionConfiguration;
import com.codenvy.analytics.services.view.ViewConfiguration;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;

import static com.mongodb.util.MyAsserts.assertFalse;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestDisplayConfigurationReader extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<display>" +
                                                "    <view time-unit=\"day,week\" passed-days-count=\"by_1_day,by_lifetime\" columns=\"20\">" +
                                                "        <section name=\"workspaces\">" +
                                                "            <description>desc</description>" +
                                                "            <row class=\"Date.class\">" +
                                                "                <parameter key=\"format\" value=\"dd MMM\"/>" +
                                                "            </row>" +
                                                "            <row class=\"Empty.class\" />" +
                                                "        </section>" +
                                                "    </view>" +
                                                "</display>";

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
        DisplayConfiguration displayConfiguration =
                configurationManager.loadConfiguration(DisplayConfiguration.class, FILE);
        Assert.assertEquals(1, displayConfiguration.getViews().size());

        ViewConfiguration viewConfiguration = displayConfiguration.getViews().get(0);

        assertEquals("day,week", viewConfiguration.getTimeUnit());
        assertEquals("by_1_day,by_lifetime", viewConfiguration.getPassedDaysCount());
        assertEquals(1, viewConfiguration.getSections().size());
        assertEquals(20, viewConfiguration.getColumns());
        assertFalse(viewConfiguration.isOnDemand());

        SectionConfiguration sectionConfiguration = viewConfiguration.getSections().get(0);
        assertEquals("workspaces", sectionConfiguration.getName());
        assertEquals("desc", sectionConfiguration.getDescription());

        assertEquals(2, sectionConfiguration.getRows().size());

        RowConfiguration rowConfiguration = sectionConfiguration.getRows().get(0);
        assertEquals("Date.class", rowConfiguration.getClazz());
        assertEquals(1, rowConfiguration.getParameters().size());
        assertEquals("format", rowConfiguration.getParameters().get(0).getKey());
        assertEquals("dd MMM", rowConfiguration.getParameters().get(0).getValue());

        rowConfiguration = sectionConfiguration.getRows().get(1);
        assertEquals("Empty.class", rowConfiguration.getClazz());
        assertEquals(0, rowConfiguration.getParameters().size());
    }
}
