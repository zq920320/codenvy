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

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import com.codenvy.organization.client.UserManager;
import com.codenvy.organization.exception.OrganizationServiceException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActOnJob {

    private ActOnJob            job;
    private Map<String, String> context;

    @BeforeMethod
    private void setUp() throws IOException, OrganizationServiceException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        File file = prepareLog(date);

        System.setProperty(PigScriptExecutor.ANALYTICS_LOGS_DIRECTORY_PROPERTY, file.getParent());

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("firstName", "Chuck");
        attributes.put("lastName", "Norris");
        attributes.put("phone", "00000000");
        attributes.put("employer", "Eath");

        UserManager userManager = mock(UserManager.class);

        context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), MetricParameter.FROM_DATE.getDefaultValue());
        context.put(MetricParameter.TO_DATE.name(), MetricParameter.TO_DATE.getDefaultValue());
        context.put(PigScriptExecutor.LOG, file.getAbsolutePath());

        job = spy(new ActOnJob(null));
        doNothing().when(job).writeUserProfileAttributes(any(BufferedWriter.class), any(String.class));
    }

    @Test
    public void testPrepareFile() throws Exception {
        File jobFile = job.prepareFile(context);
        assertEquals(jobFile.getName(), ActOnJob.FILE_NAME);

        Set<String> content = read(jobFile);

        assertEquals(content.size(), 4);
        assertTrue(content.contains("email,firstName,lastName,phone,company,projects,builts,deployments,spentTime"));
        assertTrue(content.contains("2,0,0,15"));
        assertTrue(content.contains("1,2,1,30"));
        assertTrue(content.contains("0,1,1,20"));
    }


    private Set<String> read(File jobFile) throws IOException {
        Set<String> result = new HashSet<String>();

        BufferedReader reader = new BufferedReader(new FileReader(jobFile));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } finally {
            reader.close();
        }

        return result;
    }

    private File prepareLog(String date) throws IOException {
        List<Event> events = new ArrayList<Event>();

        // active users [user1, user2, user3]
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withTime("09:00:00").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withTime("09:00:00").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3").withTime("09:00:00").withDate(date).build());

        // projects created
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project1", "type1").withDate(date)
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project2", "type1").withDate(date)
                        .withTime("10:05:00")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project1", "type1").withDate(date)
                        .withTime("10:00:00")
                        .build());

        // projects built
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "project1", "type1").withTime("10:06:00")
                        .withDate(date)
                        .build());


        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1")
                        .withTime("10:10:00")
                        .withDate(date).build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2")
                        .withTime("10:00:00")
                        .withDate(date).build());

        return LogGenerator.generateLog(events);
    }
}
