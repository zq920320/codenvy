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
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertNull;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestReportsConfiguration extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<reports>\n" +
                                                "    <report>\n" +
                                                "        <recipients>\n" +
                                                "            <recipient>group1</recipient>\n" +
                                                "        </recipients>\n" +
                                                "        <frequency>\n" +
                                                "            <daily/>\n" +
                                                "            <weekly>\n" +
                                                "                <views>\n" +
                                                "                    <view>summary_report</view>\n" +
                                                "                    <view>factory-timeline</view>\n" +
                                                "                </views>\n" +
                                                "                <context-modifier>\n" +
                                                "                    <class>clazz</class>\n" +
                                                "                </context-modifier>\n" +
                                                "            </weekly>\n" +
                                                "        </frequency>\n" +
                                                "    </report>\n" +
                                                "</reports>\n";

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
        ReportsConfiguration configuration = configurationManager.loadConfiguration(ReportsConfiguration.class, FILE);

        assertNotNull(configuration.getReports());
        assertEquals(1, configuration.getReports().size());

        ReportConfiguration report = configuration.getReports().get(0);
        assertNotNull(report.getFrequencies());
        assertNotNull(report.getRecipients());

        RecipientsConfiguration recipients = report.getRecipients();
        assertEquals(1, recipients.getRecipients().size());
        assertEquals("group1", recipients.getRecipients().get(0));

        List<FrequencyConfiguration> frequenciesConfiguration = report.getFrequencies();
        assertEquals(1, frequenciesConfiguration.size());

        FrequencyConfiguration frequency = frequenciesConfiguration.get(0);
        assertNull(frequency.getMonthly());
        assertNotNull(frequency.getDaily());
        assertNotNull(frequency.getWeekly());

        DailyFrequencyConfiguration dailyFrequencyConfiguration = frequency.getDaily();
        assertNull(dailyFrequencyConfiguration.getViews());

        WeeklyFrequencyConfiguration weeklyFrequencyConfiguration = frequency.getWeekly();
        assertNotNull(weeklyFrequencyConfiguration.getViews());

        ViewsConfiguration viewsConfiguration = weeklyFrequencyConfiguration.getViews();
        assertEquals(2, viewsConfiguration.getViews().size());
        assertEquals("summary_report", viewsConfiguration.getViews().get(0));
        assertEquals("factory-timeline", viewsConfiguration.getViews().get(1));

        assertNotNull(weeklyFrequencyConfiguration.getContextModifier());
        assertEquals("clazz", weeklyFrequencyConfiguration.getContextModifier().getClazz());
    }
}
