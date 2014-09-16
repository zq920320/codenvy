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
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.DataComputationFeature;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov  */
public class TestFactoryStatisticsFilteringById extends BaseTest {

    public static final String TEST_FACTORY_ID          = "abc123";
    public static final String TEST_ENCODED_FACTORY_URL = "https://test.com/factory?id=" + TEST_FACTORY_ID;
    public static final String TEST_USER_ALIAS          = "user1@gmail.com";
    public static final String TEST_FACTORY_URL         = "factoryUrl2";

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("uid1", TEST_USER_ALIAS, TEST_USER_ALIAS)
                                .withDate("2013-02-10").withTime("07:00:00").build());

        // factory 1 events
        events.add(
            Event.Builder
                .createFactoryUrlAcceptedEvent("tmp-1", TEST_ENCODED_FACTORY_URL, "http://referrer1", "org1", "affiliate1")
                .withDate("2013-02-10").withTime("08:00:00").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", TEST_USER_ALIAS)
                                .withDate("2013-02-10").withTime("09:00:00").build());

        events.add(Event.Builder.createSessionUsageEvent(TEST_USER_ALIAS, "tmp-1", "id1", "2013-02-10 10:00:00", "2013-02-10 10:05:00", true)
                                .withDate("2013-02-10").withTime("10:00:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", TEST_USER_ALIAS, "project", "type")
                                .withDate("2013-02-10").withTime("10:04:00").build());

        // factory 2 events
        events.add(
            Event.Builder
                .createFactoryUrlAcceptedEvent("tmp-2", TEST_FACTORY_URL, "http://referrer2", "org2", "affiliate1")
                .withDate("2013-02-10").withTime("08:00:01").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", TEST_USER_ALIAS)
                                .withDate("2013-02-10").withTime("09:01:00").build());

        events.add(Event.Builder.createSessionUsageEvent(TEST_USER_ALIAS, "tmp-2", "id2", "2013-02-10 10:20:00", "2013-02-10 10:30:00", true)
                                .withDate("2013-02-10").withTime("10:20:00").build());

        events.add(Event.Builder.createRunStartedEvent(TEST_USER_ALIAS, "tmp-2", "project", "type", "id1")
                                .withDate("2013-02-10").withTime("10:23:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_TEMPORARY_WORKSPACES, MetricType.TEMPORARY_WORKSPACES_CREATED).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, builder.build());
        
        DataComputationFeature dataComputationFeature = new DataComputationFeature();
        dataComputationFeature.forceExecute(builder.build());
    }

    @Test
    public void testSummarizedFactoryStatistics() throws Exception {
        Summaraziable metric = (Summaraziable)MetricFactory.getMetric(MetricType.FACTORY_STATISTICS_LIST);
        ListValueData summaryValue = (ListValueData)metric.getSummaryValue(Context.EMPTY);

        // verify summary data on metric FACTORY_STATISTICS_LIST_PRECOMPUTED
        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();

        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "900000");
        assertEquals(summary.get(UsersStatisticsList.AUTHENTICATED_SESSION).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.CONVERTED_SESSION).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.RUNS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.BUILDS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.WS_CREATED).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.ENCODED_FACTORY).getAsString(), "1");
    }

    @Test
    public void testSummarizedFactoryStatisticsFilteredById() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY_ID, TEST_FACTORY_ID);

        Summaraziable metric = (Summaraziable)MetricFactory.getMetric(MetricType.FACTORY_STATISTICS_LIST);
        ListValueData summaryValue = (ListValueData)metric.getSummaryValue(builder.build());

        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();

        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "300000");
        assertEquals(summary.get(UsersStatisticsList.AUTHENTICATED_SESSION).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.CONVERTED_SESSION).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.RUNS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.BUILDS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.WS_CREATED).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.ENCODED_FACTORY).getAsString(), "1");
    }

    @Test
    public void testSummarizedFactoryStatisticsFilteredByUrl() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY, TEST_FACTORY_URL);

        Summaraziable metric = (Summaraziable)MetricFactory.getMetric(MetricType.FACTORY_STATISTICS_LIST);
        ListValueData summaryValue = (ListValueData)metric.getSummaryValue(builder.build());

        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();

        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "600000");
        assertEquals(summary.get(UsersStatisticsList.AUTHENTICATED_SESSION).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.CONVERTED_SESSION).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.RUNS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.BUILDS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.WS_CREATED).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.ENCODED_FACTORY).getAsString(), "0");
    }
}
