/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestActOnJob {

    @Test
    public void testPrepareFile() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        File file = prepareLog(date);

        Map<String, String> context = Utils.initilizeContext(TimeUnit.DAY, new Date());
        context.put(PigScriptExecutor.LOG, file.getAbsolutePath());

        ActOnJob job = spy(new ActOnJob());
        doReturn(context).when(job).prepareContext();

        File jobFile = job.prepareFile();
        Set<String> content = read(jobFile);

        assertEquals(content.size(), 4);
        assertTrue(content.contains("email,projects,deployments"));
        assertTrue(content.contains("user1,2,0"));
        assertTrue(content.contains("user2,1,1"));
        assertTrue(content.contains("user3,0,1"));
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
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3").withDate(date).build());

        // projects created
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project1", "type1").withDate(date).build());
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project2", "type1").withDate(date).build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project1", "type1").withDate(date).build());

        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1").withDate(date).build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2").withDate(date).build());

        return LogGenerator.generateLog(events);
    }
}
