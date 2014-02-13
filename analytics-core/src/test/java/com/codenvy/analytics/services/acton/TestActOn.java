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


package com.codenvy.analytics.services.acton;

import static org.testng.AssertJUnit.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.AbstractUsersProfile;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActOn extends BaseTest {

    private static final Map<String,String> HEADERS = ActOn.headers;
    
    @BeforeClass
    public void prepare() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.ANY.name());
        Parameters.LOG.put(context, prepareLog().getAbsolutePath());
        Parameters.EVENT.put(context, "*");
        Parameters.PARAM.put(context, "user");
        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(context, MetricType.USERS_STATISTICS_LIST.name().toLowerCase());
        Parameters.STORAGE_TABLE_USERS_PROFILES.put(context, MetricType.USERS_PROFILES_LIST.name().toLowerCase());

        Parameters.STORAGE_TABLE.put(context, MetricType.ACTIVE_USERS_SET.name().toLowerCase());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, context);

        Parameters.STORAGE_TABLE.put(context, MetricType.USERS_PROFILES_LIST.name().toLowerCase());
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, context);

        Parameters.STORAGE_TABLE.put(context, MetricType.PRODUCT_USAGE_SESSIONS_LIST.name().toLowerCase());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, context);

        Parameters.STORAGE_TABLE.put(context, MetricType.USERS_STATISTICS_LIST.name().toLowerCase());
        pigServer.execute(ScriptType.USERS_STATISTICS, context);

        Parameters.FROM_DATE.put(context, "20131102");
        Parameters.TO_DATE.put(context, "20131102");

        Parameters.STORAGE_TABLE.put(context, MetricType.ACTIVE_USERS_SET.name().toLowerCase());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, context);

        Parameters.STORAGE_TABLE.put(context, MetricType.USERS_PROFILES_LIST.name().toLowerCase());
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, context);

        Parameters.STORAGE_TABLE.put(context, MetricType.PRODUCT_USAGE_SESSIONS_LIST.name().toLowerCase());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, context);

        Parameters.STORAGE_TABLE.put(context, MetricType.USERS_STATISTICS_LIST.name().toLowerCase());
        pigServer.execute(ScriptType.USERS_STATISTICS, context);
    }

    @Test
    public void testWholePeriod() throws Exception {
        ActOn job = Injector.getInstance(ActOn.class);

        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131102");

        File jobFile = job.prepareFile(context);
        assertEquals(jobFile.getName(), ActOn.FILE_NAME);

        Map<String, Map<String, String>> content = read(jobFile);
        
        assertEquals(content.size(), 4);
        
        // verify head of FTP data
        Map<String, String> headData = content.get("_HEAD");
        assertEquals(HEADERS.size(), headData.size());
        for (String column: HEADERS.values()) {
            assertEquals(column, headData.get(column));
        }

        // verify "user1" data
        Map<String, String> user1Data = content.get("user1");
        assertEquals(HEADERS.size(), user1Data.size());
        assertEquals("user1", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_EMAIL)));
        assertEquals("f", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_FIRST_NAME)));
        assertEquals("l", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_LAST_NAME)));
        assertEquals("phone", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_PHONE)));
        assertEquals("company", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_COMPANY)));
        assertEquals("2", user1Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("5", user1Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user1Data.get(HEADERS.get(ActOn.INACTIVE)));
        assertEquals("1", user1Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("1", user1Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("120", user1Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("true", user1Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("29", user1Data.get(HEADERS.get(ActOn.POINTS)));
                    
        // verify "user2" data
        Map<String, String> user2Data = content.get("user2");
        assertEquals(HEADERS.size(), user2Data.size());
        assertEquals("user2", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_EMAIL)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_FIRST_NAME)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_LAST_NAME)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_PHONE)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_COMPANY)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("7", user2Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("6", user2Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("10", user2Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user2Data.get(HEADERS.get(ActOn.INACTIVE)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("120", user2Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("false", user2Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("6", user2Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("104", user2Data.get(HEADERS.get(ActOn.POINTS)));

        // verify "user3" data
        Map<String, String> user3Data = content.get("user3");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user3", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_EMAIL)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_FIRST_NAME)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_LAST_NAME)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_PHONE)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_COMPANY)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("1", user3Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("1", user3Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user3Data.get(HEADERS.get(ActOn.INACTIVE)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("false", user3Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("1", user3Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("14", user3Data.get(HEADERS.get(ActOn.POINTS)));
    }

    @Test
    public void testOneDayPeriod() throws Exception {
        ActOn job = Injector.getInstance(ActOn.class);

        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        File jobFile = job.prepareFile(context);
        assertEquals(jobFile.getName(), ActOn.FILE_NAME);

        Map<String, Map<String, String>> content = read(jobFile);
        
        assertEquals(content.size(), 4);

        // verify head of FTP data
        Map<String, String> headData = content.get("_HEAD");
        assertEquals(HEADERS.size(), headData.size());
        for (String column: HEADERS.values()) {
            assertEquals(column, headData.get(column));
        }

        // verify "user1" data
        Map<String, String> user1Data = content.get("user1");
        assertEquals(HEADERS.size(), user1Data.size());
        assertEquals("user1", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_EMAIL)));
        assertEquals("f", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_FIRST_NAME)));
        assertEquals("l", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_LAST_NAME)));
        assertEquals("phone", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_PHONE)));
        assertEquals("company", user1Data.get(HEADERS.get(AbstractUsersProfile.USER_COMPANY)));
        assertEquals("2", user1Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user1Data.get(HEADERS.get(ActOn.INACTIVE)));
        assertEquals("1", user1Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("1", user1Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("120", user1Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("true", user1Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("29", user1Data.get(HEADERS.get(ActOn.POINTS)));
        
        // verify "user2" data
        Map<String, String> user2Data = content.get("user2");
        assertEquals(HEADERS.size(), user2Data.size());
        assertEquals("user2", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_EMAIL)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_FIRST_NAME)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_LAST_NAME)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_PHONE)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractUsersProfile.USER_COMPANY)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user2Data.get(HEADERS.get(ActOn.INACTIVE)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("120", user2Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("false", user2Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("10", user2Data.get(HEADERS.get(ActOn.POINTS)));
        
        // verify "user3" data
        Map<String, String> user3Data = content.get("user3");   
        assertEquals(HEADERS.size(), user3Data.size());        
        assertEquals("user3", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_EMAIL)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_FIRST_NAME)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_LAST_NAME)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_PHONE)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractUsersProfile.USER_COMPANY)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("false", user3Data.get(HEADERS.get(ActOn.INACTIVE)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("false", user3Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("0", user3Data.get(HEADERS.get(ActOn.POINTS)));
    }

    private Map<String, Map<String,String>> read(File jobFile) throws IOException {
        Map<String, Map<String, String>> results = new HashMap<>();
        
        List<String> columns = new ArrayList<>();
        for (String header: HEADERS.values()) {
            columns.add(header);
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(jobFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");  // remove all '"'
                String[] userDataArray= line.split(",");
                // put line values into map
                Map<String, String> userDataMap = new HashMap<>();
                for (int i = 0; i < userDataArray.length; i++) {
                    userDataMap.put(columns.get(i), userDataArray[i]);
                }
                
                if (userDataArray[0].equals("email")) {
                    results.put("_HEAD", userDataMap);    
                } else {
                    results.put(userDataArray[0], userDataMap);
                }
                
            }
        }

        return results;
    }

    private File prepareLog() throws IOException {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserSSOLoggedInEvent("user2", "google")
                        .withDate("2013-11-01").build());
        
        events.add(Event.Builder.createUserUpdateProfile("user1", "f", "l", "company", "phone", "jobtitle")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user2", "", "", "", "", "")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user3", "", "", "", "", "")
                        .withDate("2013-11-01").build());

        // active users [user1, user2, user3]
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withTime("09:00:00").withDate("2013-11-01")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withTime("09:00:00").withDate("2013-11-01")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3").withTime("09:00:00").withDate("2013-11-01")
                        .build());

        // projects created
        events.add(
                Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project1", "type1").withDate("2013-11-01")
                     .withTime("10:00:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project2", "type1").withDate("2013-11-01")
                     .withTime("10:05:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project1", "type1").withDate("2013-11-01")
                     .withTime("10:03:00").build());

        // projects built
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "project1", "type1").withTime("10:06:00")
                        .withDate("2013-11-01").build());


        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1")
                        .withTime("10:10:00,000")
                        .withDate("2013-11-02").build());
        
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2")
                        .withTime("10:00:00")
                        .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project2", "type1", "paas1")
                        .withTime("10:11:00,100")
                        .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project3", "type1", "paas1")
                        .withTime("10:12:00,200")
                        .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project4", "type1", "paas1")
                        .withTime("10:13:00,300")
                        .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project5", "type1", "paas1")
                        .withTime("10:14:00,400")
                        .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1")
                        .withTime("10:15:00,500")
                        .withDate("2013-11-02").build());

        
        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "1")
                        .withDate("2013-11-02")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "1")
                        .withDate("2013-11-02")
                        .withTime("19:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user2", "ws1", "ide", "3")
                        .withDate("2013-11-02")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2", "ws1", "ide", "3")
                        .withDate("2013-11-02")
                        .withTime("20:10:00").build());

        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1", "", "", "", "", "", "")
                        .withDate("2013-11-01")
                        .withTime("20:03:00").build());
       
        events.add(Event.Builder.createDebugStartedEvent("user2", "ws1", "", "", "id1")
                        .withDate("2013-11-01")
                        .withTime("20:06:00").build());        
        
        events.add(Event.Builder.createUserInviteEvent("user1", "ws2", "email")
                        .withDate("2013-11-01").build());

        events.add(Event.Builder.createRunStartedEvent("user2", "ws2", "project", "type", "id1")
                         .withDate("2013-11-01")
                         .withTime("20:59:00").build());        
        events.add(Event.Builder.createRunFinishedEvent("user2", "ws2", "project", "type", "id1")
                         .withDate("2013-11-01")
                         .withTime("21:01:00").build());

        events.add(Event.Builder.createBuildStartedEvent("user1", "ws1", "project", "type", "id2")
                         .withDate("2013-11-01")
                         .withTime("21:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent("user1", "ws1", "project", "type", "id2")
                         .withDate("2013-11-01")
                         .withTime("21:14:00").build());

        return LogGenerator.generateLog(events);
    }
}
