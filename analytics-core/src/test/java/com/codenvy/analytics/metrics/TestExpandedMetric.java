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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.view.SectionData;
import com.codenvy.analytics.services.view.ViewData;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.Utils.initDateInterval;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.codenvy.analytics.metrics.MetricFactory.getMetric;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 * @author Anatoliy Bazko
 */
public class TestExpandedMetric extends AbstractTestExpandedMetric {

    @BeforeClass
    public void prepareDatabase() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent(UID1, "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID2, "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID3, "user3@gmail.com", "user3@gmail.com")
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID4, "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID5, "user5@gmail.com", "user5@gmail.com")
                                .withDate("2013-11-20").withTime("08:00:00").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent(TWID1, "tmp-1", AUID1)
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(TWID2, "tmp-2", UID2)
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID1, "ws1", UID1)
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID2, "ws2", UID2)
                                .withDate("2013-11-20").withTime("08:00:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent(TWID1, "factory1", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-20").withTime("07:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent(TWID2, "factory2", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-20").withTime("07:00:00").build());

        events.add(Event.Builder.createUserAddedToWsEvent(AUID1, TWID1, "website").withDate("2013-11-20", "07:10:00").build());
        events.add(Event.Builder.createUserChangedNameEvent(AUID1, UID1).withDate("2013-11-20", "08:10:00").build());

        events.add(Event.Builder.createSessionUsageEvent(AUID1, TWID1, "session-fid1", false)
                                .withDate("2013-11-20").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(AUID1, TWID1, "session-fid1", false)
                                .withDate("2013-11-20").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionUsageEvent(AUID1, TWID1, "session-fid1", true)
                                .withDate("2013-11-20").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(AUID1, TWID1, "session-fid1", true)
                                .withDate("2013-11-20").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionUsageEvent(UID2, TWID2, "session-fid2", false)
                                .withDate("2013-11-20").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(UID2, TWID2, "session-fid2", false)
                                .withDate("2013-11-20").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionUsageEvent(UID2, TWID2, "session-fid2", true)
                                .withDate("2013-11-20").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent(UID2, TWID2, "session-fid2", true)
                                .withDate("2013-11-20").withTime("11:05:00").build());

        events.add(Event.Builder.createBuildStartedEvent(UID1, TWID1, "project", "type", "build-id1")
                                .withDate("2013-11-20").withTime("12:00:00").build());
        events.add(Event.Builder.createBuildStartedEvent(UID2, WID2, "project", "type", "build-id2")
                                .withDate("2013-11-20").withTime("12:00:00").build());

        events.add(Event.Builder.createUserSSOLoggedInEvent(UID1, "jaas").withDate("2013-11-20").withTime("09:00:00").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(UID2, "google").withDate("2013-11-20").withTime("09:00:00").build());

        events.add(Event.Builder.createProjectCreatedEvent(UID2, WID2, "project1", "jar", "gae").withDate("2013-11-20").withTime("09:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent(UID2, WID2, "project2", "war", "gae").withDate("2013-11-20").withTime("09:00:00").build());

        // inside 'session-fid1' session
        events.add(Event.Builder.createBuildStartedEvent(UID2, TWID2, "project", "type", "bid1").withDate("2013-11-20").withTime("11:01:00").build());
        events.add(Event.Builder.createRunStartedEvent(UID2, WID2, "project1", "jar", "rid1").withDate("2013-11-20").withTime("11:01:00").build());

        File log = LogGenerator.generateLog(events);
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131120");
        builder.put(Parameters.TO_DATE, "20131120");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_PROJECTS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.put(Parameters.STORAGE_TABLE, MetricType.PROJECTS_LIST.toString().toLowerCase());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.PROJECTS).getParamsAsMap());

        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILDS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.RUNS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.DEPLOYMENTS_BY_TYPES, MetricType.PROJECT_PAASES).getParamsAsMap());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_TEMPORARY_WORKSPACES, MetricType.TEMPORARY_WORKSPACES_CREATED).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_LOGGED_IN_TYPES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_USERS_FROM_FACTORY, MetricType.CREATED_USERS_FROM_FACTORY).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_USERS_FROM_FACTORY, builder.build());
    }

    @Test
    public void testFilteringUsersStatisticsListByTotalUsersAndTimeUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131120");
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.TIME_INTERVAL, 0);  // the first time interval from 2013-11-17 to 2013-11-23
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TOTAL_USERS.toString());

        Metric metric = getMetric(MetricType.USERS_STATISTICS_LIST);
        Context context = viewBuilder.initializeTimeInterval(builder.build());

        ListValueData l = getAsList(metric, context);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.USER);

        assertEquals(m.size(), 2);
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
    }

    @Test
    public void testFilteringWorkspaceStatisticsListByTotalWorkspacesAndTimeUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131120");
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.TIME_INTERVAL, 0);  // the first time interval from 2013-11-17 to 2013-11-23
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TOTAL_WORKSPACES.toString());

        Metric metric = getMetric(MetricType.WORKSPACES_STATISTICS_LIST);
        Context context = viewBuilder.initializeTimeInterval(builder.build());

        ListValueData l = getAsList(metric, context);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.WS);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(WID2));
    }

    @Test
    public void testFilteringUsersStatisticsListByActiveUsersTimeUnitAndDateRange() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131124");
        builder.put(Parameters.TO_DATE, "20131130");
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.TIME_INTERVAL, 0);
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.ACTIVE_USERS.toString());
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        Metric metric = getMetric(MetricType.USERS_STATISTICS_LIST);
        Context context = viewBuilder.initializeTimeInterval(builder.build());

        ListValueData l = getAsList(metric, context);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.USER);

        assertTrue(m.isEmpty());

        builder.put(Parameters.FROM_DATE, "20131117");
        builder.put(Parameters.TO_DATE, "20131123");
        context = viewBuilder.initializeTimeInterval(builder.build());

        l = getAsList(metric, context);
        m = listToMap(l, AbstractMetric.USER);

        assertEquals(m.size(), 2);
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
    }

    @Test
    public void testFilteringOfDrillDownPage() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131117");
        builder.put(Parameters.TO_DATE, "20131123");
        builder.put(MetricFilter.USER, UID1);
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.USERS_WHO_BUILT.toString());

        Metric metric = getMetric(MetricType.USERS_STATISTICS_LIST);
        Context context = viewBuilder.initializeTimeInterval(builder.build());

        ListValueData l = getAsList(metric, context);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.USER);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testDrillDownTopFactoriesMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TEMPORARY_WORKSPACES_CREATED.toString());
        builder.put(MetricFilter.FACTORY, "factory1");
        builder.put(Parameters.TO_DATE, "20131123");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_LIFETIME.toString());
        Context context = initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);

        Metric metric = getMetric(MetricType.WORKSPACES_STATISTICS_LIST);

        ListValueData l = getAsList(metric, context);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.WS);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(TWID1));
    }

    @Test
    public void testDrillDownTopUsersMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.PRODUCT_USAGE_SESSIONS.toString());
        builder.put(MetricFilter.USER_ID, UID2);
        builder.put(Parameters.TO_DATE, "20131123");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_LIFETIME.toString());
        Context context = initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);

        Metric metric = getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        // test drill down page values
        ListValueData l = getAsList(metric, context);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.SESSION_ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey("session-fid2"));
    }

    @Test
    public void testNonActiveUsersMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131117");
        builder.put(Parameters.TO_DATE, "20131123");

        Metric metric = getMetric(MetricType.NON_ACTIVE_USERS);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(builder.build());

        assertTrue(expandedValue.isEmpty());

        metric = getMetric(MetricType.CREATED_USERS);
        expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(builder.build());

        List<ValueData> l = treatAsList(expandedValue);
        assertEquals(l.size(), 5);
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID1)));
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID2)));
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID3)));
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID4)));
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID5)));

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131117");
        builder.put(Parameters.TO_DATE, "20131123");
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.NON_ACTIVE_USERS.toString());

        metric = getMetric(MetricType.USERS_STATISTICS_LIST);

        ListValueData lvd = getAsList(metric, builder.build());
        assertTrue(lvd.isEmpty());
    }

    @Test
    public void testAbstractFactorySessionsMetrics() throws Exception {
        Metric metric = getMetric(MetricType.FACTORY_SESSIONS_BELOW_10_MIN);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.SESSION_ID);
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("session-fid1"));
        assertTrue(m.containsKey("session-fid2"));
    }

    @Test
    public void testProductUsageTimeBelow1MinMetric() throws Exception {
        Metric metric = getMetric(MetricType.PRODUCT_USAGE_TIME_BETWEEN_1_AND_10_MIN);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.SESSION_ID);
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("session-fid1"));
        assertTrue(m.containsKey("session-fid2"));
    }

    @Test
    public void testProductUsageTimeTotalMetric() throws Exception {
        Metric metric = getMetric(MetricType.PRODUCT_USAGE_TIME_TOTAL);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.SESSION_ID);
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("session-fid1"));
        assertTrue(m.containsKey("session-fid2"));
    }

    @Test
    public void testAbstractLoggedInTypeMetrics() throws Exception {
        Metric metric = getMetric(MetricType.USERS_LOGGED_IN_WITH_FORM);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.USER);
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testCalculatedSubtractionMetrics() throws Exception {
        Metric metric = getMetric(MetricType.CREATED_USERS_FROM_AUTH);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.USER);
        assertEquals(m.size(), 4);
        assertTrue(m.containsKey(UID2));
        assertTrue(m.containsKey(UID3));
        assertTrue(m.containsKey(UID4));
        assertTrue(m.containsKey(UID5));
    }

    @Test
    public void testCalculatedPercentMetric() throws Exception {
        Metric metric = getMetric(MetricType.FACTORY_SESSIONS_WITH_BUILD_PERCENT);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.SESSION_ID);
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey("session-fid2"));
    }

    @Test
    public void testSessionsListFilteredByCalculatedMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.FACTORY_SESSIONS_WITH_BUILD_PERCENT.toString());

        Metric metric = getMetric(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);

        ListValueData l = getAsList(metric, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.SESSION_ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey("session-fid2"));
    }

    @Test
    public void testExpandedAbstractActiveEntitiesMetrics() throws Exception {
        Metric metric = getMetric(MetricType.ACTIVE_WORKSPACES);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.WS);

        assertEquals(m.size(), 2);
        assertTrue(m.containsKey(WID1));
        assertTrue(m.containsKey(WID2));

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.PAGE, 2);
        builder.put(Parameters.PER_PAGE, 1);
        builder.put(Parameters.SORT, "+ws");

        expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(builder.build());
        m = listToMap(expandedValue, AbstractMetric.WS);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(WID2));

        builder.put(Parameters.PAGE, 3);

        expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(builder.build());
        m = listToMap(expandedValue, AbstractMetric.WS);

        assertEquals(m.size(), 0);
    }

    @Test
    public void testExpandedAbstractLongValueResultedMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY, "factory1");

        Metric metric = getMetric(MetricType.FACTORY_USED);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(builder.build());

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.FACTORY);
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey("factory1"));
    }

    @Test
    public void testExpandedAbstractCountMetrics() throws Exception {
        Metric metric = getMetric(MetricType.CREATED_PROJECTS);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey(UID2 + "/" + WID2 + "/project1"));
        assertTrue(m.containsKey(UID2 + "/" + WID2 + "/project2"));
    }

    @Test
    public void testExpandedAbstractProjectTypeMetrics() throws Exception {
        Metric metric = getMetric(MetricType.PROJECT_TYPE_JAR);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID2 + "/" + WID2 + "/project1"));
    }

    @Test
    public void testExpandedAbstractProjectPaasMetrics() throws Exception {
        Metric metric = getMetric(MetricType.PROJECT_PAAS_GAE);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey(UID2 + "/" + WID2 + "/project1"));
        assertTrue(m.containsKey(UID2 + "/" + WID2 + "/project2"));
    }

    @Test
    public void testProjectListFilteredByReadBasedMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.CREATED_PROJECTS.toString());

        Metric metric = getMetric(MetricType.PROJECTS_LIST);

        ListValueData l = getAsList(metric, builder.build());
        assertEquals(l.size(), 2);
    }

    @Test
    public void testConversionExpandedValueDataIntoViewData() throws Exception {
        Metric metric = getMetric(MetricType.ACTIVE_WORKSPACES);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        ViewData viewData = viewBuilder.getViewData(expandedValue);
        assertEquals(viewData.size(), 1);

        SectionData sectionData = viewData.get("section_expended");
        assertEquals(sectionData.size(), 3);
        assertTrue(sectionData.contains(asList(StringValueData.valueOf("ws"))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf(WID1))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf(WID2))));
    }

    @Test
    public void testTotalUsers() throws Exception {
        Metric metric = getMetric(MetricType.CREATED_USERS);
        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.USER);
        assertEquals(m.size(), 5);
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
        assertTrue(m.containsKey(UID3));
        assertTrue(m.containsKey(UID4));
        assertTrue(m.containsKey(UID5));
    }
}
