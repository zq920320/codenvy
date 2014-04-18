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
import com.codenvy.analytics.services.view.CSVReportPersister;
import com.codenvy.analytics.services.view.ViewBuilder;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;


/**
 * @author Anatoliy Bazko
 */
public class TestDummyContextModifier extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION =
            "<reports>\n" +
            "    <report>\n" +
            "        <recipients>\n" +
            "            <recipient>analytics</recipient>\n" +
            "        </recipients>\n" +
            "        <frequency>\n" +
            "            <daily>\n" +
            "                <views>\n" +
            "                    <view>summary_report</view>\n" +
            "                </views>\n" +
            "                <context-modifier>\n" +
            "                    <class>com.codenvy.analytics.services.reports.DummyContextModifier</class>\n" +
            "                </context-modifier>\n" +
            "            </daily>\n" +
            "        </frequency>\n" +
            "    </report>\n" +
            "</reports>";

    private ReportSender            reportSender;
    private XmlConfigurationManager xmlConfigurationManager;

    @BeforeClass
    public void prepare() throws Exception {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
            out.write(CONFIGURATION);
        }

        xmlConfigurationManager = mock(XmlConfigurationManager.class);
        when(xmlConfigurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager configurationManager = new XmlConfigurationManager();
                return configurationManager.loadConfiguration(ReportsConfiguration.class, FILE);
            }
        });

        reportSender = new ReportSender(mock(CSVReportPersister.class),
                                        configurator,
                                        mock(RecipientsHolder.class),
                                        mock(ViewBuilder.class),
                                        xmlConfigurationManager);
    }


    @Test
    public void shouldReturnEmptyContext() throws Exception {
        XmlConfigurationManager confManager = new XmlConfigurationManager();
        ReportsConfiguration reportConf = confManager.loadConfiguration(ReportsConfiguration.class, FILE);
        DailyFrequencyConfiguration daily = reportConf.getReports().get(0).getFrequencies().get(0).getDaily();

        ContextModifier contextModifier = reportSender.getContextModifier(daily);
        Context context = contextModifier.update(Context.EMPTY);

        assertEquals(Context.EMPTY, context);
    }
}
