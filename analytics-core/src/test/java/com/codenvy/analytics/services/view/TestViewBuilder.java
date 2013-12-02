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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.XmlConfigurationManager;
import com.codenvy.analytics.storage.CSVDataPersister;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestViewBuilder extends BaseTest {

    private String RESOURCE = "<display>\n" +
                              "     <view time-unit=\"day\">\n" +
                              "         <section name=\"workspaces\" columns=\"3\">\n" +
                              "             <row class=\"com.codenvy.analytics.services.view.DateRow\">\n" +
                              "                 <parameter key=\"section-name\" value=\"desc\"/>\n" +
                              "             </row>\n" +
                              "             <row class=\"com.codenvy.analytics.services.view" +
                              ".TestViewBuilder$TestMetricRow\">\n" +
                              "                 <parameter key=\"name\" value=\"CREATED_WORKSPACES\"/>\n" +
                              "                 <parameter key=\"description\" value=\"Created Workspaces\"/>\n" +
                              "             </row>\n" +
                              "             <row class=\"com.codenvy.analytics.services.view.EmptyRow\"/>\n" +
                              "         </section>\n" +
                              "     </view>" +
                              "</display>";

    private DisplayConfiguration displayConfiguration;

    @BeforeClass
    public void prepare() throws Exception {
        File view = new File(BASE_DIR, "view.xml");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(view))) {
            out.write(RESOURCE);
        }

        displayConfiguration = new XmlConfigurationManager<>(DisplayConfiguration.class)
                .loadConfiguration(view.getAbsolutePath());
    }

    @Test
    public void shouldReturnCorrectData() throws Exception {
        ViewBuilder spyBuilder = spy(new ViewBuilder());

        ArgumentCaptor<String> tblName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> data = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map> context = ArgumentCaptor.forClass(Map.class);

        spyBuilder.build(displayConfiguration);
        verify(spyBuilder).retainData(tblName.capture(), data.capture(), context.capture());

        assertValueData(data.getValue());

        Calendar calendar = Utils.getToDate(Utils.initializeContext(Parameters.TimeUnit.DAY));

        File csvReport = new File("./target/reports/" + dirFormat.format(calendar.getTime()) + "/workspaces_day.csv");
        assertTrue(csvReport.exists());

        File csvBackupReport =
                new File("./target/backup/reports/" + dirFormat.format(calendar.getTime()) + "/workspaces_day.csv");
        assertTrue(csvBackupReport.exists());

        CSVDataPersister.restoreBackup();
    }

    private void assertValueData(List<List<ValueData>> data) {
        Calendar day1 = Calendar.getInstance();
        day1.add(Calendar.DAY_OF_MONTH, -1);

        Calendar day2 = Calendar.getInstance();
        day2.add(Calendar.DAY_OF_MONTH, -2);

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertTrue(dateRow.get(1).getAsString().contains("" + day1.get(Calendar.DAY_OF_MONTH)));
        assertTrue(dateRow.get(2).getAsString().contains("" + day2.get(Calendar.DAY_OF_MONTH)));

        List<ValueData> metricRow = data.get(1);
        assertEquals(3, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("10"), metricRow.get(1));
        assertEquals(new StringValueData("10"), metricRow.get(2));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(3, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(2));
    }

    public static class TestMetricRow extends MetricRow {

        public TestMetricRow(Map<String, String> parameters) {
            super(parameters);
        }

        @Override
        protected ValueData getMetricValue(Map<String, String> context) throws IOException {
            return new StringValueData("10");
        }
    }

}
