/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestDateFieldsMetricRow extends BaseTest {

    private Context context;
    private Map<String, String> parameters;

    @BeforeClass
    public void prepare() throws Exception {
        context = Utils.initializeContext(Parameters.TimeUnit.DAY);

        parameters = new HashMap<>();
        parameters.put("date-fields", "date1,date2=HH:mm:ss");
        parameters.put("fields", "date1,date2,date3");
    }

    @Test
    public void testFormatTimeValue15h50m() throws Exception {
        TestedMetric metric = new TestedMetric();
        Row row = new MetricRow(metric, parameters);

        List<List<ValueData>> data = row.getData(context, 1);
        assertEquals(data.size(), 1);

        List<ValueData> items = data.get(0);
        assertEquals(items.size(), 3);

        assertEquals(items.get(0), StringValueData.valueOf(fullDateFormat.format(new Date(1 * 60 * 60 * 1000))));
        assertEquals(items.get(1), StringValueData.valueOf(shortDateFormat.format(new Date(2 * 60 * 60 * 1000))));
        assertEquals(items.get(2), StringValueData.valueOf("10,800,000"));
    }

    // ----------------------> Tested metric

    private class TestedMetric implements Metric {

        @Override
        public ValueData getValue(Context context) throws IOException {
            Map<String, ValueData> values = new HashMap<>();
            values.put("date1", LongValueData.valueOf(1 * 60 * 60 * 1000));
            values.put("date2", LongValueData.valueOf(2 * 60 * 60 * 1000));
            values.put("date3", LongValueData.valueOf(3 * 60 * 60 * 1000));

            ValueData valueData = new MapValueData(values);
            return new ListValueData(Arrays.asList(valueData));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListValueData.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public ListValueData getExpandedValue(Context context) throws IOException {
            return null;
        }

        @Override
        public boolean isExpandable() {
            return false;
        }
    }
}
