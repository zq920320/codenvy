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
public class TestUsersAddedToWorkspaces extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1@gmail.com")
                                .withDate("2013-01-02").withTime("10:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws", "wsid2", "user2@gmail.com")
                                .withDate("2013-01-02").withTime("10:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws2", "wsid3", "user4@gmail.com")
                                .withDate("2013-01-02").withTime("10:00:00").build());

        events.add(Event.Builder.createUserAddedToWsEvent("user1@gmail.com", "ws1", "", "", "", "website")
                                .withDate("2013-01-02").withTime("10:00:00").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user2@gmail.com", "ws", "", "", "", "website")
                                .withDate("2013-01-02").withTime("10:00:01").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user3@gmail.com", "ws", "", "", "", "invite")
                                .withDate("2013-01-02").withTime("10:00:02").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user4@gmail.com", "ws2", "", "", "", "invite")
                                .withDate("2013-01-02").withTime("10:00:03").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user5@gmail.com", "ws2", "", "", "", "invite")
                                .withDate("2013-01-02").withTime("10:00:04").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_ADDED_TO_WORKSPACES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    @Test
    public void testDatePeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_ADDED_TO_WORKSPACES_USING_INVITATION);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(3));
    }

    @Test
    public void testComplexFilterWhenAllParamHasTilda() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "~ user1@gmail.com OR user2@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_ADDED_TO_WORKSPACES_USING_INVITATION);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(3));
    }
}
