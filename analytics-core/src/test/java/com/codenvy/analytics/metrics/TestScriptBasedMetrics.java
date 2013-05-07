/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptBasedMetrics extends BaseTest {

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

    @Test
    public void testFilter() throws Exception {
        ListStringValueData item1 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws1"), new StringValueData("user1"),
                                                                          new StringValueData("project1"), new StringValueData("type1")));
        ListStringValueData item2 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws2"), new StringValueData("user1"),
                                                                          new StringValueData("project1"), new StringValueData("type1")));
        ListStringValueData item3 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws1"), new StringValueData("user2"),
                                                                          new StringValueData("project1"), new StringValueData("type2")));
        ListStringValueData item4 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws3"), new StringValueData("user3"),
                                                                          new StringValueData("project1"), new StringValueData("type2")));

        ListListStringValueData value = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{item1, item2, item3, item4}));


        Map<String, String> context = Utils.initilizeContext(TimeUnit.DAY, new Date());
        context.put(Metric.USER_FILTER_PARAM, "user1");

        ProjectCreatedListMetric metric = spy(new ProjectCreatedListMetric());
        doReturn(value).when(metric).executeScript(anyMap());

        value = (ListListStringValueData)metric.getValue(context);
        assertEquals(value.getAll().size(), 2);
        assertTrue(value.getAll().contains(item1));
        assertTrue(value.getAll().contains(item2));
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
