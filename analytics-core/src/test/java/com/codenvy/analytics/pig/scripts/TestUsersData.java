/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.metrics.users.UsersTimeInWorkspacesList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;
import static com.mongodb.util.MyAsserts.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersData extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        Map<String, String> params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                        .withDate("2013-11-01").build());
        
        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f2", "l2", "company1", "11", "1")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f2", "l2", "company1", "11", "1")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user3@gmail.com", "f2", "l2", "company2", "11", "1")
                        .withDate("2013-11-01").build());

        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createSessionStartedEvent("user3@gmail.com", "ws2", "ide", "2").withDate("2013-11-01")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user3@gmail.com", "ws2", "ide", "2").withDate("2013-11-01")
                        .withTime("19:02:00").build());

        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type")
                                .withDate("2013-11-01")
                                .withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type")
                                .withDate("2013-11-01")
                                .withTime("19:10:00").build());

        events.add(Event.Builder.createBuildStartedEvent("user3@gmail.com", "ws2", "project", "type")
                                .withDate("2013-11-01")
                                .withTime("19:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent("user3@gmail.com", "ws2", "project", "type")
                                .withDate("2013-11-01")
                                .withTime("19:14:00").build());
         
        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws2", "email")
                         .withDate("2013-11-01").build());
        
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "s", "", "")
                        .withDate("2013-11-01").withTime("20:01:00").build());
        events.add(Event.Builder.createProjectDeployedEvent("user4@gmail.com", "ws1", "s", "", "", "")
                        .withDate("2013-11-01").withTime("20:02:00").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1@gmail.com", "", "", "", "", "", "")
                        .withDate("2013-11-01").withTime("20:03:00").build());
        events.add(Event.Builder.createRunStartedEvent("user4@gmail.com", "ws1", "", "")
                        .withDate("2013-11-01").withTime("20:04:00").build());
        events.add(Event.Builder.createDebugStartedEvent("user4@gmail.com", "ws1", "", "")
                        .withDate("2013-11-01").withTime("20:06:00").build());

        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "", "project1", "type1", "paas1")
                        .withDate("2013-11-01")
                        .withTime("20:10:00")
                        .build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws2", "", "project1", "type1", "paas2")
                        .withDate("2013-11-01")
                        .withTime("20:15:00")
                        .build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testusersdata-sessions");
        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(params, "testusersdata");
        Parameters.STORAGE_TABLE_USERS_PROFILES.put(params, "testusersdata-profiles");
        Parameters.LOG.put(params, log.getAbsolutePath());

        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);

        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testusersdata");
        pigServer.execute(ScriptType.USERS_STATISTICS, params);

        Parameters.STORAGE_TABLE.put(params, MetricType.USERS_PROFILES_LIST.name().toLowerCase());
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, params);
    }

    @Test
    public void testSingleProfile() throws Exception {
        Map<String, String> context = Utils.newContext();

        Metric metric = new TestUsersStatisticsList();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 4);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            assertEquals(all.size(), 14);

            String user = all.get(UsersStatisticsList.USER).getAsString();
            switch (user) {
                case "user1@gmail.com":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "0");
                    break;

                case "user2@gmail.com":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "120");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "1");
                    break;

                case "user3@gmail.com":
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.INVITES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "120");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "1");
                    break;

                case "user4@gmail.com":
                    assertEquals(all.get(UsersStatisticsList.PROJECTS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.DEPLOYS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.BUILDS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.DEBUGS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.RUNS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.FACTORIES).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.LOGINS).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.RUN_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.BUILD_TIME).getAsString(), "0");
                    assertEquals(all.get(UsersStatisticsList.PAAS_DEPLOYS).getAsString(), "0");
                    break;

                default:
                    fail("unknown user " + user);
                    break;
            }
        }
    }

    @Test
    public void testUsersTimeInWorkspaces() throws Exception {
        Map<String, String> context = Utils.newContext();

        TestUsersTimeInWorkspaces metric = new TestUsersTimeInWorkspaces();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 2);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            String ws = all.get(UsersStatisticsList.USER).getAsString();

            switch (ws) {
                case "ws1":
                    assertEquals(all.size(), 3);
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "300");
                    break;

                case "ws2":
                    assertEquals(all.size(), 3);
                    assertEquals(all.get(UsersStatisticsList.SESSIONS).getAsString(), "1");
                    assertEquals(all.get(UsersStatisticsList.TIME).getAsString(), "120");
                    break;

                default:
                    fail("unknown ws " + ws);
                    break;

            }
        }
    }

    @Test
    public void testUsersTimeInWorkspacesWithFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER.put(context, "user1@gmail.com");

        TestUsersTimeInWorkspaces metric = new TestUsersTimeInWorkspaces();
        ListValueData valueData = (ListValueData)metric.getValue(context);

        assertEquals(valueData.size(), 1);

        List<ValueData> items = valueData.getAll();
        MapValueData entry = (MapValueData)items.get(0);

        assertEquals(entry.getAll().get(UsersStatisticsList.SESSIONS).getAsString(), "1");
        assertEquals(entry.getAll().get(UsersStatisticsList.TIME).getAsString(), "300");
        assertEquals(entry.getAll().get(UsersStatisticsList.USER).getAsString(), "ws1");

    }

    @Test
    public void testUsersStatisticsByCompany() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER_COMPANY.put(context, "company1");

        TestUsersStatisticsList metric = new TestUsersStatisticsList();
        ListValueData valueData = (ListValueData)metric.getValue(context);

        assertEquals(valueData.size(), 2);
    }

    private class TestUsersTimeInWorkspaces extends UsersTimeInWorkspacesList {

        @Override
        public String getStorageCollectionName() {
            return "testusersdata-sessions";
        }
    }

    private class TestUsersStatisticsList extends UsersStatisticsList {

        @Override
        public String getStorageCollectionName() {
            return "testusersdata";
        }
    }
}
