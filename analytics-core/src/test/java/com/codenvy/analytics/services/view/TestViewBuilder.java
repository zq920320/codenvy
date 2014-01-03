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
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

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

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestViewBuilder extends BaseTest {

    private static final String FILE          = BASE_DIR + "/resource";
    private static final String CONFIGURATION = "<display>\n" +
                                                "     <view time-unit=\"day,week,month,lifetime\" name=\"view\" columns=\"3\">\n" +
                                                "         <section name=\"workspaces\">\n" +
                                                "             <row class=\"com.codenvy.analytics.services.view" +
                                                ".DateRow\">\n" +
                                                "                 <parameter key=\"section-name\" value=\"desc\"/>\n" +
                                                "             </row>\n" +
                                                "             <row class=\"com.codenvy.analytics.services.view" +
                                                ".TestViewBuilder$TestMetricRow\">\n" +
                                                "                 <parameter key=\"name\" " +
                                                "value=\"CREATED_WORKSPACES\"/>\n" +
                                                "                 <parameter key=\"description\" value=\"Created " +
                                                "Workspaces\"/>\n" +
                                                "             </row>\n" +
                                                "             <row class=\"com.codenvy.analytics.services.view" +
                                                ".EmptyRow\"/>\n" +
                                                "         </section>\n" +
                                                "     </view>" +
                                                "</display>";

    private XmlConfigurationManager<DisplayConfiguration> configurationManager;

    @BeforeClass
    public void prepare() throws Exception {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(FILE))) {
            out.write(CONFIGURATION);
        }

        configurationManager = spy(new XmlConfigurationManager<>(DisplayConfiguration.class, FILE));
    }

    @Test
    public void testIfShippedConfigurationCorrect() throws Exception {
        ViewBuilder viewBuilder = new ViewBuilder();
        viewBuilder.doExecute(Utils.initializeContext(Parameters.TimeUnit.DAY));
    }

    @Test
    public void testLastDayPeriod() throws Exception {
        ViewBuilder spyBuilder = spy(new ViewBuilder());

        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> viewData = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> context = ArgumentCaptor.forClass(Map.class);

        DisplayConfiguration displayConfiguration = configurationManager.loadConfiguration();

        spyBuilder.computeDisplayData(displayConfiguration, Utils.initializeContext(Parameters.TimeUnit.DAY));
        verify(spyBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        Map<String, List<List<ValueData>>> actualData = viewData.getAllValues().get(0);
        assertEquals(actualData.size(), 1);
        assertLastDayData(actualData.values().iterator().next());

        Calendar calendar = Utils.getToDate(Utils.initializeContext(Parameters.TimeUnit.DAY));

        File csvReport = new File("./target/reports/" + dirFormat.format(calendar.getTime()) + "/view_day.csv");
        assertTrue(csvReport.exists());

        csvReport = new File("./target/reports/" + dirFormat.format(calendar.getTime()) + "/view_week.csv");
        assertTrue(csvReport.exists());

        csvReport = new File("./target/reports/" + dirFormat.format(calendar.getTime()) + "/view_month.csv");
        assertTrue(csvReport.exists());

        csvReport = new File("./target/reports/" + dirFormat.format(calendar.getTime()) + "/view_lifetime.csv");
        assertTrue(csvReport.exists());

        CSVReportPersister.restoreBackup();
    }

    @Test
    public void testSpecificDayPeriod() throws Exception {
        ViewBuilder spyBuilder = spy(new ViewBuilder());

        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> viewData = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> context = ArgumentCaptor.forClass(Map.class);

        Map<String, String> executionContext = Utils.newContext();
        Parameters.TO_DATE.put(executionContext, "20130930");
        Parameters.FROM_DATE.put(executionContext, "20130930");
        DisplayConfiguration displayConfiguration = configurationManager.loadConfiguration();

        spyBuilder.computeDisplayData(displayConfiguration, executionContext);
        verify(spyBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        Map<String, List<List<ValueData>>> actualData = viewData.getAllValues().get(0);
        assertEquals(actualData.size(), 1);
        assertSpecificDayData(actualData.values().iterator().next());

        File csvReport = new File("./target/reports/2013/09/30/view_day.csv");
        assertTrue(csvReport.exists());

        new File("./target/reports/2013/09/30/view_week.csv");
        assertTrue(csvReport.exists());

        new File("./target/reports/2013/09/30/view_month.csv");
        assertTrue(csvReport.exists());

        new File("./target/reports/2013/09/30/view_lifetime.csv");
        assertTrue(csvReport.exists());
    }

    @Test
    public void testQueryViewData() throws Exception {
        DisplayConfiguration displayConfiguration = configurationManager.loadConfiguration();
        Map<String, String> context = Utils.initializeContext(Parameters.TimeUnit.DAY);
        ViewBuilder viewBuilder = new ViewBuilder();
        viewBuilder.computeDisplayData(displayConfiguration, context);

        Map<String, List<List<ValueData>>> actualData =
                viewBuilder.queryViewData(displayConfiguration.getView("view"), context);

        assertEquals(actualData.size(), 1);
        assertLastDayData(actualData.values().iterator().next());
    }

    private void assertSpecificDayData(List<List<ValueData>> data) {
        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertTrue(dateRow.get(1).getAsString().contains("30"));
        assertTrue(dateRow.get(2).getAsString().contains("29"));

        List<ValueData> metricRow = data.get(1);
        assertEquals(3, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));
        assertEquals(new StringValueData("5"), metricRow.get(2));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(3, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(2));
    }

    private void assertLastDayData(List<List<ValueData>> data) {
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
        assertEquals(new StringValueData("5"), metricRow.get(2));

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
            if (Parameters.TO_DATE.get(context).equals(Parameters.TO_DATE.getDefaultValue())) {
                return new StringValueData("10");
            } else {
                return new StringValueData("5");
            }
        }
    }

}
