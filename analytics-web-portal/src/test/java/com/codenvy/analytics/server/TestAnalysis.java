/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.client.AnalysisViewService;
import com.codenvy.analytics.metrics.InitialValueContainer;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import com.codenvy.analytics.shared.TimeLineViewData;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestAnalysis {

    private final String LOG_DIR = "target" + File.separator + UUID.randomUUID().toString();

    @BeforeTest
    public void setUp() throws Exception {
        File initValues = prepareInitValues();
        System.setProperty(InitialValueContainer.ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY, initValues.getPath());

        prepareLog();
        System.setProperty(PigScriptExecutor.ANALYTICS_LOGS_DIRECTORY_PROPERTY, LOG_DIR);
    }

    @Test
    public void testPrepareFile() throws Exception {
        AnalysisViewService service = new AnalysisViewServiceImpl();
        List<TimeLineViewData> data = service.getData();

        TimeLineViewData timeLineViewData = data.get(0);
        assertEquals(timeLineViewData.get(1).get(1), "12"); // total accounts
        assertEquals(timeLineViewData.get(2).get(1), "3"); // created
        assertEquals(timeLineViewData.get(3).get(1), "2"); // built
        assertEquals(timeLineViewData.get(4).get(1), "2"); // deploy
        assertEquals(timeLineViewData.get(5).get(1), "1"); // deploy local and paas
        assertEquals(timeLineViewData.get(6).get(1), ""); // invites
    }

    private File prepareInitValues() throws Exception {
        final String content = "<metrics>" +
                               "  <metric type=\"TOTAL_USERS_NUMBER\">" +
                               "     <initial-value fromDate=\"20091104\" toDate=\"20091104\">10</initial-value>" +
                               "  </metric>" +
                               "</metrics>";

        File initValues = new File("target", "initial-values.xml");

        BufferedWriter writer = new BufferedWriter(new FileWriter(initValues));
        writer.write(content);
        writer.close();

        return initValues;
    }

    private void prepareLog() throws IOException {
        List<Event> events = new ArrayList<Event>();

        // users created
        events.add(Event.Builder.createUserCreatedEvent("userid1", "user1").withDate("2013-04-20")
                                .build());
        events.add(Event.Builder.createUserCreatedEvent("userid2", "user2").withDate("2013-04-20")
                                .build());

        // projects created
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "project1", "type1").withDate("2013-04-20")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "", "project2", "type1").withDate("2013-04-20")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "", "project1", "type1").withDate("2013-04-20")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws2", "", "project1", "type1").withDate("2013-04-20")
                                .build());

        // projects built
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "project1", "type1").withDate("2013-04-20")
                                .build());


        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws2", "", "project1", "type1", "paas1")
                                .withDate("2013-04-20").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws2", "", "project1", "type1", "paas2")
                                .withDate("2013-04-20").build());

        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws2", "", "project1", "type1", "local")
                                .withDate("2013-04-20").build());

        LogGenerator.generateLog(events, LOG_DIR, "2013", "04", "20");
    }
}
