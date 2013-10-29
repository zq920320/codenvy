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
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.services.XmlConfigurationManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;


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
        ViewBuilder viewBuilder = new ViewBuilder();
        viewBuilder.build(viewConfiguration);
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
