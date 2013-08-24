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


package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.DataProcessing;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActOnJob {

    private ActOnJob            job;
    private Map<String, String> context;

    @BeforeMethod
    private void setUp() throws Exception {
        File file = prepareLog();

        context = Utils.newContext();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.LOG.put(context, file.getAbsolutePath());

        DataProcessing.calculateAndStore(MetricType.USER_UPDATE_PROFILE, context);
        DataProcessing.calculateAndStore(MetricType.ACTIVE_USERS_SET, context);
        DataProcessing.calculateAndStore(MetricType.PROJECT_CREATED, context);
        DataProcessing.calculateAndStore(MetricType.PROJECT_BUILT, context);
        DataProcessing.calculateAndStore(MetricType.PROJECT_DEPLOYED, context);
        DataProcessing.calculateAndStore(MetricType.PRODUCT_USAGE_SESSIONS, context);

        job = new ActOnJob(null);
    }

    @Test
    public void testPrepareFile() throws Exception {
        File jobFile = job.prepareFile(context);
        assertEquals(jobFile.getName(), ActOnJob.FILE_NAME);

        Set<String> content = read(jobFile);

        assertEquals(content.size(), 4);
        assertTrue(content.contains("email,firstName,lastName,phone,company,projects,builts,deployments,spentTime"));
        assertTrue(content.contains("\"user1\",\"f\",\"l\",\"phone\",\"company\",2,0,0,5"));
        assertTrue(content.contains("\"user2\",\"\",\"\",\"\",\"\",1,2,1,10"));
        assertTrue(content.contains("\"user3\",\"\",\"\",\"\",\"\",0,1,1,0"));
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
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user2", "", "", "", "", "")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user3", "", "", "", "", "")
                        .withDate("2013-01-01").build());

        // active users [user1, user2, user3]
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withTime("09:00:00").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withTime("09:00:00").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3").withTime("09:00:00").withDate("2013-01-01")
                        .build());

        // projects created
        events.add(
                Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project1", "type1").withDate("2013-01-01")
                     .withTime("10:00:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project2", "type1").withDate("2013-01-01")
                     .withTime("10:05:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project1", "type1").withDate("2013-01-01")
                     .withTime("10:00:00").build());

        // projects built
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "project1", "type1").withTime("10:06:00")
                        .withDate("2013-01-01").build());


        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1")
                        .withTime("10:10:00").withDate("2013-01-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2")
                        .withTime("10:00:00").withDate("2013-01-01").build());


        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user2", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:10:00").build());


        return LogGenerator.generateLog(events);
    }
}
