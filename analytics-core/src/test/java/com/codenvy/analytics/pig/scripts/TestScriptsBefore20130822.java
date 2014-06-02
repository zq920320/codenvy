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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestScriptsBefore20130822 extends BaseTest {

    @Test
    public void testTotalUsageTimeBefore20130822() throws Exception {
        final String date = "20130821";

        prepareDataFor(date);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);

        Metric metric = MetricFactory.getMetric(MetricType.USAGE);
        ValueData valueData = metric.getValue(builder.build());

        assertEquals(valueData, LongValueData.valueOf((2 + 4 + 5) * 60 * 1000));
    }

    /**
     * The script which is used since 20130822 doesn't take in account old sequences of events.
     */
    @Test
    public void testTotalUsageTime20130822() throws Exception {
        final String date = "20130822";

        prepareDataFor(date);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);

        Metric metric = MetricFactory.getMetric(MetricType.USAGE);
        ValueData valueData = metric.getValue(builder.build());

        assertEquals(valueData, LongValueData.valueOf(0));
    }

    public void prepareDataFor(String date) throws Exception {
        List<Event> events = new ArrayList<>();

        // sessions #1 - 2m
        events.add(Event.Builder.createProjectCreatedEvent("ANONYMOUSUSER_user11", "ws1", "sss", "project1", "type3")
                                .withDate(date).withTime("18:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("ANONYMOUSUSER_user11", "ws1", "sss", "project1", "type3")
                                .withDate(date).withTime("18:01:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("ANONYMOUSUSER_user11", "ws1", "sss", "project1", "type3")
                                .withDate(date).withTime("18:02:00").build());

        // sessions #2 - 4m
        events.add(Event.Builder.createProjectCreatedEvent("ANONYMOUSUSER_user11", "ws1", "sss", "project1", "type1")
                                .withDate(date).withTime("19:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("ANONYMOUSUSER_user11", "ws1", "sss", "project1", "type2")
                                .withDate(date).withTime("19:04:00").build());

        // sessions #3 - 5m
        events.add(Event.Builder.createProjectCreatedEvent("user@gmail.com", "ws1", "sss", "project1", "type2")
                                .withDate(date).withTime("20:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("user@gmail.com", "ws1", "sss", "project1", "type2")
                                .withDate(date).withTime("20:05:00").build());

        // session #4 - 0m
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate(date)
                                .withTime("20:25:00").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }
}
