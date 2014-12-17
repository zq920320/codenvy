/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsDouble;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static java.lang.Math.round;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestTaskerMetrics extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
    }

    @Test
    public void testTasks() throws IOException {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 6);
    }

    @Test
    public void testTasksList() throws IOException {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);

        ListValueData value = (ListValueData)(metric).getValue(Context.EMPTY);
        assertEquals(value.size(), 6);

        List<ValueData> tasks = value.getAll();
        assertEquals(treatAsMap(tasks.get(0)).toString(), "{"
                                                          + "date=1382252400000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project1, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project1, "
                                                          + "persistent_ws=0, "
                                                          + "task_id=id1_b, "
                                                          + "task_type=builder, "
                                                          + "memory=1536, "
                                                          + "usage_time=120000, "
                                                          + "started_time=1382252400000, "
                                                          + "stopped_time=1382253000000, "
                                                          + "gigabyte_ram_hours=0.05, "
                                                          + "launch_type=timeout, "
                                                          + "shutdown_type=normally"
                                                          + "}");

        assertEquals(treatAsMap(tasks.get(1)).toString(), "{"
                                                          + "date=1382256000000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project2, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project2, "
                                                          + "persistent_ws=0, "
                                                          + "task_id=id2_b, "
                                                          + "task_type=builder, "
                                                          + "memory=250, "
                                                          + "usage_time=60000, "
                                                          + "started_time=1382256000000, "
                                                          + "stopped_time=1382256060000, "
                                                          + "gigabyte_ram_hours=0.004069010416666667, "
                                                          + "launch_type=always-on, "
                                                          + "shutdown_type=normally"
                                                          + "}");

        assertEquals(treatAsMap(tasks.get(2)).toString(), "{"
                                                          + "date=1382256000000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project3, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project3, "
                                                          + "persistent_ws=0, "
                                                          + "task_id=id3_b, "
                                                          + "task_type=builder, "
                                                          + "memory=1536, "
                                                          + "usage_time=120000, "
                                                          + "started_time=1382256000000, "
                                                          + "stopped_time=1382256060000, "
                                                          + "gigabyte_ram_hours=0.05, "
                                                          + "launch_type=timeout, "
                                                          + "shutdown_type=timeout"
                                                          + "}");

        assertEquals(treatAsMap(tasks.get(3)).toString(), "{"
                                                          + "date=1382252400000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project1, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project1, "
                                                          + "persistent_ws=0, "
                                                          + "task_id=id1_r, "
                                                          + "task_type=runner, "
                                                          + "memory=128, "
                                                          + "usage_time=120000, "
                                                          + "started_time=1382252400000, "
                                                          + "stopped_time=1382253000000, "
                                                          + "gigabyte_ram_hours=0.004166666666666667, "
                                                          + "launch_type=timeout, "
                                                          + "shutdown_type=normally"
                                                          + "}");

        assertEquals(treatAsMap(tasks.get(4)).toString(), "{"
                                                          + "date=1382256000000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project2, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project2, "
                                                          + "persistent_ws=0, "
                                                          + "task_id=id2_r, "
                                                          + "task_type=runner, "
                                                          + "memory=128, "
                                                          + "usage_time=120000, "
                                                          + "started_time=1382256000000, "
                                                          + "stopped_time=1382256060000, "
                                                          + "gigabyte_ram_hours=0.004166666666666667, "
                                                          + "launch_type=always-on, "
                                                          + "shutdown_type=timeout"
                                                          + "}");

        assertEquals(treatAsMap(tasks.get(5)).toString(), "{"
                                                          + "date=1382256000000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project3, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project3, "
                                                          + "persistent_ws=0, "
                                                          + "task_id=id3_r, "
                                                          + "task_type=runner, "
                                                          + "memory=128, "
                                                          + "usage_time=60000, "
                                                          + "started_time=1382256000000, "
                                                          + "stopped_time=1382256060000, "
                                                          + "gigabyte_ram_hours=0.0020833333333333333, "
                                                          + "launch_type=timeout, "
                                                          + "shutdown_type=normally"
                                                          + "}");
    }

    @Test
    public void testTasksLaunched() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 6);
    }

    @Test
    public void testExpandedTasksLaunched() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project1={project_id=user/ws/project1}, " +
                                   "user/ws/project3={project_id=user/ws/project3}, " +
                                   "user/ws/project2={project_id=user/ws/project2}}");
    }

    @Test
    public void testTasksStopped() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 6);
    }

    @Test
    public void testExpandedTasksStopped() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project1={project_id=user/ws/project1}, " +
                                   "user/ws/project3={project_id=user/ws/project3}, " +
                                   "user/ws/project2={project_id=user/ws/project2}}");
    }

    @Test
    public void testTasksTime() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_TIME);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 600000);
    }

    @Test
    public void testExpandedTasksTime() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_TIME);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project1={project_id=user/ws/project1}, " +
                                   "user/ws/project3={project_id=user/ws/project3}, " +
                                   "user/ws/project2={project_id=user/ws/project2}}");
    }

    @Test
    public void testTasksMemoryUsagePerHour() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_MEMORY_USAGE_PER_HOUR);
        DoubleValueData d = getAsDouble(metric, Context.EMPTY);
        assertEquals(round(d.getAsDouble() * 10000), 1354);
    }

    @Test
    public void testExpandedTasksMemoryUsagePerHour() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_MEMORY_USAGE_PER_HOUR);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project1={project_id=user/ws/project1}, " +
                                   "user/ws/project3={project_id=user/ws/project3}, " +
                                   "user/ws/project2={project_id=user/ws/project2}}");
    }

    @Test
    public void testTasksLaunchedWithTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_TIMEOUT);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 4);
    }

    @Test
    public void testExpandedTasksLaunchedWithTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_TIMEOUT);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project1={project_id=user/ws/project1}, " +
                                   "user/ws/project3={project_id=user/ws/project3}}");
    }

    @Test
    public void testTasksLaunchedWithAlwaysOn() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_ALWAYS_ON);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 2);
    }

    @Test
    public void testExpandedTasksLaunchedWithAlwaysOn() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_ALWAYS_ON);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project2={project_id=user/ws/project2}}");
    }

    @Test
    public void testTasksStoppedNormally() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_NORMALLY);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 4);
    }

    @Test
    public void testExpandedTasksStoppedNormally() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_NORMALLY);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project1={project_id=user/ws/project1}, " +
                                   "user/ws/project3={project_id=user/ws/project3}, " +
                                   "user/ws/project2={project_id=user/ws/project2}}");
    }

    @Test
    public void testTasksStoppedByTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_BY_TIMEOUT);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 2);
    }

    @Test
    public void testExpandedStoppedByTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_BY_TIMEOUT);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.toString(), "{user/ws/project3={project_id=user/ws/project3}, " +
                                   "user/ws/project2={project_id=user/ws/project2}}");
    }

    void prepareData() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131020");
        builder.put(Parameters.TO_DATE, "20131020");
        builder.put(Parameters.LOG, initLogs().getAbsolutePath());


        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILDS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILDS_FINISHED).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USED_TIME, MetricType.BUILDS_TIME).getParamsAsMap());
        pigServer.execute(ScriptType.USED_TIME, builder.build());


        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.RUNS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.RUNS_FINISHED).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USED_TIME, MetricType.RUNS_TIME).getParamsAsMap());
        pigServer.execute(ScriptType.USED_TIME, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());

    }

    private File initLogs() throws Exception {
        List<Event> events = new ArrayList<>();

        /** BUILD EVENTS */
        // #1 2min, stopped normally
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_b")
                                      .withParam("TIMEOUT", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_b")
                                      .withParam("TIMEOUT", "600")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("FINISHED-NORMALLY", "1")
                                      .build());

        // #2 1m, stopped normally
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_b")
                                      .withParam("TIMEOUT", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_b")
                                      .withParam("MEMORY", "250")
                                      .withParam("TIMEOUT", "-1")
                                      .withParam("USAGE-TIME", "60000")
                                      .withParam("FINISHED-NORMALLY", "1")
                                      .build());


        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_b")
                                      .withParam("TIMEOUT", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_b")
                                      .withParam("TIMEOUT", "600")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("FINISHED-NORMALLY", "0")
                                      .build());

        /** RUN EVENTS */
        // #1 2min, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("STOPPED-BY-USER", "1")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("STOPPED-BY-USER", "0")
                                      .build());


        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "60")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "60")
                                      .withParam("USAGE-TIME", "60000")
                                      .withParam("STOPPED-BY-USER", "1")
                                      .build());

        return LogGenerator.generateLog(events);
    }
}
