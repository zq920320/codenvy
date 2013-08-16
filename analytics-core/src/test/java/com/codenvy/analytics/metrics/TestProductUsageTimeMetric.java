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

import com.codenvy.analytics.metrics.value.ListListStringValueData;
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
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsageTimeMetric {

    private HashMap<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        // session started and session finished [5m]
        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2010-10-09")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2010-10-09")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createSessionStartedEvent("user2@gmail.com", "ws1", "ide", "2").withDate("2010-10-09")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2@gmail.com", "ws1", "ide", "2").withDate("2010-10-09")
                        .withTime("20:05:00").build());
        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, "20101009");
        Utils.putToDate(context, "20101009");

        DataProcessing.calculateAndStore(MetricType.PRODUCT_USAGE_SESSIONS, context);
    }

    @Test
    public void testGetValues() throws Exception {
        Metric metric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        ListListStringValueData value = (ListListStringValueData)metric.getValue(context);

        assertEquals(value.size(), 2);
        assertEquals(Long.valueOf(value.getAll().get(0).getAll().get(3)).longValue(), 300);
        assertEquals(Long.valueOf(value.getAll().get(1).getAll().get(3)).longValue(), 300);
    }

    @Test
    public void testGetValuesWithUserFilters() throws Exception {
        context.put(MetricFilter.FILTER_USER.name(), "user1@gmail.com");

        Metric metric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        ListListStringValueData value = (ListListStringValueData)metric.getValue(context);

        assertEquals(value.size(), 1);
        assertEquals(Long.valueOf(value.getAll().get(0).getAll().get(3)).longValue(), 300);
    }

    @Test
    public void testGetValuesWithDomainsFilters() throws Exception {
        context.put(MetricFilter.FILTER_USER.name(), "@gmail.com");

        Metric metric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        ListListStringValueData value = (ListListStringValueData)metric.getValue(context);

        assertEquals(value.size(), 2);
        assertEquals(Long.valueOf(value.getAll().get(0).getAll().get(3)).longValue(), 300);
        assertEquals(Long.valueOf(value.getAll().get(1).getAll().get(3)).longValue(), 300);
    }
}
