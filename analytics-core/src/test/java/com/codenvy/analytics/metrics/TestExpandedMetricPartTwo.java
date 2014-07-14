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

import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.sessions.*;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Reshetnyak
 */
public class TestExpandedMetricPartTwo extends AbstractTestExpandedMetric {

    private static final String TEST_WS      = "ws1";
    private static final String TEST_USER    = "user1@gmail.com";
    private static final String SESSION_ID   = "session_id";
    private static final String TEST_COMPANY = "comp";

    private File log;

    @BeforeClass
    public void prepareDatabase() throws IOException, ParseException {
        List<Event> events = new ArrayList<>();

        // set user company
        events.add(Event.Builder.createUserCreatedEvent(TEST_USER, TEST_USER, TEST_USER)
                                .withDate("2013-11-01").withTime("08:40:00").build());
        events.add(Event.Builder.createUserUpdateProfile(TEST_USER,
                                                         TEST_USER,
                                                         TEST_USER,
                                                         "first name 1",
                                                         "last name 1",
                                                         TEST_COMPANY,
                                                         "555-444-333",
                                                         "adm")
                                .withDate("2013-11-01").withTime("08:50:00").build());

        events.add(Event.Builder.createUserCreatedEvent("user2@gmail.com", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("08:51:00").build());

        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com",
                                                         "user2@gmail.com",
                                                         "user2@gmail.com",
                                                         "first name 2",
                                                         "last name 3",
                                                         TEST_COMPANY,
                                                         "555-444-333",
                                                         "develop")
                                .withDate("2013-11-01").withTime("08:52:00").build());


