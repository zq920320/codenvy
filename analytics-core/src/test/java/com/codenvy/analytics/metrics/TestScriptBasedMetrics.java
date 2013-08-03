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


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptBasedMetrics extends BaseTest {

    private LinkedHashMap<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        context = new LinkedHashMap<String, String>();
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldNotStoreValue() throws Exception {
        Utils.putFromDate(context, Calendar.getInstance());
        Utils.putToDate(context, Calendar.getInstance());
        Utils.putTimeUnit(context, TimeUnit.DAY);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.INVITATIONS_SENT_LIST));

        assertEquals(mockedMetric.getValue(context), new LongValueData(10L));

        mockedMetric.load(context);
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldNotStoreValueWithTimeUnit() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        Utils.putFromDate(context, calendar);
        Utils.putToDate(context, calendar);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.PROJECTS_CREATED_LIST));

        mockedMetric.load(context);
    }

    @Test
    public void shouldStoreValue() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        Utils.putFromDate(context, calendar);
        Utils.putToDate(context, calendar);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.USERS_CREATED_NUMBER));

        assertEquals(mockedMetric.getValue(context), new LongValueData(10L));
        assertEquals(mockedMetric.load(context), new LongValueData(10L));
    }

    @Test
    public void testCalculatePerPeriod() throws Exception {
        Calendar toDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();
        fromDate.add(Calendar.DAY_OF_MONTH, -6);

        Map<String, String> context = new HashMap<String, String>(3);
        Utils.putFromDate(context, fromDate);
        Utils.putToDate(context, toDate);
        Utils.putTimeUnit(context, TimeUnit.WEEK);

        TestedMetric mockedMetric = spy(new TestedMetric(MetricType.USERS_CREATED_NUMBER));

        assertEquals(mockedMetric.getValue(context), new LongValueData(70L));
    }

    @Test
    public void testFilter() throws Exception {
        ListStringValueData item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "project1", "type1"));
        ListStringValueData item2 = new ListStringValueData(Arrays.asList("ws2", "user1", "project1", "type1"));
        ListStringValueData item3 = new ListStringValueData(Arrays.asList("ws1", "user2", "project1", "type2"));
        ListStringValueData item4 = new ListStringValueData(Arrays.asList("ws3", "user3", "project1", "type2"));

        ListListStringValueData value = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{item1, item2, item3, item4}));


        Map<String, String> context = Utils.initializeContext(TimeUnit.DAY);
        context.put(MetricFilter.FILTER_USER.name(), "user1");

        ProjectsCreatedListMetric metric = spy(new ProjectsCreatedListMetric());
        doReturn(value).when(metric).executeScript(anyMap());

        value = (ListListStringValueData)metric.getValue(context);
        assertEquals(value.getAll().size(), 2);
        assertTrue(value.getAll().contains(item1));
        assertTrue(value.getAll().contains(item2));
    }

    /**
     * For testing purpose only.
     */
    public class TestedMetric extends PersistableScriptBasedMetric {

        TestedMetric(MetricType metricType) {
            super(metricType);
        }

        @Override
        protected ScriptType getScriptType() {
            return ScriptType.USERS_CREATED;
        }

        @Override
        protected Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        protected ValueData executeScript(Map<String, String> context) throws IOException {
            return new LongValueData(10);
        }
    }
}
