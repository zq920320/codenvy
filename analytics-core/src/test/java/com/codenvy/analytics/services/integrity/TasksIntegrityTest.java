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
package com.codenvy.analytics.services.integrity;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.metrics.DataComputation;
import com.mongodb.DBCollection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/** @author Anatoliy Bazko */
public class TasksIntegrityTest extends BaseTest {
    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
    }

    @Test
    public void testIntegrity() throws Exception {
        doIntegrity("20131019");
        doIntegrity("20131020");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131020");
        builder.put(Parameters.TO_DATE, "20131020");
        Context context = builder.build();

        DataComputation dataComputation = new DataComputation(configurator, collectionsManagement);
        dataComputation.forceExecute(context);

        DBCollection collection = mongoDb.getCollection(MetricType.TASKS.toString().toLowerCase());
        assertEquals(collection.count(), 24L);

        collection = mongoDb.getCollection(MetricType.USERS_STATISTICS.toString().toLowerCase());
        assertEquals(collection.count(), 18L);

        collection = mongoDb.getCollection(MetricType.PROJECTS_STATISTICS.toString().toLowerCase());
        assertEquals(collection.count(), 18L);

        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);

        ListValueData l = getAsList(metric, context);
        Map<String, Map<String, ValueData>> m = listToMap(l, "id");
        assertEquals(l.size(), 12);
        assertNull(m.get("id5b-2013-10-20").get("shutdown_type"));
        assertNull(m.get("id5b-2013-10-20").get("usage_time"));
        assertNull(m.get("id5b-2013-10-20").get("gigabyte_ram_hours"));

        assertNull(m.get("id5r-2013-10-20").get("shutdown_type"));
        assertNull(m.get("id5r-2013-10-20").get("usage_time"));
        assertNull(m.get("id5r-2013-10-20").get("gigabyte_ram_hours"));

        assertNull(m.get("id5d-2013-10-20").get("shutdown_type"));
        assertNull(m.get("id5d-2013-10-20").get("usage_time"));
        assertNull(m.get("id5d-2013-10-20").get("gigabyte_ram_hours"));

        assertEquals(m.get("id2b-2013-10-20").get("shutdown_type"), StringValueData.valueOf("normal"));
        assertEquals(m.get("id2b-2013-10-20").get("usage_time"), LongValueData.valueOf(60000));
        assertEquals(m.get("id2b-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.025));

        assertEquals(m.get("id2r-2013-10-20").get("shutdown_type"), StringValueData.valueOf("user"));
        assertEquals(m.get("id2r-2013-10-20").get("usage_time"), LongValueData.valueOf(60000));
        assertEquals(m.get("id2r-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.025));

        assertEquals(m.get("id2d-2013-10-20").get("shutdown_type"), StringValueData.valueOf("user"));
        assertEquals(m.get("id2d-2013-10-20").get("usage_time"), LongValueData.valueOf(60000));
        assertEquals(m.get("id2d-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.025));

        assertEquals(m.get("id3b-2013-10-20").get("shutdown_type"), StringValueData.valueOf("timeout"));
        assertEquals(m.get("id3b-2013-10-20").get("usage_time"), LongValueData.valueOf(120000));
        assertEquals(m.get("id3b-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.05));

        assertEquals(m.get("id3r-2013-10-20").get("shutdown_type"), StringValueData.valueOf("timeout"));
        assertEquals(m.get("id3r-2013-10-20").get("usage_time"), LongValueData.valueOf(120000));
        assertEquals(m.get("id3r-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.05));

        assertEquals(m.get("id3d-2013-10-20").get("shutdown_type"), StringValueData.valueOf("timeout"));
        assertEquals(m.get("id3d-2013-10-20").get("usage_time"), LongValueData.valueOf(120000));
        assertEquals(m.get("id3d-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.05));

        assertEquals(m.get("id4b-2013-10-20").get("shutdown_type"), StringValueData.valueOf("normal"));
        assertEquals(m.get("id4b-2013-10-20").get("usage_time"), LongValueData.valueOf(60000));
        assertEquals(m.get("id4b-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.025));

        assertEquals(m.get("id4r-2013-10-20").get("shutdown_type"), StringValueData.valueOf("user"));
        assertEquals(m.get("id4r-2013-10-20").get("usage_time"), LongValueData.valueOf(60000));
        assertEquals(m.get("id4r-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.025));

        assertEquals(m.get("id4d-2013-10-20").get("shutdown_type"), StringValueData.valueOf("user"));
        assertEquals(m.get("id4d-2013-10-20").get("usage_time"), LongValueData.valueOf(60000));
        assertEquals(m.get("id4d-2013-10-20").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.025));

        metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        l = getAsList(metric, context);
        assertEquals(l.size(), 3);

        m = listToMap(l, "user");
        assertEquals(m.get(UID2).get("build_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID2).get("run_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID2).get("debug_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID3).get("build_time"), LongValueData.valueOf(120000));
        assertEquals(m.get(UID3).get("run_time"), LongValueData.valueOf(120000));
        assertEquals(m.get(UID3).get("debug_time"), LongValueData.valueOf(120000));
        assertEquals(m.get(UID4).get("build_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID4).get("run_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID4).get("debug_time"), LongValueData.valueOf(60000));

        metric = MetricFactory.getMetric(MetricType.PROJECTS_STATISTICS_LIST);
        l = getAsList(metric, context);
        assertEquals(l.size(), 3);

        m = listToMap(l, "project_id");
        assertEquals(m.get(UID2 + "/" + WID2 + "/project2").get("build_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID2 + "/" + WID2 + "/project2").get("run_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID2 + "/" + WID2 + "/project2").get("debug_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID3 + "/" + WID3 + "/project3").get("build_time"), LongValueData.valueOf(120000));
        assertEquals(m.get(UID3 + "/" + WID3 + "/project3").get("run_time"), LongValueData.valueOf(120000));
        assertEquals(m.get(UID3 + "/" + WID3 + "/project3").get("debug_time"), LongValueData.valueOf(120000));
        assertEquals(m.get(UID4 + "/" + WID4 + "/project4").get("build_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID4 + "/" + WID4 + "/project4").get("run_time"), LongValueData.valueOf(60000));
        assertEquals(m.get(UID4 + "/" + WID4 + "/project4").get("debug_time"), LongValueData.valueOf(60000));
    }

    private void prepareData() throws Exception {
        addPersistentWs(WID1, "ws1");
        addPersistentWs(WID2, "ws2");
        addPersistentWs(WID3, "ws3");
        addPersistentWs(WID4, "ws4");
        addRegisteredUser(UID1, "user1");
        addRegisteredUser(UID2, "user2");
        addRegisteredUser(UID3, "user3");
        addRegisteredUser(UID4, "user4");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131019");
        builder.put(Parameters.TO_DATE, "20131019");
        builder.put(Parameters.LOG, initLogs("2013-10-19").getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131020");
        builder.put(Parameters.TO_DATE, "20131020");
        builder.put(Parameters.LOG, initLogs("2013-10-20").getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());
    }

    private File initLogs(String date) throws Exception {
        List<Event> events = new ArrayList<>();

        // BUILDS

        // #1 build-finished event without build-started, will be removed
        events.add(new Event.Builder().withDate(date)
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1b-" + date)
                                      .withParam("TIMEOUT", "-1")
                                      .build());

        // #2 1m, stopped normally
        events.add(new Event.Builder().withDate(date)
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2b-" + date)
                                      .withParam("TIMEOUT", "-1")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws2")
                                      .withParam("USER", "user2")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2b-" + date)
                                      .withParam("TIMEOUT", "-1")
                                      .build());

        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate(date)
                                      .withTime("12:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws3")
                                      .withParam("USER", "user3")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3b-" + date)
                                      .withParam("TIMEOUT", "60000")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("12:02:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3b-" + date)
                                      .withParam("TIMEOUT", "60000")
                                      .build());

        // #4 1m, stopped normally
        events.add(new Event.Builder().withDate(date)
                                      .withTime("13:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws4")
                                      .withParam("USER", "user4")
                                      .withParam("PROJECT", "project4")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4b-" + date)
                                      .withParam("TIMEOUT", "120000")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("13:01:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project4")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4b-" + date)
                                      .withParam("TIMEOUT", "120000")
                                      .build());

        // #5 build-started event without build-finished
        events.add(new Event.Builder().withDate(date)
                                      .withTime("14:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id5b-" + date)
                                      .withParam("TIMEOUT", "-1")
                                      .build());

        // RUNS

        // #1 run-finished event without run-started, will be removed
        events.add(new Event.Builder().withDate(date)
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "-1")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate(date)
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws2")
                                      .withParam("USER", "user2")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "-1")
                                      .build());

        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate(date)
                                      .withTime("12:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws3")
                                      .withParam("USER", "user3")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "60000")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("12:02:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "60000")
                                      .build());

        // #4 1m, stopped by user
        events.add(new Event.Builder().withDate(date)
                                      .withTime("13:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws4")
                                      .withParam("USER", "user4")
                                      .withParam("PROJECT", "project4")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "120000")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("13:01:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project4")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "120000")
                                      .build());

        // #5 run-started event without build-finished
        events.add(new Event.Builder().withDate(date)
                                      .withTime("14:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id5r-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "120000")
                                      .build());


        // DEBUGS

        // #1 debug-finished event without debug-started, will be removed
        events.add(new Event.Builder().withDate(date)
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "-1")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate(date)
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws2")
                                      .withParam("USER", "user2")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "-1")
                                      .build());

        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate(date)
                                      .withTime("12:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws3")
                                      .withParam("USER", "user3")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "60000")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("12:02:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "60000")
                                      .build());

        // #4 1m, stopped by user
        events.add(new Event.Builder().withDate(date)
                                      .withTime("13:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws4")
                                      .withParam("USER", "user4")
                                      .withParam("PROJECT", "project4")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "120000")
                                      .build());
        events.add(new Event.Builder().withDate(date)
                                      .withTime("13:01:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project4")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "120000")
                                      .build());

        // #5 debug-started event without build-finished
        events.add(new Event.Builder().withDate(date)
                                      .withTime("14:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id5d-" + date)
                                      .withParam("MEMORY", "1536")
                                      .withParam("LIFETIME", "120000")
                                      .build());


        return LogGenerator.generateLog(events);
    }

}