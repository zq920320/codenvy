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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

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
public class TestProjectsList extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "ws2", "user2@gmail.com")
                                .withDate("2013-01-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid3", "ws3", "user3@gmail.com")
                                .withDate("2013-01-01").withTime("08:59:00").build());

        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project1", "jar")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws2", "project2", "war")
                                .withDate("2013-01-01")
                                .withTime("10:00:01")
                                .build());

        events.add(Event.Builder.createProjectCreatedEvent("user2@yahoo.com", "ws3", "project3", "war")
                                .withDate("2013-01-01")
                                .withTime("10:00:03")
                                .build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.PROJECTS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());
    }

    @Test
    public void testTestUsersProjectsListMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS_LIST);
        List<ValueData> items = ((ListValueData)metric.getValue(builder.build())).getAll();

        assertEquals(3, items.size());

        Map<String, ValueData> m = ((MapValueData)items.get(0)).getAll();
        assertEquals("project1", m.get("project").getAsString());
        assertEquals("jar", m.get("project_type").getAsString());
        assertEquals("wsid1", m.get("ws").getAsString());
        assertEquals("user1@gmail.com", m.get("user").getAsString());
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:00").getTime()), m.get("date"));

        m = ((MapValueData)items.get(1)).getAll();
        assertEquals("project2", m.get("project").getAsString());
        assertEquals("war", m.get("project_type").getAsString());
        assertEquals("wsid2", m.get("ws").getAsString());
        assertEquals("user1@gmail.com", m.get("user").getAsString());
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:01").getTime()), m.get("date"));

        m = ((MapValueData)items.get(2)).getAll();
        assertEquals("project3", m.get("project").getAsString());
        assertEquals("war", m.get("project_type").getAsString());
        assertEquals("wsid3", m.get("ws").getAsString());
        assertEquals("user2@yahoo.com", m.get("user").getAsString());
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:03").getTime()), m.get("date"));
    }

    @Test
    public void testTestUsersProjectsListMetricFilterByUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS_LIST);
        List<ValueData> items = ((ListValueData)metric.getValue(builder.build())).getAll();

        assertEquals(2, items.size());

        Map<String, ValueData> m = ((MapValueData)items.get(0)).getAll();
        assertEquals("project1", m.get("project").getAsString());
        assertEquals("jar", m.get("project_type").getAsString());
        assertEquals("wsid1", m.get("ws").getAsString());
        assertEquals("user1@gmail.com", m.get("user").getAsString());
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:00").getTime()), m.get("date"));

        m = ((MapValueData)items.get(1)).getAll();
        assertEquals("project2", m.get("project").getAsString());
        assertEquals("war", m.get("project_type").getAsString());
        assertEquals("wsid2", m.get("ws").getAsString());
        assertEquals("user1@gmail.com", m.get("user").getAsString());
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:01").getTime()), m.get("date"));
    }

    @Test
    public void testTestUsersProjectsMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS);
        LongValueData valueData = (LongValueData)metric.getValue(builder.build());

        assertEquals(valueData.getAsLong(), 3);
    }

    @Test
    public void testTestUsersProjectsMetricFilterByUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS);
        LongValueData valueData = (LongValueData)metric.getValue(builder.build());

        assertEquals(valueData.getAsLong(), 2);
    }
}
