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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.sessions.ProductUsageSessionsList;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.metrics.workspaces.UsageTimeByWorkspacesList;
import com.codenvy.analytics.metrics.workspaces.WorkspacesStatisticsList;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.services.DataComputationFeature;

import org.quartz.JobExecutionException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static com.mongodb.util.MyAsserts.assertEquals;
import static com.mongodb.util.MyAsserts.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersData extends BaseTest {

    private static final String RESOURCE_DIR = BASE_DIR + "/test-classes/" + TestUsersData.class.getSimpleName();
    private static final String MESSAGES     = RESOURCE_DIR + "/messages.log";

    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
    }

    @Test
    public void testUserStatistics() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        ListValueData valueData = getAsList(metric, Context.EMPTY);
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
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300000");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), "f1");
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), "l1");
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), "company1");
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
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "120000");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), "f2");
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), "l2");
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), "company1");
                    break;

                case "id3":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120000");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120000");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), "f3");
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), "l3");
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), "company3");
                    break;

                case "id4":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME).getAsString(), StringValueData.DEFAULT);
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME).getAsString(), StringValueData.DEFAULT);
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY).getAsString(), StringValueData.DEFAULT);
                    break;

                default:
                    fail("unknown user " + user);
                    break;
            }
        }

        assertEquals(summaryValue.size(), 1);

        Map<String, ValueData> m = treatAsMap(summaryValue.getAll().get(0));
        assertEquals(m.get(UsersStatisticsList.PROJECTS), LongValueData.valueOf(1));
        assertEquals(m.get(UsersStatisticsList.DEPLOYS), LongValueData.valueOf(2));
        assertEquals(m.get(UsersStatisticsList.BUILDS), LongValueData.valueOf(1));
        assertEquals(m.get(UsersStatisticsList.DEBUGS), LongValueData.valueOf(1));
        assertEquals(m.get(UsersStatisticsList.RUNS), LongValueData.valueOf(2));
        assertEquals(m.get(UsersStatisticsList.FACTORIES), LongValueData.valueOf(1));
        assertEquals(m.get(UsersStatisticsList.TIME), LongValueData.valueOf(420000));
        assertEquals(m.get(UsersStatisticsList.SESSIONS), LongValueData.valueOf(2));
        assertEquals(m.get(UsersStatisticsList.RUN_TIME), LongValueData.valueOf(120000));
        assertEquals(m.get(UsersStatisticsList.BUILD_TIME), LongValueData.valueOf(120000));
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
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.WS).getAsString(), "wsid1");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300000");
                    assertEquals(all.get(WorkspacesStatisticsList.JOINED_USERS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    break;

                case "wsid2":
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "2");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.WS).getAsString(), "wsid2");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120000");
                    assertEquals(all.get(WorkspacesStatisticsList.JOINED_USERS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120000");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "120000");
                    break;

                default:
                    fail("unknown workspace: " + ws);
                    break;
            }
        }

        ListValueData summaryValue = (ListValueData)((Summaraziable)metric).getSummaryValue(Context.EMPTY);

        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.DEPLOYS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.BUILDS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "420000");
        assertEquals(summary.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120000");
        assertEquals(summary.get(UsersStatisticsList.RUNS).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.RUN_TIME).getAsString(), "120000");
    }

    @Test
    public void testUssageTimeInWorkspaces() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USAGE_TIME_BY_WORKSPACES_LIST);
        ListValueData data = getAsList(metric, Context.EMPTY);

        assertEquals(data.size(), 2);

        Map<String, ValueData> m = treatAsMap(data.getAll().get(0));
        doChecUsageTimeInWorkspaces(m);

        m = treatAsMap(data.getAll().get(1));
        doChecUsageTimeInWorkspaces(m);
    }

    private void doChecUsageTimeInWorkspaces(Map<String, ValueData> m) {
        if (m.get(AbstractMetric.WS).equals(StringValueData.valueOf("wsid1"))) {
            assertEquals(m.get(UsersStatisticsList.SESSIONS).getAsString(), LongValueData.valueOf(1));
            assertEquals(m.get(UsersStatisticsList.TIME).getAsString(), LongValueData.valueOf(300000));

        } else if (m.get(AbstractMetric.WS).equals(StringValueData.valueOf("wsid2"))) {
            assertEquals(m.get(UsersStatisticsList.SESSIONS).getAsString(), LongValueData.valueOf(1));
            assertEquals(m.get(UsersStatisticsList.TIME).getAsString(), LongValueData.valueOf(120000));

        } else {
            fail("unknown ws");
        }
    }

    @Test
    public void testUsersTimeInWorkspacesWithFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "id1");

        Metric metric = MetricFactory.getMetric(MetricType.USAGE_TIME_BY_WORKSPACES_LIST);
        ListValueData data = getAsList(metric, builder.build());

        assertEquals(data.size(), 1);

        Map<String, ValueData> m = treatAsMap(data.getAll().get(0));

        assertEquals(m.get(UsageTimeByWorkspacesList.SESSIONS), LongValueData.valueOf(1));
        assertEquals(m.get(ProductUsageSessionsList.TIME), LongValueData.valueOf(300000));
        assertEquals(m.get(ProductUsageSessionsList.WS), StringValueData.valueOf("wsid1"));
    }

    @Test
    public void testFilterStatisticsByCompany() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData data = getAsList(metric, builder.build());

        assertEquals(data.size(), 2);
    }

    @Test
    public void testFilterStatisticsByUserAliases() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ALIASES, "user1@gmail.com");

        ListValueData data = getAsList(metric, builder.build());
        assertEquals(data.size(), 1);

        Map<String, ValueData> m = treatAsMap(data.getAll().get(0));

        assertEquals(m.get(AbstractMetric.USER), StringValueData.valueOf("id1"));
        assertEquals(m.get(AbstractMetric.ALIASES), StringValueData.valueOf("[user1@gmail.com]"));
    }

    @Test
    public void testFilterStatisticsByIdAsUserAliases() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ALIASES, "id1");

        ListValueData data = getAsList(metric, builder.build());
        assertEquals(data.size(), 1);

        Map<String, ValueData> m = treatAsMap(data.getAll().get(0));

        assertEquals(m.get(AbstractMetric.USER), StringValueData.valueOf("id1"));
        assertEquals(m.get(AbstractMetric.ALIASES), StringValueData.valueOf("[user1@gmail.com]"));
    }

    @Test
    public void testFilterStatisticsByUserFirstName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_FIRST_NAME, "f1 OR f2");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData data = getAsList(metric, builder.build());

        assertEquals(data.size(), 2);
    }

    @Test
    public void testFilterStatisticsByUserID() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "id1 OR id2 OR id3 OR id4");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData data = getAsList(metric, builder.build());

        assertEquals(data.size(), 4);
    }

    private void prepareData() throws IOException, ParseException, JobExecutionException {
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
}
