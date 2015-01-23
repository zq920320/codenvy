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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.metrics.integrity.CreatedUsersIntegrity;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestTotalUserWithCompanyFilter extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("id1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserUpdateProfile("id1", "user1@gmail.com", "user1@gmail.com", "f2", "l2", "BYU-Idaho", "11", "1")
                                .withDate("2013-01-01").withTime("10:10:00,000").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "TEST_WS", "user2@gmail.com")
                                .withDate("2013-01-01").withTime("10:10:00").build());

        // This use case from issue DASHB-494 where is not created-user event for user.
        events.add(Event.Builder.createUserCreatedEvent("id2", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserUpdateProfile("id2", "user2@gmail.com", "user2@gmail.com", "f2", "l2", "eXo", "11", "1")
                                .withDate("2013-01-01").withTime("10:10:00,000").build());

        // add user2 activity
        events.add(Event.Builder.createUserAddedToWsEvent("id2", "TEST_WS", "website")
                                .withDate("2013-01-01").withTime("10:20:00").build());

        events.add(Event.Builder.createUserCreatedEvent("id3", "user3@gmail.com", "user3@gmail.com")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserUpdateProfile("id3", "user3@gmail.com", "user3@gmail.com", "f2", "l2", "eXoPlatform", "11", "1")
                                .withDate("2013-01-01").withTime("10:15:00,000").build());

        File log = LogGenerator.generateLog(events);
        computeStatistics(log, "20130101");

        CreatedUsersIntegrity
                createdUsersUpdater = new CreatedUsersIntegrity(collectionsManagement);
        createdUsersUpdater.doCompute();
    }

    private void computeStatistics(File log, String date) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());
    }

    @Test
    public void testFilterCompany1() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER_COMPANY, "BYU-");

        Metric metric = MetricFactory.getMetric(MetricType.TOTAL_USERS);

        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(value.getAsLong(), 1);
    }

    @Test
    public void testFilterCompany2() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER_COMPANY, "byU-");

        Metric metric = MetricFactory.getMetric(MetricType.TOTAL_USERS);

        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(value.getAsLong(), 1);
    }

    @Test
    public void testFilterCompany3() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER_COMPANY, "BYU+");

        Metric metric = MetricFactory.getMetric(MetricType.TOTAL_USERS);

        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(value.getAsLong(), 0);
    }

    @Test
    public void testFilterCompany4() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER_COMPANY, "exo");

        Metric metric = MetricFactory.getMetric(MetricType.TOTAL_USERS);

        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(value.getAsLong(), 2);
    }

    @Test
    public void testFilterCompany5() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER_COMPANY, "eXoPlatform");

        Metric metric = MetricFactory.getMetric(MetricType.TOTAL_USERS);

        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(value.getAsLong(), 1);
    }

    @Test
    public void testFilterCompany6() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER_COMPANY, "XoP");

        Metric metric = MetricFactory.getMetric(MetricType.TOTAL_USERS);

        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(value.getAsLong(), 1);
    }
    @Test
    public void testFilterCompany7() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER_COMPANY, "eXoPi");

        Metric metric = MetricFactory.getMetric(MetricType.TOTAL_USERS);

        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(value.getAsLong(), 0);
    }
}
