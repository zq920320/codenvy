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

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestReadBasedMetric {

    public static final MetricType METRIC_TYPE = MetricType.USER_CODE_REFACTOR;

    private HashMap<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user1@gmail.com", "project1", "type", "feature").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user2@gmail.com", "project2", "type", "feature").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCodeRefactorEvent("", "", "project2", "type", "feature").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, "20101001");
        Utils.putToDate(context, "20101001");
        Utils.putEvent(context, "user-code-refactor");

        DataProcessing.runFor(METRIC_TYPE, context);
    }

    @Test
    public void testGetValues() throws Exception {
        Metric metric = MetricFactory.createMetric(METRIC_TYPE);

        LongValueData value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 3);
    }

    @Test
    public void testGetValuesWithUserFilters() throws Exception {
        Metric metric = MetricFactory.createMetric(METRIC_TYPE);

        context.put(MetricFilter.FILTER_USER.name(), "user1@gmail.com");

        LongValueData value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 1);

        context.put(MetricFilter.FILTER_USER.name(), "user2@gmail.com");

        value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 1);

        context.put(MetricFilter.FILTER_USER.name(), "user1@gmail.com,user2@gmail.com");

        value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 2);

        context.put(MetricFilter.FILTER_USER.name(), "@gmail.com");

        value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 2);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetValuesWihtDoubleFilterFilters() throws Exception {
        Metric metric = MetricFactory.createMetric(METRIC_TYPE);

        context.put(MetricFilter.FILTER_USER.name(), "user@gmail.com");
        context.put(MetricFilter.FILTER_WS.name(), "ws");

        LongValueData value = (LongValueData) metric.getValue(context);
    }

    @Test
    public void testGetValuesAnotherPeriodFilters() throws Exception {
        Metric metric = MetricFactory.createMetric(METRIC_TYPE);

        Utils.putFromDate(context, "20101002");
        Utils.putToDate(context, "20101002");

        LongValueData value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 0);

        Utils.putFromDate(context, "20101001");
        Utils.putToDate(context, "20101002");

        value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 3);
    }

    @Test
    public void testGetValuesWihtWsFilters() throws Exception {
        Metric metric = MetricFactory.createMetric(METRIC_TYPE);

        context.put(MetricFilter.FILTER_WS.name(), "ws");

        LongValueData value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 2);

        context.put(MetricFilter.FILTER_WS.name(), "default");

        value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(), 0);
    }
}
