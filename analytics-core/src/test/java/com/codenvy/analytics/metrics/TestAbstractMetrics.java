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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestAbstractMetrics extends BaseTest {

    private Map<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        context = new HashMap<String, String>();
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldNotStoreValue() throws Exception {
        Utils.putToDate(context, Calendar.getInstance());
        Utils.putFromDate(context, Calendar.getInstance());
        Utils.putTimeUnit(context, TimeUnit.DAY);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.INVITATIONS_SENT));

        assertEquals(mockedMetric.getValue(context), new LongValueData(10L));

        mockedMetric.load(context);
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldNotStoreValueWithTimeUnit() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        Utils.putToDate(context, calendar);
        Utils.putFromDate(context, calendar);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.PROJECTS_CREATED_LIST));

        mockedMetric.load(context);
    }

    @Test
    public void shouldStoreValue() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        Utils.putToDate(context, calendar);
        Utils.putFromDate(context, calendar);
        Utils.putTimeUnit(context, TimeUnit.DAY);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.INVITATIONS_SENT));

        assertEquals(mockedMetric.getValue(context), new LongValueData(10L));
        assertEquals(mockedMetric.load(context), new LongValueData(10L));
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
            return ScriptType.EVENT_COUNT_USER_INVITE;
        }

        @Override
        protected ValueData executeScript(Map<String, String> context) throws IOException {
            return new LongValueData(10);
        }
    }
}
