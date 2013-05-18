/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.ldap.ReadOnlyUserManager;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import com.codenvy.organization.client.UserManager;
import com.codenvy.organization.exception.OrganizationServiceException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestActOnJob {

    private ActOnJob job;

    @BeforeMethod
    private void setUp() throws IOException, OrganizationServiceException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        File file = prepareLog(date);

        Map<String, String> context = Utils.initializeContext(TimeUnit.DAY, new Date());
        context.put(PigScriptExecutor.LOG, file.getAbsolutePath());

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("firstName", "Chuck");
        attributes.put("lastName", "Norris");
        attributes.put("phone", "00000000");
        attributes.put("employer", "Eath");
        
        UserManager userManager = mock(UserManager.class);

        Properties properties = new Properties();
        properties.put("user-profile-attributes", "email,firstName,lastName,phone,employer");
        properties.put("user-profile-headers", "email,firstName,lastName,phone,company");
        properties.put("metric-names", "PROJECTS_CREATED_NUMBER,PROJECTS_BUILT_NUMBER,APP_DEPLOYED_NUMBER,PRODUCT_USAGE_TIME_TOTAL");
        properties.put("metric-headers", "projects,builts,deployments,spentTime");

        ReadOnlyUserManager readOnlyUserManager = spy(new ReadOnlyUserManager(userManager));
        doReturn(attributes).when(readOnlyUserManager).getUserAttributes(anyString());

        job = spy(new ActOnJob(readOnlyUserManager, properties));
        doReturn(context).when(job).initilalizeContext();
    }

    @Test
    public void testPrepareFile() throws Exception {
        File jobFile = job.prepareFile();
        Set<String> content = read(jobFile);

        assertEquals(content.size(), 4);
        assertTrue(content.contains("email,firstName,lastName,phone,company,projects,builts,deployments,spentTime"));
        assertTrue(content.contains("\"user1\",\"Chuck\",\"Norris\",\"00000000\",\"Eath\",2,0,0,5"));
        assertTrue(content.contains("\"user2\",\"Chuck\",\"Norris\",\"00000000\",\"Eath\",1,2,1,10"));
        assertTrue(content.contains("\"user3\",\"Chuck\",\"Norris\",\"00000000\",\"Eath\",0,1,1,0"));
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
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project1", "type1").withDate(date).withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project2", "type1").withDate(date).withTime("10:05:00")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project1", "type1").withDate(date).withTime("10:00:00")
                                .build());

        // projects built
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "project1", "type1").withTime("10:06:00").withDate(date)
                                .build());


        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1").withTime("10:10:00")
                                .withDate(date).build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2").withTime("10:00:00")
                                .withDate(date).build());

        return LogGenerator.generateLog(events);
    }
}
