/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
public class TestMetricRow extends BaseTest {
    @Test
    public void testFormatTimeValue15h50m() throws Exception {
        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("date-fields", "date1,date2=HH:mm:ss");
        parameters.put("fields", "date1,date2,date3");

        ListValueTestedMetric metric = new ListValueTestedMetric();
        Row row = new MetricRow(metric, parameters);

        List<List<ValueData>> data = row.getData(context, 1);
        assertEquals(data.size(), 1);

        List<ValueData> items = data.get(0);
        assertEquals(items.size(), 3);

        assertEquals(items.get(0), StringValueData.valueOf(fullDateFormat.format(new Date(1 * 60 * 60 * 1000))));
        assertEquals(items.get(1), StringValueData.valueOf(shortDateFormat.format(new Date(2 * 60 * 60 * 1000))));
        assertEquals(items.get(2), StringValueData.valueOf("10,800,000"));
    }

    @Test
    public void testDateRangeFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());
        builder.put(Parameters.FROM_DATE, "20131227");
        builder.put(Parameters.TO_DATE, "20140202");    // 3 months
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        SingleValueTestedMetric metric = new SingleValueTestedMetric();
        Row row = new MetricRow(metric, new HashMap<String, String>());

        List<List<ValueData>> data = row.getData(builder.build(), 3);
        assertEquals(data.size(), 1);

        List<ValueData> items = data.get(0);
        assertEquals(items.size(), 3);

        assertEquals(items.get(0).getAsString(), "fromDate: 20140201; toDate: 20140202");
        assertEquals(items.get(1).getAsString(), "fromDate: 20140101; toDate: 20140131");
        assertEquals(items.get(2).getAsString(), "fromDate: 20131227; toDate: 20131231");
    }


    @Test
    public void testNumbers() throws Exception {
        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("numeric-format", "%,.2f");
        parameters.put("fields", "date1,date2,date3,float");

        ListValueTestedMetric metric = new ListValueTestedMetric();
        Row row = new MetricRow(metric, parameters);

        List<List<ValueData>> data = row.getData(context, 1);
        assertEquals(data.size(), 1);

        List<ValueData> items = data.get(0);
        assertEquals(items.size(), 4);

        assertEquals(items.get(0), StringValueData.valueOf("3,600,000"));
        assertEquals(items.get(1), StringValueData.valueOf("7,200,000"));
        assertEquals(items.get(2), StringValueData.valueOf("10,800,000"));
        assertEquals(items.get(3), StringValueData.valueOf("12,345.68"));
    }

    // ----------------------> Tested metric

    private class ListValueTestedMetric implements Metric {

        @Override
        public ValueData getValue(Context context) throws IOException {
            Map<String, ValueData> values = new HashMap<>();
            values.put("date1", LongValueData.valueOf(1 * 60 * 60 * 1000));
            values.put("date2", LongValueData.valueOf(2 * 60 * 60 * 1000));
            values.put("date3", LongValueData.valueOf(3 * 60 * 60 * 1000));
            values.put("float", DoubleValueData.valueOf(12345.6789));

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
    }

    private class SingleValueTestedMetric implements Metric {

        @Override
        public ValueData getValue(Context context) throws IOException {
            return StringValueData.valueOf(String.format("fromDate: %s; toDate: %s",
                                                         context.getAsString(Parameters.FROM_DATE),
                                                         context.getAsString(Parameters.TO_DATE)));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return StringValueData.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
