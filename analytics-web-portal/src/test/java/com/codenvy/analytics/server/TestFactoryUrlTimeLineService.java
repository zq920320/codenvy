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


package com.codenvy.analytics.server;

import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import com.codenvy.analytics.shared.TableData;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestFactoryUrlTimeLineService {

    @BeforeTest
    public void setUp() throws Exception {
        Map<String, String> context = Utils.initializeContext(TimeUnit.LIFETIME);
        context.put(PigScriptExecutor.LOG, preapreLogs().getAbsolutePath());
        Utils.putToDate(context, "20130210");
        Utils.putFromDate(context, "20130210");

        DataProcessing.calculateAndStore(MetricType.FACTORY_CREATED, context);
        DataProcessing.calculateAndStore(MetricType.TEMPORARY_WORKSPACE_CREATED, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_TYPES, context);
        DataProcessing.calculateAndStore(MetricType.PRODUCT_USAGE_TIME_FACTORY, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_PROJECT_IMPORTED, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_AND_BUILT, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_AND_DEPLOY, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_AND_RUN, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_URL_ACCEPTED, context);
    }

    @Test
    public void testRun() throws Exception {
        FactoryUrlTimeLineServiceImpl service = new FactoryUrlTimeLineServiceImpl();
        List<TableData> data = service.getData(TimeUnit.LIFETIME, Utils.newContext());

        assertEquals(data.get(0).get(1).get(0), "Factories created");
        assertEquals(data.get(0).get(1).get(1), "6");

        assertEquals(data.get(1).get(0).get(0), "Workspaces created");
        assertEquals(data.get(1).get(0).get(1), "9");

        assertEquals(data.get(1).get(2).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(2).get(1), "10");

        assertEquals(data.get(1).get(3).get(0), "Anonymous Sessions");
        assertEquals(data.get(1).get(3).get(1), "6");

        assertEquals(data.get(1).get(4).get(0), "Authenticated Sessions");
        assertEquals(data.get(1).get(4).get(1), "4");

        assertEquals(data.get(1).get(6).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(6).get(1), "10");

        assertEquals(data.get(1).get(7).get(0), "Abandoned Sessions");
        assertEquals(data.get(1).get(7).get(1), "8");

        assertEquals(data.get(1).get(8).get(0), "Converted Sessions");
        assertEquals(data.get(1).get(8).get(1), "2");

        assertEquals(data.get(1).get(10).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(10).get(1), "10");

        assertEquals(data.get(1).get(11).get(0), "% Built");
        assertEquals(data.get(1).get(11).get(1), "20%");

        assertEquals(data.get(1).get(12).get(0), "% Run");
        assertEquals(data.get(1).get(12).get(1), "");

        assertEquals(data.get(1).get(13).get(0), "% Deployed");
        assertEquals(data.get(1).get(13).get(1), "10%");

        assertEquals(data.get(1).get(15).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(15).get(1), "10");

        assertEquals(data.get(1).get(16).get(0), "< 10 Mins");
        assertEquals(data.get(1).get(16).get(1), "1");

        assertEquals(data.get(1).get(17).get(0), "> 10 Mins");
        assertEquals(data.get(1).get(17).get(1), "9");

        assertEquals(data.get(1).get(19).get(0), "Product Usage Mins");
        assertEquals(data.get(1).get(19).get(1), "275");
    }

    @Test
    public void testRunWithFilters() throws Exception {
        FactoryUrlTimeLineServiceImpl service = new FactoryUrlTimeLineServiceImpl();

        Map<String, String> filter = Utils.newContext();
        filter.put(MetricFilter.FILTER_WS.name(), "ws1");
        List<TableData> data = service.getData(TimeUnit.LIFETIME, filter);

        assertEquals(data.get(0).get(1).get(0), "Factories created");
        assertEquals(data.get(0).get(1).get(1), "2");

        assertEquals(data.get(1).get(0).get(0), "Workspaces created");
        assertEquals(data.get(1).get(0).get(1), "4");

        assertEquals(data.get(1).get(2).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(2).get(1), "10");

        assertEquals(data.get(1).get(3).get(0), "Anonymous Sessions");
        assertEquals(data.get(1).get(3).get(1), "6");

        assertEquals(data.get(1).get(4).get(0), "Authenticated Sessions");
        assertEquals(data.get(1).get(4).get(1), "4");

        assertEquals(data.get(1).get(6).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(6).get(1), "10");

        assertEquals(data.get(1).get(7).get(0), "Abandoned Sessions");
        assertEquals(data.get(1).get(7).get(1), "8");

        assertEquals(data.get(1).get(8).get(0), "Converted Sessions");
        assertEquals(data.get(1).get(8).get(1), "2");

        assertEquals(data.get(1).get(10).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(10).get(1), "10");

        assertEquals(data.get(1).get(11).get(0), "% Built");
        assertEquals(data.get(1).get(11).get(1), "20%");

        assertEquals(data.get(1).get(12).get(0), "% Run");
        assertEquals(data.get(1).get(12).get(1), "");

        assertEquals(data.get(1).get(13).get(0), "% Deployed");
        assertEquals(data.get(1).get(13).get(1), "10%");

        assertEquals(data.get(1).get(15).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(15).get(1), "10");

        assertEquals(data.get(1).get(16).get(0), "< 10 Mins");
        assertEquals(data.get(1).get(16).get(1), "1");

        assertEquals(data.get(1).get(17).get(0), "> 10 Mins");
        assertEquals(data.get(1).get(17).get(1), "9");

        assertEquals(data.get(1).get(19).get(0), "Product Usage Mins");
        assertEquals(data.get(1).get(19).get(1), "275");
    }

    private File preapreLogs() throws IOException {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1", "project1", "type1", "repo1", "factory1")
                        .withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user2", "project1", "type1", "repo1", "factory2")
                        .withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws2", "user3", "project1", "type1", "repo1", "factory3")
                        .withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws3", "user4", "project1", "type1", "repo1", "factory4")
                        .withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws4", "user5", "project1", "type1", "repo1", "factory5")
                        .withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws5", "user6", "project1", "type1", "repo1", "factory6")
                        .withDate("2013-02-10").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory1", "ref1").withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factory1", "ref1").withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factory2", "ref1").withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factory2", "ref1").withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-5", "factory3", "ref1").withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-7", "factory4", "ref1").withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-8", "factory5", "ref1").withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-9", "factory6", "ref1").withDate("2013-02-10").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user").withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user").withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-3", "user").withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-4", "user").withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-5", "user").withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-6", "user").withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-7", "user").withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-8", "user")
                        .withDate("2013-02-10").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-9", "user").withDate("2013-02-10").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-2", "user2", "true", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-3", "user3", "true", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id4", "tmp-4", "user4", "true", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id5", "tmp-5", "user5", "false", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id6", "tmp-6", "user6", "false", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id7", "tmp-7", "user7", "false", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id8", "tmp-8", "user8", "false", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id9", "tmp-9", "user9", "false", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id10", "tmp-10", "user10", "false", "brType", "brVer")
                        .withDate("2013-02-10").withTime("10:00:00").build());

        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:05:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-2", "user2")
                        .withDate("2013-02-10").withTime("10:10:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", "tmp-3", "user3")
                        .withDate("2013-02-10").withTime("10:15:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id4", "tmp-4", "user4")
                        .withDate("2013-02-10").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id5", "tmp-5", "user5")
                        .withDate("2013-02-10").withTime("10:25:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id6", "tmp-6", "user6")
                        .withDate("2013-02-10").withTime("10:30:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id7", "tmp-7", "user7")
                        .withDate("2013-02-10").withTime("10:35:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id8", "tmp-8", "user8")
                        .withDate("2013-02-10").withTime("10:40:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id9", "tmp-9", "user9")
                        .withDate("2013-02-10").withTime("10:45:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id10", "tmp-10", "user10")
                        .withDate("2013-02-10").withTime("10:50:00").build());

        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "", "project", "type")
                       .withDate("2013-02-10").withTime("10:01:00").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "tmp-3", "", "project", "type", "paas")
                        .withDate("2013-02-10").withTime("10:14:00").build());


        events.add(Event.Builder.createFactoryProjectImportedEvent("id1", "tmp-1", "project1", "type1")
                        .withDate("2013-02-10").build());
        events.add(Event.Builder.createFactoryProjectImportedEvent("id2", "tmp-2", "project2", "type2")
                        .withDate("2013-02-10").build());

        return LogGenerator.generateLog(events);
    }
}
