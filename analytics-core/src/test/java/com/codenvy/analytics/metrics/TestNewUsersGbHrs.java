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
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
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

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsDouble;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getExpandedValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Anatoliy Bazko */
public class TestNewUsersGbHrs extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent(UID1, "user1@gmail.com", "user1@gmail.com").withDate("2014-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID2, "user2@gmail.com", "user2@gmail.com").withDate("2014-01-02").build());

        /** BUILD EVENTS */
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", UID1)
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1")
                                      .withParam("TIMEOUT", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", UID1)
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1")
                                      .withParam("TIMEOUT", "600")
                                      .build());

        events.add(new Event.Builder().withDate("2014-01-02")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", UID1)
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2")
                                      .withParam("TIMEOUT", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-02")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", UID1)
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2")
                                      .withParam("TIMEOUT", "600")
                                      .build());

        events.add(new Event.Builder().withDate("2014-01-02")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", UID2)
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3")
                                      .withParam("TIMEOUT", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-02")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", UID1)
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3")
                                      .withParam("TIMEOUT", "600")
                                      .build());

        File log = LogGenerator.generateLog(events);

        executeScripts(log, "20140101");
        doIntegrity("20140101");

        executeScripts(log, "20140102");
        doIntegrity("20140102");
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

        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());
    }

    @Test
    public void testGetValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.NEW_USERS_GB_HOURS);

        // user1 0.05GbHrs
        Context context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140101").build();
        DoubleValueData d = getAsDouble(metric, context);
        assertEquals(Math.round(d.getAsDouble() * 100), 5);

        // user1 0.05GbHrs, but wasn't created today
        // user2 0.05GbHrs
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140102").put(Parameters.TO_DATE, "20140102").build();
        d = getAsDouble(metric, context);
        assertEquals(Math.round(d.getAsDouble() * 100), 5);

        // user1 0.10GbHrs
        // user2 0.05GbHrs
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140102").build();
        d = getAsDouble(metric, context);
        assertEquals(Math.round(d.getAsDouble() * 100), 15);
    }

    @Test
    public void testGetExpandedValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.NEW_USERS_GB_HOURS);

        // user1 0.05GbHrs
        Context context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140101").build();
        ListValueData l = getExpandedValue(metric, context);
        assertEquals(l.size(), 1);

        Map<String, Map<String, ValueData>> m = listToMap(l, "user");
        assertTrue(m.containsKey(UID1));

        // user1 0.05GbHrs, but wasn't created today
        // user2 0.05GbHrs
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140102").put(Parameters.TO_DATE, "20140102").build();
        l = getExpandedValue(metric, context);
        assertEquals(l.size(), 1);

        m = listToMap(l, "user");
        assertTrue(m.containsKey(UID2));

        // user1 0.10GbHrs
        // user2 0.05GbHrs
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140102").build();
        l = getExpandedValue(metric, context);
        assertEquals(l.size(), 2);

        m = listToMap(l, "user");
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
    }
}


