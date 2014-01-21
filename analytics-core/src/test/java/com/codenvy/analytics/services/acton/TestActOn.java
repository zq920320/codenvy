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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActOn extends BaseTest {

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

        Set<String> content = read(jobFile);

        assertEquals(content.size(), 4);
        assertTrue(content.contains(
                "email,firstName,lastName,phone,company,projects,builts,deployments,spentTime,inactive,invites"));
        assertTrue(content.contains("\"user1\",\"f\",\"l\",\"phone\",\"company\",\"2\",\"0\",\"0\",\"5\",\"true\",\"1\""));
        assertTrue(content.contains("\"user2\",\"\",\"\",\"\",\"\",\"1\",\"2\",\"1\",\"10\",\"true\",\"0\""));
        assertTrue(content.contains("\"user3\",\"\",\"\",\"\",\"\",\"0\",\"1\",\"1\",\"0\",\"true\",\"0\""));
    }

    @Test
    public void testOneDayPeriod() throws Exception {
        ActOn job = Injector.getInstance(ActOn.class);

        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        File jobFile = job.prepareFile(context);
        assertEquals(jobFile.getName(), ActOn.FILE_NAME);

        Set<String> content = read(jobFile);

        assertEquals(content.size(), 4);
        assertTrue(content.contains(
                "email,firstName,lastName,phone,company,projects,builts,deployments,spentTime,inactive,invites"));
        assertTrue(content.contains("\"user1\",\"f\",\"l\",\"phone\",\"company\",\"2\",\"0\",\"0\",\"0\",\"true\",\"1\""));
        assertTrue(content.contains("\"user2\",\"\",\"\",\"\",\"\",\"1\",\"1\",\"0\",\"0\",\"true\",\"0\""));
        assertTrue(content.contains("\"user3\",\"\",\"\",\"\",\"\",\"0\",\"0\",\"0\",\"0\",\"false\",\"0\""));
    }

    private Set<String> read(File jobFile) throws IOException {
        Set<String> result = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(jobFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }

        return result;
    }

    private File prepareLog() throws IOException {
        List<Event> events = new ArrayList<>();

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
                        .withTime("10:10:00").withDate("2013-11-02").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2")
                        .withTime("10:00:00").withDate("2013-11-02").build());


        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "1").withDate("2013-11-02")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "1").withDate("2013-11-02")
                        .withTime("19:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user2", "ws1", "ide", "3").withDate("2013-11-02")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2", "ws1", "ide", "3").withDate("2013-11-02")
                        .withTime("20:10:00").build());

        events.add(Event.Builder.createUserInviteEvent("user1", "ws2", "email").withDate(
                "2013-11-01").build());


        return LogGenerator.generateLog(events);
    }
}
