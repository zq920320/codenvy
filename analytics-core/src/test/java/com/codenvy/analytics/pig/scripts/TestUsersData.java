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
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.sessions.ProductUsageSessionsList;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.metrics.workspaces.UsageTimeByWorkspacesList;
import com.codenvy.analytics.metrics.workspaces.WorkspacesStatisticsList;
import com.codenvy.analytics.services.DataComputationFeature;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.mongodb.util.MyAsserts.assertEquals;
import static com.mongodb.util.MyAsserts.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersData extends BaseTest {

    private static final String RESOURCE_DIR = BASE_DIR + "/test-classes/" + TestUsersData.class.getSimpleName();
    private static final String MESSAGES     = RESOURCE_DIR + "/messages.log";

    @BeforeClass
    public void prepare() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, MESSAGES);

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        DataComputationFeature dataComputationFeature = new DataComputationFeature();
        dataComputationFeature.forceExecute(builder.build());
    }

    @Test
    public void testUserStatistics() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        ListValueData valueData = ValueDataUtil.getAsList(metric, Context.EMPTY);
        ListValueData summaryValue = (ListValueData)((Summaraziable)metric).getSummaryValue(Context.EMPTY);

        assertUserData(valueData, summaryValue);
    }

    private void assertUserData(ListValueData value, ListValueData summaryValue) {
        assertEquals(value.size(), 4);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();

            String user = all.get(UsersStatisticsList.USER).getAsString();
            switch (user) {
                case "id1":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300500");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), "f1");
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), "l1");
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), "company1");
                    assertEquals(all.get(UsersStatisticsList.USER_JOB).getAsString(), "Other");
                    break;

                case "id2":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "120500");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), "f2");
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), "l2");
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), "company1");
                    assertEquals(all.get(UsersStatisticsList.USER_JOB).getAsString(), "Other");
                    break;

                case "id3":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120000");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120500");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), "f3");
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), "l3");
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), "company3");
                    assertEquals(all.get(UsersStatisticsList.USER_JOB).getAsString(), "Other");
                    break;

                case "id4":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), StringValueData.DEFAULT);
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), StringValueData.DEFAULT);
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), StringValueData.DEFAULT);
                    assertEquals(all.get(UsersStatisticsList.USER_JOB).getAsString(), StringValueData.DEFAULT);
                    break;

                default:
                    fail("unknown user " + user);
                    break;
            }
        }

        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(summary.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.DEPLOYS).getAsString(), "3");
        assertEquals(summary.get(UsersStatisticsList.BUILDS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.RUNS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.FACTORIES).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "420500");
        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.INVITES).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.LOGINS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.RUN_TIME).getAsString(), "120500");
        assertEquals(summary.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120500");
        assertEquals(summary.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "2");
    }

    @Test
    public void testWorkspaceStatistics() throws Exception {
        Context.Builder builder = new Context.Builder();

        Metric metric = new WorkspacesStatisticsList();
        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 2);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            assertEquals(all.size(), 14);

            String ws = all.get(UsersStatisticsList.WS).getAsString();
            switch (ws) {
                case "wsid1":
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.WS).getAsString(), "wsid1");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300500");
                    assertEquals(all.get(WorkspacesStatisticsList.JOINED_USERS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    break;

                case "wsid2":
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "2");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "2");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.WS).getAsString(), "wsid2");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120000");
                    assertEquals(all.get(WorkspacesStatisticsList.JOINED_USERS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120500");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "120500");
                    break;

                default:
                    fail("unknown workspace: " + ws);
                    break;
            }
        }

        ListValueData summaryValue = (ListValueData)((Summaraziable)metric).getSummaryValue(Context.EMPTY);

        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(summary.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.INVITES).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.DEPLOYS).getAsString(), "3");
        assertEquals(summary.get(UsersStatisticsList.BUILDS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.FACTORIES).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "420500");
        assertEquals(summary.get(WorkspacesStatisticsList.JOINED_USERS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120500");
        assertEquals(summary.get(UsersStatisticsList.RUNS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.RUN_TIME).getAsString(), "120500");
    }

    @Test
    public void testUsersTimeInWorkspaces() throws Exception {
        Context.Builder builder = new Context.Builder();

        UsageTimeByWorkspacesList metric = new UsageTimeByWorkspacesList();
        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 2);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            String ws = all.get(ProductUsageSessionsList.WS).getAsString();

            switch (ws) {
                case "wsid1":
                    assertEquals(all.size(), 3);
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300500");
                    break;

                case "wsid2":
                    assertEquals(all.size(), 3);
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120000");
                    break;

                default:
                    fail("unknown ws " + ws);
                    break;

            }
        }
    }

    @Test
    public void testUsersTimeInWorkspacesWithFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "id1");

        UsageTimeByWorkspacesList metric = new UsageTimeByWorkspacesList();
        ListValueData valueData = (ListValueData)metric.getValue(builder.build());

        assertEquals(valueData.size(), 1);

        List<ValueData> items = valueData.getAll();
        MapValueData entry = (MapValueData)items.get(0);

        assertEquals(entry.getAll().get(UsageTimeByWorkspacesList.SESSIONS).getAsString(), "1");
        assertEquals(entry.getAll().get(ProductUsageSessionsList.TIME).getAsString(), "300500");
        assertEquals(entry.getAll().get(ProductUsageSessionsList.WS).getAsString(), "wsid1");

    }

    @Test
    public void testUsersStatisticsByCompany() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1");

        UsersStatisticsList metric = new UsersStatisticsList();
        ListValueData valueData = (ListValueData)metric.getValue(builder.build());

        assertEquals(valueData.size(), 2);
    }

    @Test
    public void testUserStatisticsFilteredByAliases() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ALIASES, "user1@gmail.com");

        ListValueData valueData = (ListValueData)metric.getValue(builder.build());
        List<ValueData> rows = treatAsList(valueData);
        assertEquals(rows.size(), 1);

        String userId = ((MapValueData)rows.get(0)).getAll().get(AbstractMetric.USER).getAsString();
        assertEquals(userId, "id1");

        String aliases = ((MapValueData)rows.get(0)).getAll().get(AbstractMetric.ALIASES).getAsString();
        assertEquals(aliases, "[user1@gmail.com]");
    }

    @Test
    public void testUserStatisticsFilteredByWrongAliases() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ALIASES, "id1");

        ListValueData valueData = getAsList(metric, builder.build());
        List<ValueData> rows = treatAsList(valueData);
        assertEquals(rows.size(), 0);
    }

    @Test
    public void testUserStatisticsFilteredBySeveralAliases() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ALIASES, "user1@gmail.com OR user2@gmail.com");

        ListValueData valueData = getAsList(metric, builder.build());
        List<ValueData> rows = treatAsList(valueData);
        assertEquals(rows.size(), 2);
    }

    @Test
    public void testUsersStatisticsByCombinedFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "id1 OR id2 OR id3 OR id4");

        UsersStatisticsList metric = new UsersStatisticsList();
        assertEquals(getAsList(metric, builder.build()).size(), 4);

        builder.put(MetricFilter.USER_FIRST_NAME, "f1 OR f2");
        assertEquals(getAsList(metric, builder.build()).size(), 2);

        builder.put(MetricFilter.ALIASES, "user1@gmail.com");
        assertEquals(getAsList(metric, builder.build()).size(), 1);
    }
}
