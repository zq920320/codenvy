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

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.projects.*;
import com.codenvy.analytics.metrics.sessions.ProductUsageTimeBelow1Min;
import com.codenvy.analytics.metrics.sessions.ProductUsageTimeTotal;
import com.codenvy.analytics.metrics.sessions.factory.AbstractFactorySessions;
import com.codenvy.analytics.metrics.sessions.factory.FactorySessionsBelow10Min;
import com.codenvy.analytics.metrics.sessions.factory.FactorySessionsWithBuildPercent;
import com.codenvy.analytics.metrics.sessions.factory.ProductUsageFactorySessionsList;
import com.codenvy.analytics.metrics.users.*;
import com.codenvy.analytics.metrics.workspaces.ActiveWorkspaces;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.view.SectionData;
import com.codenvy.analytics.services.view.ViewData;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestExpandedMetric extends AbstractTestExpandedMetric {

    private static final String TEST_WS    = "ws1";
    private static final String TEST_WS_ID = "wsid1";
    private static final String TEST_USER  = "user1_@gmail.com";
    private static final String SESSION_ID = "session_id";
    private static final String TEST_COMPANY = "comp";

    @BeforeClass
    public void prepareDatabase() throws Exception {
        List<Event> events = new ArrayList<>();

        // add user activity at previous day
        /*events.add(Event.Builder.createUserCreatedEvent(TEST_USER, TEST_USER, TEST_USER)
                                .withDate("2013-10-31").withTime("08:00:00").build());*/
        events.add(Event.Builder.createUserAddedToWsEvent("user5@gmail.com", TEST_WS, "", "", "", "website")
                                .withDate("2013-10-31").withTime("08:00:00").build());

        events.add(Event.Builder.createUserCreatedEvent(TEST_USER, TEST_USER, TEST_USER)
                                .withDate("2013-11-01").withTime("08:00:00").build());

        // set user company
        events.add(Event.Builder.createUserCreatedEvent("user1@gmail.com", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("08:40:00").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent(TEST_WS, TEST_WS_ID, TEST_USER)
                                .withDate("2013-11-01").withTime("08:41:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws2", "wsid2", TEST_USER)
                                .withDate("2013-11-01").withTime("08:41:00").build());

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

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws3", "wsid3", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("08:52:00").build());

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
        events.add(Event.Builder.createUserCreatedEvent("user-id5", "user5@gmail.com", "user5@gmail.com")
                                .withDate("2013-11-01").withTime("09:05:00").build());

        // create factory session events
        events.add(
            Event.Builder.createSessionUsageEvent("user1@gmail.com", "tmp-1", "factory-id1", "2013-11-01 10:00:00", "2013-11-01 10:05:00", true)
                         .withDate("2013-11-01").withTime("10:00:00").build());

        events.add(
            Event.Builder.createSessionUsageEvent("user1@gmail.com", "tmp-2", "factory-id2", "2013-11-01 10:20:00", "2013-11-01 10:30:00", true)
                         .withDate("2013-11-01").withTime("10:20:00").build());

        events.add(
            Event.Builder.createSessionUsageEvent("anonymoususer_1", "tmp-3", "factory-id3", "2013-11-01 11:00:00", "2013-11-01 11:15:00", true)
                         .withDate("2013-11-01").withTime("11:00:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1@gmail.com", "project", "type")
                                .withDate("2013-11-01").withTime("10:05:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "http://referrer1", "org1", "affiliate1")
                                .withDate("2013-11-01").withTime("11:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "http://referrer2", "org2", "affiliate1")
                                .withDate("2013-11-01").withTime("11:00:01").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "http://referrer3", "org3", "affiliate2")
                                .withDate("2013-11-01").withTime("11:00:02").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("12:01:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-3", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("12:02:00").build());

        // build event for session #1
        events.add(Event.Builder.createBuildStartedEvent("user1@gmail.com", "tmp-1", "project", "type", "id1")
                                .withDate("2013-11-01").withTime("10:03:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "tmp-1", "", "project", "type")
                                .withDate("2013-11-01").withTime("10:03:00").build());


        // same user invites twice
        events.add(Event.Builder.createUserInviteEvent(TEST_USER, TEST_WS, "_invite" + TEST_USER )
                                .withDate("2013-11-01").withTime("15:00:00,155").build());
        events.add(Event.Builder.createUserInviteEvent(TEST_USER, TEST_WS, "_invite" + TEST_USER)
                                .withDate("2013-11-01").withTime("16:00:00,155").build());
        // add user to workspace by accepting invite
        events.add(Event.Builder.createUserAddedToWsEvent("_invite" + TEST_USER, TEST_WS, "", "", "", "invite")
                                .withDate("2013-11-01").withTime("16:01:03").build());


        // login users
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER, "jaas")
                                .withDate("2013-11-01").withTime("18:55:00,155").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-11-01").withTime("19:55:00,155").build());

        // start main session
        events.add(
            Event.Builder
                .createSessionUsageEvent(TEST_USER, TEST_WS, SESSION_ID, "2013-11-01 19:00:00", "2013-11-01 19:55:00", false)
                .withDate("2013-11-01").withTime("19:00:00").build());

        events.add(
            Event.Builder
                .createSessionUsageEvent("user4@gmail.com", TEST_WS, SESSION_ID + "_micro", "2013-11-01 23:00:00", "2013-11-01 23:00:30", false)
                .withDate("2013-11-01").withTime("23:00:00").build());


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
        events.add(Event.Builder.createRunFinishedEvent(TEST_USER, TEST_WS, "project1", "Python", "id1", 120000)
                                .withDate("2013-11-01").withTime("19:10:00,900").build());

        // event of target user in another workspace and in time of main session
        events.add(Event.Builder.createBuildStartedEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:12:00").build());
        events.add(Event.Builder.createProjectBuiltEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:13:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(TEST_USER, "ws2", "project2", "war", "id2", 120000)
                                .withDate("2013-11-01").withTime("19:14:00").build());

        // event of another user in the another workspace and in time of main session
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws3", "project2", "java", "id3")
                                .withDate("2013-11-01").withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws3", "project2", "java", "id3", 120000)
                                .withDate("2013-11-01").withTime("19:10:00").build());

        // add user6@gmail.com activity (6 sessions && (120min < time < 300min)) for test
        // testAbstractTimelineProductUsageConditionMetric
        events.add(
            Event.Builder
                .createSessionUsageEvent("user6@gmail.com", TEST_WS, "user6@gmail.com1", "2013-11-20 01:00:00", "2013-11-20 03:01:00", false)
                .withDate("2013-11-20").withTime("01:00:00").build());


        events.add(
            Event.Builder
                .createSessionUsageEvent("user6@gmail.com", TEST_WS, "user6@gmail.com2", "2013-11-20 04:00:00", "2013-11-20 04:01:00", false)
                .withDate("2013-11-20").withTime("04:00:00").build());

        events.add(
            Event.Builder
                .createSessionUsageEvent("user6@gmail.com", TEST_WS, "user6@gmail.com3", "2013-11-20 05:00:00", "2013-11-20 05:01:00", false)
                .withDate("2013-11-20").withTime("05:00:00").build());


        events.add(
            Event.Builder
                .createSessionUsageEvent("user6@gmail.com", TEST_WS, "user6@gmail.com4", "2013-11-20 06:00:00", "2013-11-20 06:01:00", false)
                .withDate("2013-11-20").withTime("06:00:00").build());

        events.add(
            Event.Builder
                .createSessionUsageEvent("user6@gmail.com", TEST_WS, "user6@gmail.com5", "2013-11-20 07:00:00", "2013-11-20 07:01:00", false)
                .withDate("2013-11-20").withTime("07:00:00").build());

        events.add(
            Event.Builder
                .createSessionUsageEvent("user6@gmail.com", TEST_WS, "user6@gmail.com6", "2013-11-20 08:00:00", "2013-11-20 08:01:00", false)
                .withDate("2013-11-20").withTime("08:00:00").build());


        // add user7@gmail.com activity (6 sessions, time > 300 min) for test
        // testAbstractTimelineProductUsageConditionMetric
        events.add(
            Event.Builder
                .createSessionUsageEvent("user7@gmail.com", TEST_WS, "user7@gmail.com1", "2013-12-20 01:00:00", "2013-12-20 03:15:00", false)
                .withDate("2013-12-20").withTime("01:00:00").build());


        events.add(
            Event.Builder
                .createSessionUsageEvent("user7@gmail.com", TEST_WS, "user7@gmail.com2", "2013-12-20 04:00:00", "2013-12-20 06:15:00", false)
                .withDate("2013-12-20").withTime("04:00:00").build());


        events.add(
            Event.Builder
                .createSessionUsageEvent("user7@gmail.com", TEST_WS, "user7@gmail.com3", "2013-12-20 07:00:00", "2013-12-20 09:15:00", false)
                .withDate("2013-12-20").withTime("07:00:00").build());


        events.add(
            Event.Builder
                .createSessionUsageEvent("user7@gmail.com", TEST_WS, "user7@gmail.com4", "2013-12-20 10:00:00", "2013-12-20 13:15:00", false)
                .withDate("2013-12-20").withTime("10:00:00").build());

        events.add(
            Event.Builder
                .createSessionUsageEvent("user7@gmail.com", TEST_WS, "user7@gmail.com5", "2013-12-20 14:00:00", "2013-12-20 16:15:00", false)
                .withDate("2013-12-20").withTime("14:00:00").build());

        events.add(
            Event.Builder
                .createSessionUsageEvent("user7@gmail.com", TEST_WS, "user7@gmail.com6", "2013-12-20 17:00:00", "2013-12-20 19:15:00", false)
                .withDate("2013-12-20").withTime("17:00:00").build());

        // add event of accepting factory url for the testDrillDownTopFactoriesMetric test
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-5", "factoryUrl1", "http://referrer3", "org3", "affiliate2")
                                .withDate("2013-12-20").withTime("11:00:02").build());

        events.add(Event.Builder.createUserCreatedEvent("factory_user5", "factory_user5@gmail.com", "factory_user5@gmail.com")
                                .withDate("2013-12-20").withTime("11:00:03").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-5", "factory_user5")
                                .withDate("2013-12-20").withTime("12:01:00").build());

        log = LogGenerator.generateLog(events);
    }

    //@Test
    public void testFilteringUsersStatisticsListByTotalUsersAndTimeUnit() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        // test filtering user list by "total_users" metric
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131120");
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.TIME_INTERVAL, 0);  // interval from 20131208 to 20131214  
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TOTAL_USERS.toString());

        Metric usersStatisticsListMetric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        Context context = builder.build();
        context = viewBuilder.initializeTimeInterval(context);

        ListValueData filteredValue = (ListValueData)usersStatisticsListMetric.getValue(context);
        List<ValueData> all = filteredValue.getAll();
        assertEquals(all.size(), 5);
    }

    @Test
    public void testFilteringUsersStatisticsListByTotalUsersTimeUnitAndDateRange() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131025");
        builder.put(Parameters.TO_DATE, "20131031");  // !!!
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.TIME_INTERVAL, 0);
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TOTAL_USERS.toString());
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");  // !!!

        Metric usersStatisticsListMetric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        Context context = builder.build();
        context = viewBuilder.initializeTimeInterval(context);

        ListValueData filteredValue = (ListValueData)usersStatisticsListMetric.getValue(context);
        List<ValueData> all = filteredValue.getAll();
        assertEquals(all.size(), 0);  // !!!


        builder.put(Parameters.TO_DATE, "20131101");  // !!!
        context = builder.build();
        context = viewBuilder.initializeTimeInterval(context);

        filteredValue = (ListValueData)usersStatisticsListMetric.getValue(context);
        all = filteredValue.getAll();
        assertEquals(all.size(), 5);  // !!!
    }

    //@Test
    public void testFilteringOfDrillDownPage() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILDS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        // test expanded metric value
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        // filter users who built: {user1@gmail.com, user1}
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.USERS_WHO_BUILT.toString());

        // filter users by USER_COMPANY=TEST_COMPANY : {user1@gmail.com, user2@gmail.com}
        builder.put(MetricFilter.USER_COMPANY, TEST_COMPANY);

        // result = {user1@gmail.com, user1} INTERSECT {user1@gmail.com, user2@gmail.com} = {user1@gmail.com}
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData filteredValue = (ListValueData)metric.getValue(builder.build());
        List<ValueData> all = filteredValue.getAll();

        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get("user").getAsString(), TEST_USER);
    }

    /*
     * Testing metric: WorkspacesStatisticsList
     * Filtered by workspaces list from expanded_metric_name=temporary_workspaces_created
     * passed_days_count=by_7_days|by_60_days
     * to_date=20131101
     * factory=factoryUrl1
     */
    //@Test
    public void testDrillDownTopFactoriesMetric() throws Exception {
        computeProfiles("20131101");
        computeProfiles("20131220");

        Context.Builder builder = new Context.Builder();

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_TEMPORARY_WORKSPACES, MetricType.TEMPORARY_WORKSPACES_CREATED).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());


        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_TEMPORARY_WORKSPACES, MetricType.TEMPORARY_WORKSPACES_CREATED).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TEMPORARY_WORKSPACES_CREATED.toString());
        builder.put(MetricFilter.FACTORY, "factoryUrl1");

        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_7_DAYS.toString());
        Context context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);

        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_STATISTICS_LIST);

        // test drill down page values
        ValueData value = metric.getValue(context);
        List<ValueData> all = treatAsList(value);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get("ws").toString(), "tmp-5");

        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_60_DAYS.toString());
        context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);

        value = metric.getValue(context);
        all = treatAsList(value);

        assertEquals(all.size(), 3);

        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.get("ws").toString(), "tmp-4");
    }

    /**
     * Testing metric: ProductUsageSessionsList
     * filtered by user list from expanded_metric_name=product_usage_sessions
     * passed_days_count=by_1_days
     * to_date=20131221|20131220
     * user=factory_user5
     * @throws Exception
     */
    //@Test
    public void testDrillDownTopUsersMetric() throws Exception {
        computeProfiles("20131031");
        computeProfiles("20131101");
        computeProfiles("20131220");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());


        builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.PRODUCT_USAGE_SESSIONS.toString());
        builder.put(MetricFilter.USER, "factory_user5");

        builder.put(Parameters.TO_DATE, "20131221");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_1_DAY.toString());
        Context context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        // test drill down page values
        ValueData value = metric.getValue(context);
        List<ValueData> all = treatAsList(value);
        assertEquals(all.size(), 0);

        builder.put(Parameters.TO_DATE, "20131220");
        context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);

        value = metric.getValue(context);
        all = treatAsList(value);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get("user").toString(), "factory_user5");
    }

    //@Test
    public void testNonActiveUsersMetric() throws Exception {
        computeProfiles("20131031");
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        pigServer.execute(ScriptType.EVENTS, builder.build());

        // test expanded metric value
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Expandable metric = new NonActiveUsers();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 0);

        metric = new CreatedUsers();
        expandedValue = metric.getExpandedValue(builder.build());
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 5);
        assertTrue(all.contains(MapValueData.valueOf("user=user-id4")));
        assertTrue(all.contains(MapValueData.valueOf("user=user-id5")));

        // test filtering user list by "non_active_users" metric
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        UsersStatisticsList usersStatisticsListMetric = new UsersStatisticsList();
        ListValueData value = (ListValueData)usersStatisticsListMetric.getValue(builder.build());
        all = value.getAll();
        assertEquals(all.size(), 5);

        // calculate non-active user list
        builder.put(Parameters.EXPANDED_METRIC_NAME, "non_active_users");

        ListValueData filteredValue = (ListValueData)usersStatisticsListMetric.getValue(builder.build());
        all = filteredValue.getAll();
        assertEquals(all.size(), 0);
    }

    //@Test
    public void testUsersAcceptedInvitesPercentMetric() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_ADDED_TO_WORKSPACES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        UsersAcceptedInvitesPercent metric = new UsersAcceptedInvitesPercent();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "_invite" + TEST_USER);
    }

    //@Test
    public void testAbstractFactorySessionsMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
            scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        AbstractFactorySessions metric = new FactorySessionsBelow10Min();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 4);

        Map<String, ValueData> record = ((MapValueData)all.get(3)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("session_id").toString(), "factory-id1");
    }

    //@Test
    public void testProductUsageTimeBelow1MinMetric() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageTimeBelow1Min metric = new ProductUsageTimeBelow1Min();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 4);
        assertTrue(all.contains(MapValueData.valueOf("session_id=" + SESSION_ID + "_micro")));
    }

    //@Test
    public void testProductUsageTimeTotalMetric() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageTimeTotal metric = new ProductUsageTimeTotal();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 5);

        assertTrue(all.contains(MapValueData.valueOf("session_id=" + SESSION_ID)));
        assertTrue(all.contains(MapValueData.valueOf("session_id=" + SESSION_ID + "_micro")));
    }

    //@Test
    public void testAbstractLoggedInTypeMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_LOGGED_IN_TYPES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        AbstractLoggedInType metric = new UsersLoggedInWithForm();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get(metric.getExpandedField()).toString(), TEST_USER);
    }

    //@Test
    public void testCalculatedSubtractionMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_USERS_FROM_FACTORY, MetricType.CREATED_USERS_FROM_FACTORY).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_USERS_FROM_FACTORY, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        Expandable createdUsersMetric = new CreatedUsers();

        // test expanded metric value
        ValueData expandedValue = createdUsersMetric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 5);

        CalculatedMetric createdUsersFromAuthMetric = new CreatedUsersFromAuth();

        // test expanded metric value
        expandedValue = ((Expandable)createdUsersFromAuthMetric).getExpandedValue(builder.build());
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 4);
        assertTrue(all.contains(MapValueData.valueOf("user=user-id5")));
    }

    //@Test
    public void testCalculatedPercentMetric() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
            scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        Expandable metric = new FactorySessionsWithBuildPercent();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> workspace1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(workspace1.size(), 1);
        assertEquals(workspace1.get("session_id").toString(), "factory-id1");
    }

    //@Test
    public void testSessionsListFilteredByCalculatedMetric() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
            scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        Expandable metric = new FactorySessionsWithBuildPercent();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> workspace1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(workspace1.size(), 1);
        assertEquals(workspace1.get("session_id").toString(), "factory-id1");

        // filter factory sessions by "factory_sessions_with_build_percent" metric
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131102");

        ProductUsageFactorySessionsList sessionsListMetric = new ProductUsageFactorySessionsList();

        ListValueData value = (ListValueData)sessionsListMetric.getValue(builder.build());
        all = value.getAll();
        assertEquals(all.size(), 6);

        // calculate build projects list
        builder.put(Parameters.EXPANDED_METRIC_NAME, "factory_sessions_with_build_percent");

        ListValueData filteredValue = (ListValueData)sessionsListMetric.getValue(builder.build());
        all = filteredValue.getAll();
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get(ProductUsageFactorySessionsList.SESSION_ID).toString(), "factory-id1");
    }

    //@Test
    public void testExpandedAbstractActiveEntitiesMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, TEST_USER);

        AbstractActiveEntities metric = new ActiveWorkspaces();

        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), 3);

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);
        assertTrue(all.contains(MapValueData.valueOf("ws=wsid1")));
        assertTrue(all.contains(MapValueData.valueOf("ws=wsid2")));
        assertTrue(all.contains(MapValueData.valueOf("ws=wsid3")));

        // test expanded metric value pagination
        builder.put(Parameters.PAGE, 2);
        builder.put(Parameters.PER_PAGE, 1);
        builder.put(Parameters.SORT, "+ws");

        expandedValue = metric.getExpandedValue(builder.build());

        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);
        assertTrue(all.contains(MapValueData.valueOf("ws=wsid2")));
    }

    //@Test
    public void testExpandedAbstractLongValueResultedMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.USER_INVITE).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        AbstractLongValueResulted metric = new UserInvite();

        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), 2);

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> workspace1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(workspace1.size(), 1);
        assertEquals(workspace1.get(metric.getExpandedField()).toString(), TEST_USER);
    }

    //@Test
    public void testExpandedAbstractCountMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        // calculate projects list
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        // test expanded metric value
        AbstractCount metric = new CreatedProjects();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);

        Map<String, ValueData> record1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(record1.get(metric.getExpandedField()).toString(), TEST_USER + "/" + TEST_WS_ID + "/project1");

        Map<String, ValueData> record2 = ((MapValueData)all.get(1)).getAll();
        assertEquals(record2.get(metric.getExpandedField()).toString(), TEST_USER + "/wsid2/project2");

        Map<String, ValueData> record3 = ((MapValueData)all.get(2)).getAll();
        assertEquals(record3.get(metric.getExpandedField()).toString(), "user2@gmail.com/wsid3/project2");
    }

    //@Test
    public void testExpandedAbstractProjectTypeMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        // calculate projects list
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        // test expanded metric value
        AbstractProjectType metric = new ProjectTypeWar();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 2);

        Map<String, ValueData> record1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(record1.get(metric.getExpandedField()).toString(), TEST_USER + "/wsid2/project2");

        Map<String, ValueData> record2 = ((MapValueData)all.get(1)).getAll();
        assertEquals(record2.get(metric.getExpandedField()).toString(), "user2@gmail.com/wsid3/project2");
    }

    //@Test
    public void testExpandedAbstractProjectPaasMetrics() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        // calculate projects list
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.DEPLOYMENTS_BY_TYPES, MetricType.PROJECT_PAASES).getParamsAsMap());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());

        // test expanded metric value
        AbstractProjectPaas metric = new ProjectPaasGae();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 2);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get(metric.getExpandedField()).toString(), TEST_USER + "/wsid2/project2");

        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.get(metric.getExpandedField()).toString(), TEST_USER + "/" + TEST_WS_ID + "/project1");
    }

    //@Test
    public void testProjectListFilteredByReadBasedMetric() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        // calculate number of runs
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.RUNS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        // calculate projects list
        builder.put(Parameters.STORAGE_TABLE, MetricType.PROJECTS_LIST.toString().toLowerCase());
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        // calculate all projects list
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        ProjectsList projectsListMetric = new ProjectsList();

        ListValueData value = (ListValueData)projectsListMetric.getValue(builder.build());
        assertEquals(value.getAll().size(), 3);

        // calculate run projects list
        builder.put(Parameters.EXPANDED_METRIC_NAME, "runs");

        ListValueData filteredValue = (ListValueData)projectsListMetric.getValue(builder.build());
        List<ValueData> all = filteredValue.getAll();
        assertEquals(all.size(), 2);

        Map<String, ValueData> project1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(project1.get(ProjectsList.PROJECT).toString(), "project2");
        assertEquals(project1.get(ProjectsList.WS).toString(), "wsid3");

        Map<String, ValueData> project2 = ((MapValueData)all.get(1)).getAll();
        assertEquals(project2.get(ProjectsList.PROJECT).toString(), "project1");
        assertEquals(project2.get(ProjectsList.WS).toString(), TEST_WS_ID);
    }

    //@Test
    public void testConversionExpandedValueDataIntoViewData() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, TEST_USER);

        Expandable metric = (Expandable)MetricFactory.getMetric(MetricType.ACTIVE_WORKSPACES);

        ValueData expandedValue = metric.getExpandedValue(builder.build());

        // test view data builded on expanded metric value data
        ViewData viewData = viewBuilder.getViewData(expandedValue);
        assertEquals(viewData.size(), 1);

        SectionData sectionData = viewData.get("section_expended");
        assertEquals(sectionData.size(), 4);

        assertTrue(sectionData.contains(asList(StringValueData.valueOf("ws"))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf("wsid1"))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf("wsid2"))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf("wsid3"))));
    }

    //@Test
    public void testTotalUsers() throws Exception {
        computeProfiles("20131101");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        Expandable metric = (Expandable)MetricFactory.getMetric(MetricType.CREATED_USERS);
        ValueData expandedValue = metric.getExpandedValue(Context.EMPTY);

        List<ValueData> list = treatAsList(expandedValue);
        assertEquals(list.size(), 5);
        assertTrue(list.contains(MapValueData.valueOf("user=user-id5")));
        assertTrue(list.contains(MapValueData.valueOf("user=user-id4")));
    }

    @BeforeMethod
    public void clearDatabase() {
        super.clearDatabase();
    }
}
