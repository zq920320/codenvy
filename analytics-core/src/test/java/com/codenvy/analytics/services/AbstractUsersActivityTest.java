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
package com.codenvy.analytics.services;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
public abstract class AbstractUsersActivityTest extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        computeStatistics("20131101");
        computeStatistics("20131102");
    }

    protected void computeStatistics(String date) throws Exception {
        executeScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST, date);
        executeScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST, date);
        executeScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST, date);
        executeScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST, date);
        executeScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY_LIST, date);
        executeScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET, date);
        executeScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET, date);
        executeScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_LOGGED_IN_TYPES, date);
    }

    private void executeScript(ScriptType scriptType, MetricType metricType, String date) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.putAll(scriptsManager.getScript(scriptType, metricType).getParamsAsMap());
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, prepareLog().getAbsolutePath());
        pigServer.execute(scriptType, builder.build());
    }

    protected abstract Map<String, String> getHeaders();

    protected Map<String, Map<String, String>> read(File jobFile) throws IOException {
        Map<String, Map<String, String>> results = new HashMap<>();

        List<String> columns = new ArrayList<>();
        for (String header : getHeaders().values()) {
            columns.add(header);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(jobFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");  // remove all '"'
                String[] userDataArray = line.split(",");
                // put line values into map
                Map<String, String> userDataMap = new HashMap<>();
                for (int i = 0; i < userDataArray.length; i++) {
                    userDataMap.put(columns.get(i), userDataArray[i]);
                }

                if (userDataArray[0].equals(getHeaders().get(AbstractMetric.ID))) {
                    results.put("_HEAD", userDataMap);
                } else {
                    results.put(userDataArray[0], userDataMap);
                }

            }
        }

        return results;
    }

    private File prepareLog() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("id1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("id2", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("id3", "user3@gmail.com", "user3@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());

        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-11-01").withTime("10:10:20").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-11-01").withTime("10:10:30").build());

        events.add(Event.Builder.createUserUpdateProfile("id1", "user1@gmail.com", "user1@gmail.com", "f", "l", "company", "phone", "jobtitle")
                                .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("id2", "user2@gmail.com", "user2@gmail.com", "", "", "", "", "")
                                .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("id3", "user3@gmail.com", "user3@gmail.com", "", "", "", "", "")
                                .withDate("2013-11-01").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "ws2", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid3", "ws3", "user3@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid4", "ws2___", "user3@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());

        // active users [user1, user2, user3]
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com").withTime("09:00:00").withDate("2013-11-01")
                                .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2@gmail.com").withTime("09:00:00").withDate("2013-11-01")
                                .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3@gmail.com").withTime("09:00:00").withDate("2013-11-01")
                                .build());

        // projects created
        events.add(
                Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project1", "type1").withDate("2013-11-01")
                             .withTime("10:00:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project2", "type1").withDate("2013-11-01")
                             .withTime("10:05:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "project1", "type1").withDate("2013-11-01")
                             .withTime("10:03:00").build());

        // projects built
        events.add(Event.Builder.createBuildStartedEvent("user2@gmail.com", "ws1", "project1", "type1", "").withTime("10:06:00")
                                .withDate("2013-11-01").build());


        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project1", "type1", "paas1")
                                .withTime("10:10:00,000")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws2", "project1", "type1", "paas2")
                                .withTime("10:00:00")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project2", "type1", "paas1")
                                .withTime("10:11:00,100")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project3", "type1", "paas1")
                                .withTime("10:12:00,200")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project4", "type1", "paas1")
                                .withTime("10:13:00,300")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project5", "type1", "paas1")
                                .withTime("10:14:00,400")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project1", "type1", "paas1")
                                .withTime("10:15:00,500")
                                .withDate("2013-11-02").build());


        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "1", "2013-11-02 19:00:00", "2013-11-02 19:05:00", false)
                                .withDate("2013-11-02").withTime("19:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("user2@gmail.com", "ws1", "2", "2013-11-02 20:00:00", "2013-11-02 20:10:00", false)
                                .withDate("2013-11-02").withTime("20:00:00").build());

        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1@gmail.com", "", "", "", "", "", "")
                                .withDate("2013-11-01")
                                .withTime("20:03:00").build());

        events.add(Event.Builder.createDebugStartedEvent("user2@gmail.com", "ws1", "", "", "id1")
                                .withDate("2013-11-01")
                                .withTime("20:06:00").build());

        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws2", "email")
                                .withDate("2013-11-01").build());

        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type", "id1")
                                .withDate("2013-11-01").withTime("20:59:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type", "id1", 120000, 1)
                                .withDate("2013-11-01").withTime("21:01:00").build());

        events.add(Event.Builder.createBuildStartedEvent("user1@gmail.com", "ws1", "project", "type", "id2")
                                .withDate("2013-11-01").withTime("21:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent("user1@gmail.com", "ws1", "project", "type", "id2", 120000)
                                .withDate("2013-11-01").withTime("21:14:00").build());

        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws2___", "project1", "type1", "paas2")
                                .withTime("10:00:01")
                                .withDate("2013-11-03").build());

        return LogGenerator.generateLog(events);
    }
}
