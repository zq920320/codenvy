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
import com.codenvy.analytics.datamodel.MapValueData;
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
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.metrics.MetricFactory.getMetric;
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
        assertEquals(l.getAsLong(), 14);
    }

    @Test
    public void testTasksList() throws IOException {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);

        ListValueData value = (ListValueData)(metric).getValue(Context.EMPTY);
        assertEquals(value.size(), 14);

        Map<String, Map<String, ValueData>> m = listToMap(value, "id");
        assertEquals(m.get("session1").toString(), "{"
                                                   + "date=1382274000000, "
                                                   + "user=anonymoususer_user11, "
                                                   + "ws=ws1, "
                                                   + "project=project, "
                                                   + "project_type=projecttype, "
                                                   + "project_id=anonymoususer_user11/ws1/project, "
                                                   + "persistent_ws=0, "
                                                   + "id=session1, "
                                                   + "task_type=editor, "
                                                   + "memory=25, "
                                                   + "usage_time=60000, "
                                                   + "start_time=1382274000000, "
                                                   + "stop_time=1382274060000, "
                                                   + "gigabyte_ram_hours=4.069010416666667E-4, "
                                                   + "is_factory=1"
                                                   + "}");

        assertEquals(m.get("session3").toString(), "{"
                                                    + "date=1382275200000, "
                                                    + "user=user1@gmail.com, "
                                                    + "ws=ws1, "
                                                    + "project=project, "
                                                    + "project_type=projecttype, "
                                                    + "project_id=user1@gmail.com/ws1/project, "
                                                    + "persistent_ws=0, "
                                                    + "id=session3, "
                                                    + "task_type=editor, "
                                                    + "memory=25, "
                                                    + "usage_time=60000, "
                                                    + "start_time=1382275200000, "
                                                    + "stop_time=1382275260000, "
                                                    + "gigabyte_ram_hours=4.069010416666667E-4, "
                                                    + "is_factory=0"
                                                    + "}");

        assertEquals(m.get("id1_b").toString(), "{"
                                                + "date=1382252400000, "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id1_b, "
                                                + "task_type=builder, "
                                                + "memory=1536, "
                                                + "usage_time=120000, "
                                                + "start_time=1382252400000, "
                                                + "stop_time=1382252520000, "
                                                + "gigabyte_ram_hours=0.05, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=normal"
                                                + "}");

        assertEquals(m.get("id2_b").toString(), "{"
                                                + "date=1382256000000, "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project2, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project2, "
                                                + "persistent_ws=0, "
                                                + "id=id2_b, "
                                                + "task_type=builder, "
                                                + "memory=250, "
                                                + "usage_time=60000, "
                                                + "start_time=1382256000000, "
                                                + "stop_time=1382256060000, "
                                                + "gigabyte_ram_hours=0.004069010416666667, "
                                                + "is_factory=1, "
                                                + "launch_type=always-on, "
                                                + "shutdown_type=normal"
                                                + "}");

        assertEquals(m.get("id3_b").toString(), "{"
                                                + "date=1382256000000, "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project3, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project3, "
                                                + "persistent_ws=0, "
                                                + "id=id3_b, "
                                                + "task_type=builder, "
                                                + "memory=1536, "
                                                + "usage_time=120000, "
                                                + "start_time=1382256000000, "
                                                + "stop_time=1382256120000, "
                                                + "gigabyte_ram_hours=0.05, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=timeout"
                                                + "}");

        assertEquals(m.get("id1_r").toString(), "{"
                                                + "date=1382252400000, "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id1_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=1382252400000, "
                                                + "stop_time=1382252520000, "
                                                + "gigabyte_ram_hours=0.004166666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id2_r").toString(), "{"
                                                + "date=1382256000000, "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project2, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project2, "
                                                + "persistent_ws=0, "
                                                + "id=id2_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=1382256000000, "
                                                + "stop_time=1382256120000, "
                                                + "gigabyte_ram_hours=0.004166666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=always-on, "
                                                + "shutdown_type=timeout"
                                                + "}");

        assertEquals(m.get("id3_r").toString(), "{"
                                                + "date=1382256000000, "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project3, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project3, "
                                                + "persistent_ws=0, "
                                                + "id=id3_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=60000, "
                                                + "start_time=1382256000000, "
                                                + "stop_time=1382256060000, "
                                                + "gigabyte_ram_hours=0.0020833333333333333, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id4_r").toString(), "{"
                                                + "date=1382259600000, "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id4_r, "
                                                + "task_type=runner, "
                                                + "start_time=1382259600000, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout"
                                                + "}");
    }

    @Test
    public void testTaskListFilteredByRunsMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.RUNS.toString());

        Metric metric = getMetric(MetricType.TASKS_LIST);

        ListValueData value = getAsList(metric, builder.build());
        assertEquals(value.size(), 4);
        Map<String, Map<String, ValueData>> m = listToMap(value, "id");

        assertEquals(m.get("id1_r").toString(), "{"
                                                          + "date=1382252400000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project1, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project1, "
                                                          + "persistent_ws=0, "
                                                          + "id=id1_r, "
                                                          + "task_type=runner, "
                                                          + "memory=128, "
                                                          + "usage_time=120000, "
                                                          + "start_time=1382252400000, "
                                                          + "stop_time=1382252520000, "
                                                          + "gigabyte_ram_hours=0.004166666666666667, "
                                                          + "is_factory=1, "
                                                          + "launch_type=timeout, "
                                                          + "shutdown_type=user"
                                                          + "}");

        assertEquals(m.get("id2_r").toString(), "{"
                                                          + "date=1382256000000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project2, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project2, "
                                                          + "persistent_ws=0, "
                                                          + "id=id2_r, "
                                                          + "task_type=runner, "
                                                          + "memory=128, "
                                                          + "usage_time=120000, "
                                                          + "start_time=1382256000000, "
                                                          + "stop_time=1382256120000, "
                                                          + "gigabyte_ram_hours=0.004166666666666667, "
                                                          + "is_factory=1, "
                                                          + "launch_type=always-on, "
                                                          + "shutdown_type=timeout"
                                                          + "}");

        assertEquals(m.get("id3_r").toString(), "{"
                                                          + "date=1382256000000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project3, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project3, "
                                                          + "persistent_ws=0, "
                                                          + "id=id3_r, "
                                                          + "task_type=runner, "
                                                          + "memory=128, "
                                                          + "usage_time=60000, "
                                                          + "start_time=1382256000000, "
                                                          + "stop_time=1382256060000, "
                                                          + "gigabyte_ram_hours=0.0020833333333333333, "
                                                          + "is_factory=1, "
                                                          + "launch_type=timeout, "
                                                          + "shutdown_type=user"
                                                          + "}");

        assertEquals(m.get("id4_r").toString(), "{"
                                                          + "date=1382259600000, "
                                                          + "user=user, "
                                                          + "ws=ws, "
                                                          + "project=project1, "
                                                          + "project_type=projecttype, "
                                                          + "project_id=user/ws/project1, "
                                                          + "persistent_ws=0, "
                                                          + "id=id4_r, "
                                                          + "task_type=runner, "
                                                          + "start_time=1382259600000, "
                                                          + "is_factory=1, "
                                                          + "launch_type=timeout"
                                                          + "}");
    }

    @Test
    public void testTestSummaryOfProjectsStatisticsListMetric() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);

        ListValueData summaryValue = (ListValueData)((Summaraziable)metric).getSummaryValue(Context.EMPTY);
        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> m = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(m.toString(), "{usage_time=1260000, gigabyte_ram_hours=0.1273}");
    }

    @Test
    public void testTasksLaunched() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 10);
    }

    @Test
    public void testExpandedTasksLaunched() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id4_r={id=id4_r}, "
                                   + "id1_b={id=id1_b}, "
                                   + "id1_r={id=id1_r}, "
                                   + "id1_d={id=id1_d}, "
                                   + "id3_b={id=id3_b}, "
                                   + "id3_r={id=id3_r}, "
                                   + "id2_d={id=id2_d}, "
                                   + "id2_b={id=id2_b}, "
                                   + "id2_r={id=id2_r}, "
                                   + "id3_d={id=id3_d}}");
    }

    @Test
    public void testTasksStopped() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 9);
    }

    @Test
    public void testExpandedTasksStopped() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id1_b={id=id1_b}, " +
                                   "id1_r={id=id1_r}, " +
                                   "id1_d={id=id1_d}, " +
                                   "id3_b={id=id3_b}, " +
                                   "id3_r={id=id3_r}, " +
                                   "id2_d={id=id2_d}, " +
                                   "id2_b={id=id2_b}, " +
                                   "id2_r={id=id2_r}, " +
                                   "id3_d={id=id3_d}}");
    }

    @Test
    public void testTasksTime() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_TIME);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 900000);
    }

    @Test
    public void testExpandedTasksTime() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_TIME);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id1_b={id=id1_b}, "
                                   + "id1_r={id=id1_r}, "
                                   + "id1_d={id=id1_d}, "
                                   + "id3_b={id=id3_b}, "
                                   + "id3_r={id=id3_r}, "
                                   + "id2_d={id=id2_d}, "
                                   + "id2_b={id=id2_b}, "
                                   + "id2_r={id=id2_r}, "
                                   + "id3_d={id=id3_d}}");
    }

    @Test
    public void testTasksGigabyteRamHours() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_GIGABYTE_RAM_HOURS);
        DoubleValueData d = getAsDouble(metric, Context.EMPTY);
        assertEquals(round(d.getAsDouble() * 10000), 1458);
    }

    @Test
    public void testExpandedTasksGigabyteRamHours() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_GIGABYTE_RAM_HOURS);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id1_b={id=id1_b}, "
                                   + "id1_r={id=id1_r}, "
                                   + "id1_d={id=id1_d}, "
                                   + "id3_b={id=id3_b}, "
                                   + "id3_r={id=id3_r}, "
                                   + "id2_d={id=id2_d}, "
                                   + "id2_b={id=id2_b}, "
                                   + "id2_r={id=id2_r}, "
                                   + "id3_d={id=id3_d}}");
    }

    @Test
    public void testTasksLaunchedWithTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_TIMEOUT);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 7);
    }

    @Test
    public void testExpandedTasksLaunchedWithTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_TIMEOUT);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id4_r={id=id4_r}, " +
                                   "id1_b={id=id1_b}, " +
                                   "id1_r={id=id1_r}, " +
                                   "id1_d={id=id1_d}, " +
                                   "id3_b={id=id3_b}, " +
                                   "id3_r={id=id3_r}, " +
                                   "id3_d={id=id3_d}}");
    }

    @Test
    public void testTasksLaunchedWithAlwaysOn() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_ALWAYS_ON);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 3);
    }

    @Test
    public void testExpandedTasksLaunchedWithAlwaysOn() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_ALWAYS_ON);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id2_d={id=id2_d}, " +
                                   "id2_b={id=id2_b}, " +
                                   "id2_r={id=id2_r}}");
    }

    @Test
    public void testTasksStoppedNormally() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_NORMALLY);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 6);
    }

    @Test
    public void testExpandedTasksStoppedNormally() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_NORMALLY);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id1_b={id=id1_b}, "
                                   + "id1_r={id=id1_r}, "
                                   + "id1_d={id=id1_d}, "
                                   + "id3_r={id=id3_r}, "
                                   + "id2_d={id=id2_d}, "
                                   + "id2_b={id=id2_b}}");
    }

    @Test
    public void testTasksStoppedByTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_BY_TIMEOUT);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 3);
    }

    @Test
    public void testExpandedStoppedByTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_BY_TIMEOUT);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.toString(), "{id3_b={id=id3_b}, " +
                                   "id2_r={id=id2_r}, " +
                                   "id3_d={id=id3_d}}");
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


        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.DEBUGS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.DEBUGS_FINISHED).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USED_TIME, MetricType.DEBUGS_TIME).getParamsAsMap());
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
                                      .withTime("10:02:00")
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
                                      .withTime("11:02:00")
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
                                      .withTime("10:02:00")
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
                                      .withTime("11:02:00")
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

        // #1 2min, non-finished run
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("12:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());

        /** DEBUGS EVENTS */
        // #1 2min, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("13:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("13:02:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("STOPPED-BY-USER", "1")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("14:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("14:01:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .withParam("USAGE-TIME", "60000")
                                      .withParam("STOPPED-BY-USER", "1")
                                      .build());


        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("15:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "60")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("15:02:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "60")
                                      .withParam("USAGE-TIME", "120000")
                                      .withParam("STOPPED-BY-USER", "0")
                                      .build());

        /** EDIT EVENTS */
        events.add(Event.Builder.createExtendedSessionUsageEvent("anonymoususer_user11", "ws1", "project", "projectType", "session1", true)
                                .withDate("2013-10-20")
                                .withTime("16:00:00")
                                .build());
        events.add(Event.Builder.createExtendedSessionUsageEvent("anonymoususer_user11", "ws1", "project", "projectType", "session1", true)
                                .withDate("2013-10-20")
                                .withTime("16:01:00")
                                .build());


        events.add(Event.Builder.createExtendedSessionUsageEvent("anonymoususer_user11", "ws1", "project", "projectType", "session2", true)
                                .withDate("2013-10-20")
                                .withTime("16:10:00")
                                .build());
        events.add(Event.Builder.createExtendedSessionUsageEvent("anonymoususer_user11", "ws1", "project", "projectType", "session2", true)
                                .withDate("2013-10-20")
                                .withTime("16:12:00")
                                .build());

        events.add(Event.Builder.createExtendedSessionUsageEvent("user1@gmail.com", "ws1", "project", "projectType", "session3", false)
                                .withDate("2013-10-20")
                                .withTime("16:20:00")
                                .build());
        events.add(Event.Builder.createExtendedSessionUsageEvent("user1@gmail.com", "ws1", "project", "projectType", "session3", false)
                                .withDate("2013-10-20")
                                .withTime("16:21:00")
                                .build());

        events.add(Event.Builder.createExtendedSessionUsageEvent("user1@gmail.com", "ws1", "project", "projectType", "session4", false)
                                .withDate("2013-10-20")
                                .withTime("16:30:00")
                                .build());
        events.add(Event.Builder.createExtendedSessionUsageEvent("user1@gmail.com", "ws1", "project", "projectType", "session4", false)
                                .withDate("2013-10-20")
                                .withTime("16:32:00")
                                .build());

        return LogGenerator.generateLog(events);
    }
}
