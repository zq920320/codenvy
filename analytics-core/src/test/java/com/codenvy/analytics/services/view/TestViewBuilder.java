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
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.services.XmlConfigurationManager;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestViewBuilder extends BaseTest {

    private String RESOURCE = "<view time-unit=\"day\">\n" +
                              "    <section name=\"workspaces\" columns=\"2\">\n" +
                              "        <row class=\"com.codenvy.analytics.services.view.DateRow\"/>\n" +
                              "        <row class=\"com.codenvy.analytics.services.view" +
                              ".TestViewBuilder$TestMetricRow\">\n" +
                              "            <parameter key=\"name\" value=\"CREATED_WORKSPACES\"/>\n" +
                              "            <parameter key=\"description\" value=\"Created Workspaces\"/>\n" +
                              "        </row>\n" +
                              "        <row class=\"com.codenvy.analytics.services.view.EmptyRow\"/>\n" +
                              "    </section>\n" +
                              "</view>";

    private ViewConfiguration viewConfiguration;

    @BeforeClass
    public void prepare() throws Exception {
        File view = new File(BASE_DIR, "view.xml");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(view))) {
            out.write(RESOURCE);
        }

        viewConfiguration = new XmlConfigurationManager<>(ViewConfiguration.class)
                .loadConfiguration(view.getAbsolutePath());
    }

    @Test
    public void testParsingConfig() throws Exception {
        ViewBuilder viewBuilder = new ViewBuilder();

        ViewBuilder spyBuilder = spy(viewBuilder);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                List<List<ValueData>> data = (List<List<ValueData>>)invocation.getArguments()[1];
                assertValueData(data);

                return null;
            }
        }).when(spyBuilder).retainData(anyString(), anyList());

        spyBuilder.build(viewConfiguration);
        viewBuilder.build(viewConfiguration);
    }

    private void assertValueData(List<List<ValueData>> data) {
        Calendar day1 = Calendar.getInstance();
        day1.add(Calendar.DAY_OF_MONTH, -1);

        Calendar day2 = Calendar.getInstance();
        day2.add(Calendar.DAY_OF_MONTH, -2);

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(new StringValueData("Date"), dateRow.get(0));
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
            return new DoubleValueData(10);
        }
    }
}
