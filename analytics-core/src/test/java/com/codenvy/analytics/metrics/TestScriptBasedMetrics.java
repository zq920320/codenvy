/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptBasedMetrics extends BaseTest {

    @Test
    public void testCalculatePerPeriod() throws Exception {
        Calendar toDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();
        fromDate.add(Calendar.DAY_OF_MONTH, -6);

        Map<String, String> context = new HashMap<String, String>(3);
        Utils.putToDate(context, toDate);
        Utils.putFromDate(context, fromDate);
        Utils.putTimeUnit(context, TimeUnit.WEEK);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.USERS_CREATED_NUMBER));

        assertEquals(mockedMetric.getValue(context), new LongValueData(70L));
    }

    /**
     * For testing purpose only.
     */
    class TestedMetric extends ScriptBasedMetric {

        TestedMetric(MetricType metricType) {
            super(metricType);
        }

        @Override
        protected ScriptType getScriptType() {
            return ScriptType.EVENT_COUNT_USER_CREATED;
        }

        @Override
        protected ValueData executeScript(Map<String, String> context) throws IOException {
            return new LongValueData(10);
        }
    }
}
