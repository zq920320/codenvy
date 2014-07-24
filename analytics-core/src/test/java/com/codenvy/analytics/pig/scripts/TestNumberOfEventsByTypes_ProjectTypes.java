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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestNumberOfEventsByTypes_ProjectTypes extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1@gmail.com")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws2", "wsid2", "user1@yahoo.com")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());

        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "", "", "jar")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1@yahoo.com", "ws2", "", "", "war")
                                .withDate("2013-01-01")
                                .withTime("10:00:01")
                                .build());
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        /* is needed for testAllTypes() test only */
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.PROJECT_TYPES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());


        events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "", "", "jar")
                                .withDate("2013-01-02")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1@yahoo.com", "ws2", "", "", "war")
                                .withDate("2013-01-02")
                                .withTime("10:00:01")
                                .build());
        log = LogGenerator.generateLog(events);

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());
        
        /* is needed for testAllTypes() test only */
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.PROJECT_TYPES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    @Test
    public void testSingleDateFilterSingleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_JAR);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));
    }

    @Test
    public void testSingleDateFilterDoubleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_WAR);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));
    }


    @Test
    public void testDatePeriodFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_JAR);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_JAR);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testSingleUserFilterShouldReturnZero() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user2@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_JAR);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));
    }

    @Test
    public void testDoubleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com OR user1@yahoo.com");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_JAR);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testComplexFilterSingleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com OR user1@yahoo.com");
        builder.put(Parameters.WS, "wsid1 OR wsid2");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_JAR);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }
}