        // create user from factory
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factoryUrl1", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-01").withTime("09:01:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-4", "anonymoususer_04")
                                .withDate("2013-11-01").withTime("09:01:30").build());
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-4", "anonymoususer_4", "website")
                                .withDate("2013-11-01").withTime("09:02:00").build());
        events.add(Event.Builder.createUserChangedNameEvent("anonymoususer_4", "user4@gmail.com")
                                .withDate("2013-11-01").withTime("09:03:00").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id4", "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-11-01").withTime("09:04:00").build());

        // create user
        events.add(Event.Builder.createUserCreatedEvent("user-id5", "user5", "user5")
                                .withDate("2013-11-01").withTime("09:05:00").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id1", "user1", "user1")
                                .withDate("2013-11-01").withTime("09:05:00").build());

        // create factory session events
        events.add(Event.Builder.createSessionFactoryStartedEvent("factory-id1", "tmp-1", "user1", "true", "brType")
                                .withDate("2013-11-01").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("factory-id1", "tmp-1", "user1")
                                .withDate("2013-11-01").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("factory-id2", "tmp-2", "user1", "true", "brType")
                                .withDate("2013-11-01").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("factory-id2", "tmp-2", "user1")
                                .withDate("2013-11-01").withTime("10:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("factory-id3", "tmp-3", "anonymoususer_1", "false", "brType")
                                .withDate("2013-11-01").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("factory-id3", "tmp-3", "anonymoususer_1")
                                .withDate("2013-11-01").withTime("11:15:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project", "type")
                                .withDate("2013-11-01").withTime("10:05:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "http://referrer1", "org1", "affiliate1")
                                .withDate("2013-11-01").withTime("11:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "http://referrer2", "org2", "affiliate1")
                                .withDate("2013-11-01").withTime("11:00:01").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "http://referrer3", "org3", "affiliate2")
                                .withDate("2013-11-01").withTime("11:00:02").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1")
                                .withDate("2013-11-01").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1")
                                .withDate("2013-11-01").withTime("12:01:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-3", "user1")
                                .withDate("2013-11-01").withTime("12:02:00").build());

        // build event for session #1
        events.add(Event.Builder.createBuildStartedEvent("user1", "tmp-1", "project", "type", "id1")
                                .withDate("2013-11-01").withTime("10:03:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "", "project", "type")
                                .withDate("2013-11-01").withTime("10:03:00").build());


        // same user invites twice
        events.add(Event.Builder.createUserInviteEvent(TEST_USER, TEST_WS, TEST_USER + "_invite")
                                .withDate("2013-11-01").withTime("15:00:00,155").build());
        events.add(Event.Builder.createUserInviteEvent(TEST_USER, TEST_WS, TEST_USER + "_invite")
                                .withDate("2013-11-01").withTime("16:00:00,155").build());
        // add user to workspace by accepting invite
        events.add(Event.Builder.createUserAddedToWsEvent(TEST_USER + "_invite", TEST_WS, "", "", "", "invite")
                                .withDate("2013-11-01").withTime("16:01:03").build());


        // login users
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER, "jaas")
                                .withDate("2013-11-01").withTime("18:55:00,155").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-11-01").withTime("19:55:00,155").build());

        // start main session
        events.add(Event.Builder.createSessionStartedEvent(TEST_USER, TEST_WS, "ide", SESSION_ID)
                                .withDate("2013-11-01").withTime("19:00:00,155").build());

        // create test projects and deploy they into PaaS
        events.add(Event.Builder.createProjectCreatedEvent(TEST_USER, TEST_WS, "id1", "project1", "python")
                                .withDate("2013-11-01").withTime("18:08:00,600").build());
        events.add(Event.Builder.createApplicationCreatedEvent(TEST_USER, TEST_WS, "id1", "project1", "python", "gae")
                                .withDate("2013-11-01").withTime("18:08:10").build());

        events.add(Event.Builder.createProjectCreatedEvent(TEST_USER, "ws2", "id2", "project2", "war")
                                .withDate("2013-11-01").withTime("18:12:00").build());
        events.add(Event.Builder.createApplicationCreatedEvent(TEST_USER, "ws2", "id2", "project2", "war", "gae")
                                .withDate("2013-11-01").withTime("18:12:30").build());

        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws3", "id3", "project2", "java")
                                .withDate("2013-11-01").withTime("18:20:10").build());
        events.add(Event.Builder.createProjectDeployedEvent("user2@gmail.com", "ws3", "id3", "project2", "java", "local")
                                .withDate("2013-11-01").withTime("18:21:30").build());

        // event of target user in the target workspace and in time of first session
        events.add(Event.Builder.createRunStartedEvent(TEST_USER, TEST_WS, "project1", "Python", "id1")
                                .withDate("2013-11-01").withTime("19:08:00,600").build());
        events.add(Event.Builder.createRunFinishedEvent(TEST_USER, TEST_WS, "project1", "Python", "id1")
                                .withDate("2013-11-01").withTime("19:10:00,900").build());

        // event of target user in another workspace and in time of main session
        events.add(Event.Builder.createBuildStartedEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:12:00").build());
        events.add(Event.Builder.createProjectBuiltEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:13:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:14:00").build());

        // event of another user in the another workspace and in time of main session
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws3", "project2", "java", "id3")
                                .withDate("2013-11-01").withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws3", "project2", "java", "id3")
                                .withDate("2013-11-01").withTime("19:10:00").build());

        // finish main session
        events.add(Event.Builder.createSessionFinishedEvent(TEST_USER, TEST_WS, "ide", SESSION_ID)
                                .withDate("2013-11-01").withTime("19:55:00,555").build());

        // make micro-session with duration < than 1 min
        events.add(Event.Builder.createSessionStartedEvent("user4@gmail.com", TEST_WS, "ide", SESSION_ID + "_micro")
                                .withDate("2013-11-01").withTime("23:00:00,155").build());
        // finish main session
        events.add(Event.Builder.createSessionFinishedEvent("user4@gmail.com", TEST_WS, "ide", SESSION_ID + "_micro")
                                .withDate("2013-11-01").withTime("23:00:30,555").build());

        // add user6@gmail.com activity (6 sessions && (120min < time < 300min)) for test
        // testAbstractTimelineProductUsageConditionMetric
        events.add(Event.Builder.createUserCreatedEvent("user-id6", "user6@gmail.com", "user6@gmail.com")
                                .withDate("2013-11-20").withTime("00:05:00").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id7", "user7@gmail.com", "user7@gmail.com")
                                .withDate("2013-11-20").withTime("00:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com1")
                                .withDate("2013-11-20").withTime("01:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com1")
                                .withDate("2013-11-20").withTime("03:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com2")
                                .withDate("2013-11-20").withTime("04:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com2")
                                .withDate("2013-11-20").withTime("04:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com3")
                                .withDate("2013-11-20").withTime("05:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com3")
                                .withDate("2013-11-20").withTime("05:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com4")
                                .withDate("2013-11-20").withTime("06:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com4")
                                .withDate("2013-11-20").withTime("06:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com5")
                                .withDate("2013-11-20").withTime("07:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com5")
                                .withDate("2013-11-20").withTime("07:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com6")
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com6")
                                .withDate("2013-11-20").withTime("08:01:00").build());

        // add user7@gmail.com activity (6 sessions, time > 300 min) for test
        // testAbstractTimelineProductUsageConditionMetric
        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com1")
                                .withDate("2013-12-20").withTime("01:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com1")
                                .withDate("2013-12-20").withTime("03:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com2")
                                .withDate("2013-12-20").withTime("04:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com2")
                                .withDate("2013-12-20").withTime("06:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com3")
                                .withDate("2013-12-20").withTime("07:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com3")
                                .withDate("2013-12-20").withTime("09:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com4")
                                .withDate("2013-12-20").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com4")
                                .withDate("2013-12-20").withTime("13:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com5")
                                .withDate("2013-12-20").withTime("14:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com5")
                                .withDate("2013-12-20").withTime("16:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com6")
                                .withDate("2013-12-20").withTime("17:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com6")
                                .withDate("2013-12-20").withTime("19:15:00").build());

        // add event of accepting factory url for the testDrillDownTopFactoriesMetric test
        events.add(Event.Builder.createUserCreatedEvent("user-id_factory_user5", "factory_user5", "factory_user5")
                                .withDate("2013-12-20").withTime("11:01:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-5", "factoryUrl1", "http://referrer3", "org3", "affiliate2")
                                .withDate("2013-12-20").withTime("11:00:02").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-5", "factory_user5")
                                .withDate("2013-12-20").withTime("12:01:00").build());


        log = LogGenerator.generateLog(events);

        computeProfiles("20131101");
        computeProfiles("20131120");
        computeProfiles("20131220");
    }

    private void computeProfiles(String date) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
                scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());
    }

    private void computeActiveUserSet(String date) throws IOException, ParseException {
        Context.Builder builder;
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
                scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());
    }

    private void computeProductUsageSessions(String date) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void testAbstractTimelineProductUsageConditionMetric() throws Exception {
        computeProductUsageSessions("20131101");
        computeProductUsageSessions("20131120");
        computeProductUsageSessions("20131220");

        /** test TimelineProductUsageConditionBelow120Min */
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "0");
        AbstractTimelineProductUsageCondition metric = new TimelineProductUsageConditionBelow120Min();
        Context context = metric.initContextBasedOnTimeInterval(builder.build());
        ValueData expandedValue = metric.getExpandedValue(context);
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 4);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id_factory_user5");

        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1@gmail.com");

        record = ((MapValueData)all.get(2)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id4");

        record = ((MapValueData)all.get(3)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id1");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "2");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1@gmail.com");
        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id4");
        record = ((MapValueData)all.get(2)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id1");

        /** test TimelineProductUsageConditionBetween120And300Min */
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "0");
        metric = new TimelineProductUsageConditionBetween120And300Min();
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id6");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "1");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id6");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "2");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id6");


        /** test TimelineProductUsageConditionAbove300Min */
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "0");
        metric = new TimelineProductUsageConditionAbove300Min();
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user-id7");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "1");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 0);

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "2");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 0);
    }

    @Test
    public void testProductUsageUsersBelow10MinMetric() throws Exception {
        computeActiveUserSet("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageUsersBelow10Min metric = new ProductUsageUsersBelow10Min();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 5);

        assertTrue(all.contains(MapValueData.valueOf("user=user2@gmail.com")));
        assertTrue(all.contains(MapValueData.valueOf("user=user-id1")));
        assertTrue(all.contains(MapValueData.valueOf("user=user-id5")));
        assertTrue(all.contains(MapValueData.valueOf("user=user-id4")));
        assertTrue(all.contains(MapValueData.valueOf("user=" + TEST_USER + "_invite")));
    }
}