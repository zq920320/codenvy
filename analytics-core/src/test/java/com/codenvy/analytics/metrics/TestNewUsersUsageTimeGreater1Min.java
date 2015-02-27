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

package com.codenvy.analytics.metrics;


import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getExpandedValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Anatoliy Bazko */
public class TestNewUsersUsageTimeGreater1Min extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent(UID1, "user1@gmail.com", "user1@gmail.com").withDate("2014-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID2, "user2@gmail.com", "user2@gmail.com").withDate("2014-01-02").build());

        events.add(Event.Builder.createSessionUsageEvent(UID1, "ws", "s1", false).withDate("2014-01-01").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(UID1, "ws", "s1", false).withDate("2014-01-01").withTime("10:00:15").build());

        events.add(Event.Builder.createSessionUsageEvent(UID1, "ws", "s2", false).withDate("2014-01-01").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(UID1, "ws", "s2", false).withDate("2014-01-01").withTime("11:00:15").build());

        events.add(Event.Builder.createSessionUsageEvent(UID1, "ws", "s3", false).withDate("2014-01-02").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(UID1, "ws", "s3", false).withDate("2014-01-02").withTime("10:02:00").build());

        events.add(Event.Builder.createSessionUsageEvent(UID2, "ws", "s5", false).withDate("2014-01-02").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(UID2, "ws", "s5", false).withDate("2014-01-02").withTime("11:02:00").build());

        events.add(Event.Builder.createSessionUsageEvent(UID2, "ws", "s5", false).withDate("2014-01-02").withTime("12:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(UID2, "ws", "s5", false).withDate("2014-01-02").withTime("12:02:00").build());

        File log = LogGenerator.generateLog(events);

        executeScripts(log, "20140101");
        doComputation("20140101");

        executeScripts(log, "20140102");
        doComputation("20140102");
    }

    private void executeScripts(File log, String date) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void testGetValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.NEW_USERS_USAGE_TIME_GREATER_1_MIN);

        // user1 30sec => 0
        // user2 0 => 0
        Context context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140101").build();
        LongValueData l = getAsLong(metric, context);
        assertEquals(l.getAsLong(), 0L);

        // user1 2min, but wasn't created today => 0
        // user2 2min => 1
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140102").put(Parameters.TO_DATE, "20140102").build();
        l = getAsLong(metric, context);
        assertEquals(l.getAsLong(), 1L);

        // whole period => 2
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140102").build();
        l = getAsLong(metric, context);
        assertEquals(l.getAsLong(), 2L);
    }

    @Test
    public void testGetExpandedValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.NEW_USERS_USAGE_TIME_GREATER_1_MIN);

        // user1 30sec => 0
        // user2 0 => 0
        Context context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140101").build();
        ListValueData l = getExpandedValue(metric, context);
        assertEquals(l.size(), 0);

        // user1 2min, but wasn't created today => 0
        // user2 2min => 1
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140102").put(Parameters.TO_DATE, "20140102").build();
        l = getExpandedValue(metric, context);
        assertEquals(l.size(), 1);

        Map<String, Map<String, ValueData>> m = listToMap(l, "user");
        assertTrue(m.containsKey(UID2));

        // whole period => 1
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140102").build();
        l = getExpandedValue(metric, context);
        assertEquals(l.size(), 2);

        m = listToMap(l, "user");
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
    }
}


