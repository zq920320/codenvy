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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestTimeSpentInAction extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent("uid3", "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-01-01").withTime("19:00:00").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws2", "wsid2", "user2@gmail.com")
                                .withDate("2013-01-01").withTime("19:00:00").build());

        // user1@gmail.com 6m session
        events.add(Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project", "type", "").withDate(
                "2013-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project", "type", "").withDate(
                "2013-01-01").withTime("19:06:00").build());

        // user2@gmail.com 2m session
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type", "id1").withDate(
                "2013-01-01").withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type", "id1").withDate(
                "2013-01-01").withTime("19:10:00").build());

        // user1@gmail.com 1m session
        events.add(Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project", "type", "id2").withDate(
                "2013-01-01").withTime("19:11:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project", "type", "id2").withDate(
                "2013-01-01").withTime("19:12:00").build());

        // corrupted session events, 'run-started' event is absent
        events.add(Event.Builder.createRunFinishedEvent("user4@gmail.com", "ws1", "project", "type", "").withDate(
                "2013-01-01").withTime("19:13:00").build());

        // corrupted session events, 'run-finished' event is absent
        events.add(Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project", "type", "").withDate(
                "2013-01-01").withTime("19:07:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.TIME_SPENT_IN_ACTION, MetricType.RUNS_TIME).getParamsAsMap());
        pigServer.execute(ScriptType.TIME_SPENT_IN_ACTION, builder.build());
    }

    @Test
    public void testDateFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.REGISTERED_USER, 1);

        Metric metric = MetricFactory.getMetric(MetricType.RUNS_TIME);
        Assert.assertEquals(metric.getValue(builder.build()), new LongValueData(540000));
    }

    @Test
    public void testWrongDateFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = MetricFactory.getMetric(MetricType.RUNS_TIME);
        Assert.assertEquals(metric.getValue(builder.build()), new LongValueData(0));
    }


    @Test
    public void testSingleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "uid1");

        Metric metric = MetricFactory.getMetric(MetricType.RUNS_TIME);
        Assert.assertEquals(metric.getValue(builder.build()), new LongValueData(420000));
    }

    @Test
    public void testDoubleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "uid1 OR uid2");

        Metric metric = MetricFactory.getMetric(MetricType.RUNS_TIME);
        Assert.assertEquals(metric.getValue(builder.build()), new LongValueData(540000));
    }

    @Test
    public void testSeveralFilters() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "uid1 OR uid2");
        builder.put(Parameters.WS, "wsid2");

        Metric metric = MetricFactory.getMetric(MetricType.RUNS_TIME);
        Assert.assertEquals(metric.getValue(builder.build()), new LongValueData(120000));

    }
}


