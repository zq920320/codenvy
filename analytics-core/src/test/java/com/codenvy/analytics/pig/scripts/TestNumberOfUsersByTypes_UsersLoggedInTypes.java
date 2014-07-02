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
public class TestNumberOfUsersByTypes_UsersLoggedInTypes extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "google")
                                .withDate("2013-01-02")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-01-02")
                                .withTime("10:00:01")
                                .build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "jaas")
                                .withDate("2013-01-02")
                                .withTime("10:00:02")
                                .build());
        File log = LogGenerator.generateLog(events);


        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_LOGGED_IN_TYPES).getParamsAsMap());
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    @Test
    public void testDatePeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_LOGGED_IN_WITH_GOOGLE);
        assertEquals(metric.getValue(builder.build()).getAsString(), "2");
    }

    @Test
    public void testDatePeriodUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_LOGGED_IN_WITH_GOOGLE);
        assertEquals(metric.getValue(builder.build()).getAsString(), "1");
    }

    @Test
    public void testWrongDatePeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_LOGGED_IN_WITH_GOOGLE);
        assertEquals(metric.getValue(builder.build()).getAsString(), "0");
    }

    @Test
    public void testComplexFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com OR user1@yahoo.com");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_LOGGED_IN_WITH_GOOGLE);
        assertEquals(metric.getValue(builder.build()).getAsString(), "1");
    }
}
