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
import com.codenvy.analytics.datamodel.StringValueData;
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

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Alexander Reshetnyak */
public class TestProjectsList extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        addRegisteredUser(UID1, "user1@gmail.com");
        addRegisteredUser(UID2, "user2@gmail.com");
        addRegisteredUser(UID3, "user3@gmail.com");
        addPersistentWs(WID1, "ws1");
        addPersistentWs(WID2, "ws2");
        addPersistentWs(WID3, "ws3");

        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project1", "jar")
                                .withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws2", "project2", "war")
                                .withDate("2013-01-01", "10:00:01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user3@gmail.com", "ws3", "project3", "war")
                                .withDate("2013-01-01", "10:00:02").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.PROJECTS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());
    }

    @Test
    public void testTestUsersProjectsListMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS_LIST);
        ListValueData items = getAsList(metric, builder.build());

        Map<String, Map<String, ValueData>> m = listToMap(items, "project");
        assertEquals(m.size(), 3);
        assertTrue(m.containsKey("project1"));
        assertTrue(m.containsKey("project2"));
        assertTrue(m.containsKey("project3"));

        Map<String, ValueData> p = m.get("project1");
        assertEquals(StringValueData.valueOf("jar"), p.get("project_type"));
        assertEquals(StringValueData.valueOf(WID1), p.get("ws"));
        assertEquals(StringValueData.valueOf(UID1), p.get("user"));
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:00").getTime()), p.get("date"));

        p = m.get("project2");
        assertEquals(StringValueData.valueOf("war"), p.get("project_type"));
        assertEquals(StringValueData.valueOf(WID2), p.get("ws"));
        assertEquals(StringValueData.valueOf(UID1), p.get("user"));
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:01").getTime()), p.get("date"));

        p = m.get("project3");
        assertEquals(StringValueData.valueOf("war"), p.get("project_type"));
        assertEquals(StringValueData.valueOf(WID3), p.get("ws"));
        assertEquals(StringValueData.valueOf(UID3), p.get("user"));
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:02").getTime()), p.get("date"));
    }

    @Test
    public void testTestUsersProjectsListMetricFilterByUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECTS_LIST);
        ListValueData items = getAsList(metric, builder.build());

        Map<String, Map<String, ValueData>> m = listToMap(items, "project");
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("project1"));
        assertTrue(m.containsKey("project2"));

        Map<String, ValueData> p = m.get("project1");
        assertEquals(StringValueData.valueOf("jar"), p.get("project_type"));
        assertEquals(StringValueData.valueOf(WID1), p.get("ws"));
        assertEquals(StringValueData.valueOf(UID1), p.get("user"));
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:00").getTime()), p.get("date"));

        p = m.get("project2");
        assertEquals(StringValueData.valueOf("war"), p.get("project_type"));
        assertEquals(StringValueData.valueOf(WID2), p.get("ws"));
        assertEquals(StringValueData.valueOf(UID1), p.get("user"));
        assertEquals(LongValueData.valueOf(fullDateFormat.parse("2013-01-01 10:00:01").getTime()), p.get("date"));
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
