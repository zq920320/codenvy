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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.metrics.workspaces.WorkspacesStatisticsList;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.services.DataComputationFeature;

import org.quartz.JobExecutionException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/** @author Anatoliy Bazko */
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

        List<ValueData> value = getAsList(metric, Context.EMPTY).getAll();
        List<ValueData> summaryValue = treatAsList(((Summaraziable)metric).getSummaryValue(Context.EMPTY));

        assertUserData(value, summaryValue);
    }

    private void assertUserData(List<ValueData> value, List<ValueData> summaryValue) {
        assertEquals(value.size(), 4);

        for (ValueData object : value) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();

            String user = all.get(UsersStatisticsList.USER).getAsString();
            switch (user) {
                case "user1_12345678901234":
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

                case "user2_12345678901234":
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

                case "user3_12345678901234":
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

                case "user4_12345678901234":
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
                    assertEquals(all.get(UsersStatisticsList.USER_FIRST_NAME), StringValueData.DEFAULT);
                    assertEquals(all.get(UsersStatisticsList.USER_LAST_NAME), StringValueData.DEFAULT);
                    assertEquals(all.get(UsersStatisticsList.USER_COMPANY), StringValueData.DEFAULT);
                    break;

                default:
                    fail("unknown user " + user);
                    break;
            }
        }

        assertEquals(summaryValue.size(), 1);

        Map<String, ValueData> m = treatAsMap(summaryValue.get(0));
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
            assertEquals(all.size(), 16);

            String ws = all.get(UsersStatisticsList.WS).getAsString();
            switch (ws) {
                case "workspace1_12345678901234":
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300000");
                    assertEquals(all.get(WorkspacesStatisticsList.JOINED_USERS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_WAITING_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_WAITING_TIME).getAsString(), "3000");
                    break;

                case "workspace2_12345678901234":
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "2");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120000");
                    assertEquals(all.get(WorkspacesStatisticsList.JOINED_USERS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120000");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "120000");
                    assertEquals(all.get(UsersStatisticsList.BUILD_WAITING_TIME).getAsString(), "1000");
                    assertEquals(all.get(UsersStatisticsList.RUN_WAITING_TIME).getAsString(), "2000");
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
        assertEquals(summary.get(UsersStatisticsList.BUILD_WAITING_TIME).getAsString(), "1000");
        assertEquals(summary.get(UsersStatisticsList.RUN_WAITING_TIME).getAsString(), "5000");
    }

    @Test
    public void testUsageTimeInWorkspaces() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USAGE_TIME_BY_WORKSPACES_LIST);
        List<ValueData> value = getAsList(metric, Context.EMPTY).getAll();

        assertEquals(value.size(), 2);
        assertTrue(value.contains(MapValueData.valueOf("ws=workspace1_12345678901234,sessions=1,time=300000")));
        assertTrue(value.contains(MapValueData.valueOf("ws=workspace2_12345678901234,sessions=1,time=120000")));
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

        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        doIntegrity("20131101");

        DataComputationFeature dataComputationFeature = new DataComputationFeature();
        dataComputationFeature.forceExecute(builder.build());
    }
}
