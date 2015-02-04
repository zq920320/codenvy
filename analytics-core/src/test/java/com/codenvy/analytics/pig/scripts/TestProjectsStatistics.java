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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.Summaraziable;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.util.MyAsserts;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestProjectsStatistics extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        addPersistentWs(WID1, "ws1");
        addRegisteredUser(UID1, "user1@gmail.com");

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project1", "jar")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectDestroyedEvent("user1@gmail.com", "ws1", "project1", "jar")
                                .withDate("2013-01-01")
                                .withTime("11:00:00")
                                .build());

        events.add(Event.Builder.createBuildStartedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1", "60000")
                                .withDate("2013-01-01")
                                .withTime("10:01:00")
                                .build());
        events.add(Event.Builder.createBuildFinishedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1", "60000")
                                .withDate("2013-01-01")
                                .withTime("10:02:00")
                                .build());

        events.add(Event.Builder.createDebugStartedEvent("user1@gmail.com", "ws1", "project1", "jar", "id2", "60000", "128")
                                .withDate("2013-01-01")
                                .withTime("10:07:00")
                                .build());
        events.add(Event.Builder.createDebugFinishedEvent("user1@gmail.com", "ws1", "project1", "jar", "id2", "60000", "128")
                                .withDate("2013-01-01")
                                .withTime("10:08:00")
                                .build());

        events.add(Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project1", "jar", "id3","60000", "128")
                                .withDate("2013-01-01")
                                .withTime("10:09:00")
                                .build());
        events.add(Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project1", "jar", "id3","60000", "128")
                                .withDate("2013-01-01")
                                .withTime("10:10:00")
                                .build());

        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws1", "project1", "jar", "paas")
                                .withDate("2013-01-01")
                                .withTime("10:11:00")
                                .build());

        // added event of build project without "project-created" event within the log
        events.add(Event.Builder.createBuildStartedEvent("user1@gmail.com", "ws1", "project2", "spring", "id4", "60000")
                                .withDate("2013-01-01")
                                .withTime("10:14:00")
                                .build());
        events.add(Event.Builder.createBuildFinishedEvent("user1@gmail.com", "ws1", "project2", "spring", "id4", "60000")
                                .withDate("2013-01-01")
                                .withTime("10:15:00")
                                .build());
        
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());


        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS_STATISTICS, MetricType.PROJECTS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS_STATISTICS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.PROJECTS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        doIntegrity("20130101");
    }

    @Test
    public void testTestProjectsStatisticsListMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        
        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS_STATISTICS);
        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), 2);
        
        metric = MetricFactory.getMetric(MetricType.CREATED_PROJECTS);
        value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), 1);
        
        
        metric = MetricFactory.getMetric(MetricType.PROJECTS_STATISTICS_LIST);
        List<ValueData> items = ((ListValueData)metric.getValue(builder.build())).getAll();

        assertEquals(2, items.size());

        Map<String, ValueData> m = ((MapValueData)items.get(0)).getAll();
        assertEquals(m.get("project").getAsString(), "project2");
        assertEquals(m.get("ws").getAsString(), WID1);
        assertEquals(m.get("project_type").getAsString(), "spring");
        assertEquals(m.get("date"), LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:14:00").getTime()));
        assertEquals(m.get("user").getAsString(), UID1);
        assertEquals(m.get("builds").getAsString(), "1");
        assertEquals(m.get("runs").getAsString(), "0");
        assertEquals(m.get("debugs").getAsString(), "0");
        assertEquals(m.get("deploys").getAsString(), "0");
        assertEquals(m.get("project_creates").getAsString(), "0");
        assertEquals(m.get("project_destroys").getAsString(), "0");
        assertEquals(m.get("run_time").getAsString(), "0");
        assertEquals(m.get("build_time").getAsString(), "60000");
        assertEquals(m.get("debug_time").getAsString(), "0");
        

        m = ((MapValueData)items.get(1)).getAll();
        assertEquals(m.get("project").getAsString(), "project1");
        assertEquals(m.get("ws").getAsString(), WID1);
        assertEquals(m.get("project_type").getAsString(), "jar");
        assertEquals(m.get("date"), LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:00").getTime()));
        assertEquals(m.get("user").getAsString(), UID1);
        assertEquals(m.get("builds").getAsString(), "1");
        assertEquals(m.get("runs").getAsString(), "1");
        assertEquals(m.get("debugs").getAsString(), "1");
        assertEquals(m.get("deploys").getAsString(), "1");
        assertEquals(m.get("project_creates").getAsString(), "1");
        assertEquals(m.get("project_destroys").getAsString(), "1");
        assertEquals(m.get("run_time").getAsString(), "60000");
        assertEquals(m.get("build_time").getAsString(), "60000");
        assertEquals(m.get("debug_time").getAsString(), "60000");
    }

    @Test
    public void testTestProjectsStatisticsListMetricFilterByUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.PROJECT, "project1");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS_STATISTICS_LIST);
        List<ValueData> items = ((ListValueData)metric.getValue(builder.build())).getAll();

        assertEquals(1, items.size());

        Map<String, ValueData> m = ((MapValueData)items.get(0)).getAll();
        assertEquals(m.get("project").getAsString(), "project1");
        assertEquals(m.get("ws").getAsString(), WID1);
        assertEquals(m.get("project_type").getAsString(), "jar");
        assertEquals(m.get("date"), LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:00").getTime()));
        assertEquals(m.get("user").getAsString(), UID1);

        assertEquals(m.get("builds").getAsString(), "1");
        assertEquals(m.get("runs").getAsString(), "1");
        assertEquals(m.get("debugs").getAsString(), "1");
        assertEquals(m.get("deploys").getAsString(), "1");
        assertEquals(m.get("project_creates").getAsString(), "1");
        assertEquals(m.get("project_destroys").getAsString(), "1");
        assertEquals(m.get("run_time").getAsString(), "60000");
        assertEquals(m.get("build_time").getAsString(), "60000");
        assertEquals(m.get("debug_time").getAsString(), "60000");
    }
    
    @Test
    public void testTestSummaryOfProjectsStatisticsListMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS_STATISTICS_LIST);
        
        ListValueData summaryValue = (ListValueData)((Summaraziable)metric).getSummaryValue(Context.EMPTY);
        MyAsserts.assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> m = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(m.get("builds").getAsString(), "2");
        assertEquals(m.get("runs").getAsString(), "1");
        assertEquals(m.get("debugs").getAsString(), "1");
        assertEquals(m.get("deploys").getAsString(), "1");
        assertEquals(m.get("run_time").getAsString(), "60000");
        assertEquals(m.get("build_time").getAsString(), "120000");
        assertEquals(m.get("debug_time").getAsString(), "60000");
        assertEquals(m.get("project"), null);
        assertEquals(m.get("project_type"), null);
        assertEquals(m.get("date"), null);
        assertEquals(m.get("user"), null);
        assertEquals(m.get("ws"), null);
        assertEquals(m.get("project_id"), null);
    }
}
