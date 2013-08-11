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

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestNumberOfActiveUsersMetric {
    @Test
    public void testGetValues() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "session", "project1", "type")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "session", "project1", "type")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "session", "project2", "type")
                        .withDate("2010-10-02").build());
        events.add(Event.Builder.createProjectCreatedEvent("user3@gmail.com", "ws3", "session", "project1", "type")
                        .withDate("2010-10-03").build());
        File log = LogGenerator.generateLog(events);


        Map<String, String> context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, "20101001");
        Utils.putToDate(context, "20101001");

        DataProcessing.calculateAndStore(MetricType.USERS_CREATED_PROJECT_ONCE, context);
        Metric metric = MetricFactory.createMetric(MetricType.USERS_CREATED_PROJECT_ONCE);
        LongValueData value = (LongValueData)metric.getValue(context);
        assertEquals(value.getAsLong(), 2);

        context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, "20101002");
        Utils.putToDate(context, "20101002");

        DataProcessing.calculateAndStore(MetricType.USERS_CREATED_PROJECT_ONCE, context);
        metric = MetricFactory.createMetric(MetricType.USERS_CREATED_PROJECT_ONCE);
        value = (LongValueData)metric.getValue(context);
        assertEquals(value.getAsLong(), 2);

        context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, "20101003");
        Utils.putToDate(context, "20101003");

        DataProcessing.calculateAndStore(MetricType.USERS_CREATED_PROJECT_ONCE, context);
        metric = MetricFactory.createMetric(MetricType.USERS_CREATED_PROJECT_ONCE);
        value = (LongValueData)metric.getValue(context);
        assertEquals(value.getAsLong(), 3);

    }

}
