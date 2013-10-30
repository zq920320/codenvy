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
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.services.XmlConfigurationManager;

import org.mockito.Matchers;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestViewBuilder extends BaseTest {

    private String RESOURCE = "<view>\n" +
                              "    <section name=\"workspaces\" length=\"2\">\n" +
                              "        <row class=\"com.codenvy.analytics.services.view.DateRow\">\n" +
                              "            <parameter key=\"format\" value=\"dd MMM\"/>\n" +
                              "        </row>\n" +
                              "        <row class=\"com.codenvy.analytics.services.view" +
                              ".TestViewBuilder$TestMetricRow\">\n" +
                              "            <parameter key=\"name\" value=\"WORKSPACE_CREATED\"/>\n" +
                              "            <parameter key=\"description\" value=\"Created Workspaces\"/>\n" +
                              "        </row>\n" +
                              "        <row class=\"com.codenvy.analytics.services.view.EmptyRow\"/>\n" +
                              "    </section>\n" +
                              "</view>";

    private ViewConfiguration viewConfiguration;

    @BeforeClass
    public void setUp() throws Exception {
        File view = new File(BASE_DIR, "view.xml");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(view))) {
            out.write(RESOURCE);
        }

        viewConfiguration = new XmlConfigurationManager<>(ViewConfiguration.class)
                .loadConfiguration(view.getAbsolutePath());
    }

    @Test
    public void testParsingConfig() throws Exception {
        ViewBuilder spyBuilder = spy(new ViewBuilder());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                List<List<ValueData>> data = (List<List<ValueData>>)invocation.getArguments()[1];
                assertValueData(data);

                return null;
            }
        }).when(spyBuilder).retain(Matchers.<SectionConfiguration>any(), anyList());

        spyBuilder.build(viewConfiguration);
    }

    private void assertValueData(List<List<ValueData>> data) {
        Calendar today = Calendar.getInstance();

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(StringValueData.DEFAULT, dateRow.get(0));
        assertTrue(dateRow.get(1).getAsString().contains("" + today.get(Calendar.DAY_OF_MONTH)));
        assertTrue(dateRow.get(2).getAsString().contains("" + yesterday.get(Calendar.DAY_OF_MONTH)));

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
