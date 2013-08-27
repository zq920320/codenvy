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
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import com.codenvy.analytics.shared.TableData;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestFactoryUrl {

    private String date;

    @BeforeTest
    public void setUp() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        date = dateFormat.format(calendar.getTime());


        Map<String, String> context = Utils.initializeContext(TimeUnit.LIFETIME);
        MetricParameter.LOG.put(context, prepareLogs().getAbsolutePath());
        MetricParameter.TO_DATE.putDefaultValue(context);
        MetricParameter.FROM_DATE.put(context, MetricParameter.TO_DATE.getDefaultValue());

        DataProcessing.calculateAndStore(MetricType.FACTORY_CREATED, context);
        DataProcessing.calculateAndStore(MetricType.TEMPORARY_WORKSPACE_CREATED, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_TYPES, context);
        DataProcessing.calculateAndStore(MetricType.PRODUCT_USAGE_SESSIONS_FACTORY, context);
        DataProcessing.calculateAndStore(MetricType.PRODUCT_USAGE_TIME_FACTORY, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_PROJECT_IMPORTED, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_AND_BUILT, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_AND_DEPLOY, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_SESSIONS_AND_RUN, context);
        DataProcessing.calculateAndStore(MetricType.FACTORY_URL_ACCEPTED, context);
        DataProcessing.calculateAndStore(MetricType.ACTIVE_FACTORY_SET, context);
    }

    @Test
    public void testRun() throws Exception {
        FactoryUrlTimeLineServiceImpl service = new FactoryUrlTimeLineServiceImpl();
        List<TableData> data = service.getData(TimeUnit.LIFETIME, Utils.newContext());

        assertEquals(data.get(0).get(1).get(0), "Factories Created");
        assertEquals(data.get(0).get(1).get(1), "6");

        assertEquals(data.get(1).get(0).get(0), "Workspaces Created");
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
        assertEquals(data.get(1).get(19).get(1), "276");
    }

    @Test
    public void testRunWithFilters() throws Exception {
        FactoryUrlTimeLineServiceImpl service = new FactoryUrlTimeLineServiceImpl();

        Map<String, String> filter = Utils.newContext();
        MetricFilter.WS.put(filter, "ws1");
        List<TableData> data = service.getData(TimeUnit.LIFETIME, filter);

        assertEquals(data.get(0).get(1).get(0), "Factories Created");
        assertEquals(data.get(0).get(1).get(1), "2");

        assertEquals(data.get(1).get(0).get(0), "Workspaces Created");
        assertEquals(data.get(1).get(0).get(1), "4");

        assertEquals(data.get(1).get(2).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(2).get(1), "4");

        assertEquals(data.get(1).get(3).get(0), "Anonymous Sessions");
        assertEquals(data.get(1).get(3).get(1), "");

        assertEquals(data.get(1).get(4).get(0), "Authenticated Sessions");
        assertEquals(data.get(1).get(4).get(1), "4");

        assertEquals(data.get(1).get(6).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(6).get(1), "4");

        assertEquals(data.get(1).get(7).get(0), "Abandoned Sessions");
        assertEquals(data.get(1).get(7).get(1), "2");

        assertEquals(data.get(1).get(8).get(0), "Converted Sessions");
        assertEquals(data.get(1).get(8).get(1), "2");

        assertEquals(data.get(1).get(10).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(10).get(1), "4");

        assertEquals(data.get(1).get(11).get(0), "% Built");
        assertEquals(data.get(1).get(11).get(1), "50%");

        assertEquals(data.get(1).get(12).get(0), "% Run");
        assertEquals(data.get(1).get(12).get(1), "");

        assertEquals(data.get(1).get(13).get(0), "% Deployed");
        assertEquals(data.get(1).get(13).get(1), "25%");

        assertEquals(data.get(1).get(15).get(0), "Factory Sessions");
        assertEquals(data.get(1).get(15).get(1), "4");

        assertEquals(data.get(1).get(16).get(0), "< 10 Mins");
        assertEquals(data.get(1).get(16).get(1), "1");

        assertEquals(data.get(1).get(17).get(0), "> 10 Mins");
        assertEquals(data.get(1).get(17).get(1), "3");

        assertEquals(data.get(1).get(19).get(0), "Product Usage Mins");
        assertEquals(data.get(1).get(19).get(1), "51");
    }

    @Test
    public void testTopFactories() throws Exception {
        FactoryUrlTopFactoriesServiceImpl service = new FactoryUrlTopFactoriesServiceImpl();
        List<TableData> data = service.getData(Utils.newContext());

        assertEquals(data.size(), 7);
        assertEquals(data.get(0).size(), 7);

        assertEquals(data.get(0).get(0).size(), 13);
        assertEquals(data.get(0).get(1).size(), 13);
        assertEquals(data.get(0).get(2).size(), 13);
        assertEquals(data.get(0).get(3).size(), 13);
        assertEquals(data.get(0).get(4).size(), 13);
        assertEquals(data.get(0).get(5).size(), 13);
        assertEquals(data.get(0).get(6).size(), 13);

        for (int i = 0; i < 7; i++) {
            assertEquals(data.get(i).get(0).get(0), "Factory");
            assertEquals(data.get(i).get(1).get(0), "factory6");
            assertEquals(data.get(i).get(2).get(0), "factory5");
            assertEquals(data.get(i).get(3).get(0), "factory2");
            assertEquals(data.get(i).get(4).get(0), "factory4");
            assertEquals(data.get(i).get(5).get(0), "factory3");
            assertEquals(data.get(i).get(6).get(0), "factory1");

            assertEquals(data.get(i).get(0).get(1), "Workspace Creations");
            assertEquals(data.get(i).get(1).get(1), "1");
            assertEquals(data.get(i).get(2).get(1), "1");
            assertEquals(data.get(i).get(3).get(1), "2");
            assertEquals(data.get(i).get(4).get(1), "1");
            assertEquals(data.get(i).get(5).get(1), "1");
            assertEquals(data.get(i).get(6).get(1), "2");

            assertEquals(data.get(i).get(0).get(2), "Sessions");
            assertEquals(data.get(i).get(1).get(2), "1");
            assertEquals(data.get(i).get(2).get(2), "1");
            assertEquals(data.get(i).get(3).get(2), "2");
            assertEquals(data.get(i).get(4).get(2), "1");
            assertEquals(data.get(i).get(5).get(2), "1");
            assertEquals(data.get(i).get(6).get(2), "2");

            assertEquals(data.get(i).get(0).get(3), "% Anon");
            assertEquals(data.get(i).get(1).get(3), "100.0");
            assertEquals(data.get(i).get(2).get(3), "100.0");
            assertEquals(data.get(i).get(3).get(3), "0.0");
            assertEquals(data.get(i).get(4).get(3), "100.0");
            assertEquals(data.get(i).get(5).get(3), "100.0");
            assertEquals(data.get(i).get(6).get(3), "0.0");

            assertEquals(data.get(i).get(0).get(4), "% Auth");
            assertEquals(data.get(i).get(1).get(4), "0.0");
            assertEquals(data.get(i).get(2).get(4), "0.0");
            assertEquals(data.get(i).get(3).get(4), "100.0");
            assertEquals(data.get(i).get(4).get(4), "0.0");
            assertEquals(data.get(i).get(5).get(4), "0.0");
            assertEquals(data.get(i).get(6).get(4), "100.0");

            assertEquals(data.get(i).get(0).get(5), "% Abandon");
            assertEquals(data.get(i).get(1).get(5), "100.0");
            assertEquals(data.get(i).get(2).get(5), "100.0");
            assertEquals(data.get(i).get(3).get(5), "100.0");
            assertEquals(data.get(i).get(4).get(5), "100.0");
            assertEquals(data.get(i).get(5).get(5), "100.0");
            assertEquals(data.get(i).get(6).get(5), "0.0");

            assertEquals(data.get(i).get(0).get(6), "% Convert");
            assertEquals(data.get(i).get(1).get(6), "0.0");
            assertEquals(data.get(i).get(2).get(6), "0.0");
            assertEquals(data.get(i).get(3).get(6), "0.0");
            assertEquals(data.get(i).get(4).get(6), "0.0");
            assertEquals(data.get(i).get(5).get(6), "0.0");
            assertEquals(data.get(i).get(6).get(6), "100.0");

            assertEquals(data.get(i).get(0).get(7), "% Build");
            assertEquals(data.get(i).get(1).get(7), "0.0");
            assertEquals(data.get(i).get(2).get(7), "0.0");
            assertEquals(data.get(i).get(3).get(7), "50.0");
            assertEquals(data.get(i).get(4).get(7), "0.0");
            assertEquals(data.get(i).get(5).get(7), "0.0");
            assertEquals(data.get(i).get(6).get(7), "50.0");

            assertEquals(data.get(i).get(0).get(8), "% Run");
            assertEquals(data.get(i).get(1).get(8), "0.0");
            assertEquals(data.get(i).get(2).get(8), "0.0");
            assertEquals(data.get(i).get(3).get(8), "0.0");
            assertEquals(data.get(i).get(4).get(8), "0.0");
            assertEquals(data.get(i).get(5).get(8), "0.0");
            assertEquals(data.get(i).get(6).get(8), "0.0");

            assertEquals(data.get(i).get(0).get(9), "% Deployed");
            assertEquals(data.get(i).get(1).get(9), "0.0");
            assertEquals(data.get(i).get(2).get(9), "0.0");
            assertEquals(data.get(i).get(3).get(9), "50.0");
            assertEquals(data.get(i).get(4).get(9), "0.0");
            assertEquals(data.get(i).get(5).get(9), "0.0");
            assertEquals(data.get(i).get(6).get(9), "0.0");

            assertEquals(data.get(i).get(0).get(10), "Mins");
            assertEquals(data.get(i).get(1).get(10), "45");
            assertEquals(data.get(i).get(2).get(10), "40");
            assertEquals(data.get(i).get(3).get(10), "36");
            assertEquals(data.get(i).get(4).get(10), "35");
            assertEquals(data.get(i).get(5).get(10), "25");
            assertEquals(data.get(i).get(6).get(10), "15");

            assertEquals(data.get(i).get(0).get(11), "First Session");
            assertTrue(data.get(i).get(1).get(11).contains("18:00:00") && data.get(i).get(1).get(11).contains(date));
            assertTrue(data.get(i).get(2).get(11).contains("17:00:00") && data.get(i).get(2).get(11).contains(date));
            assertTrue(data.get(i).get(3).get(11).contains("12:00:00") && data.get(i).get(3).get(11).contains(date));
            assertTrue(data.get(i).get(4).get(11).contains("16:00:00") && data.get(i).get(4).get(11).contains(date));
            assertTrue(data.get(i).get(5).get(11).contains("14:00:00") && data.get(i).get(5).get(11).contains(date));
            assertTrue(data.get(i).get(6).get(11).contains("10:00:00") && data.get(i).get(6).get(11).contains(date));

            assertEquals(data.get(i).get(0).get(12), "Last Session");
            assertTrue(data.get(i).get(1).get(12).contains("18:00:00") && data.get(i).get(1).get(12).contains(date));
            assertTrue(data.get(i).get(2).get(12).contains("17:00:00") && data.get(i).get(2).get(12).contains(date));
            assertTrue(data.get(i).get(3).get(12).contains("13:00:00") && data.get(i).get(3).get(12).contains(date));
            assertTrue(data.get(i).get(4).get(12).contains("16:00:00") && data.get(i).get(4).get(12).contains(date));
            assertTrue(data.get(i).get(5).get(12).contains("14:00:00") && data.get(i).get(5).get(12).contains(date));
            assertTrue(data.get(i).get(6).get(12).contains("11:00:00") && data.get(i).get(6).get(12).contains(date));
        }
    }

    @Test
    public void testTopFactoriesWithWsFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.WS.put(context, "ws1,ws2");

        FactoryUrlTopFactoriesServiceImpl service = new FactoryUrlTopFactoriesServiceImpl();
        List<TableData> data = service.getData(context);

        assertEquals(data.size(), 7);
        assertEquals(data.get(0).size(), 4);

        assertEquals(data.get(0).get(0).size(), 13);
        assertEquals(data.get(0).get(1).size(), 13);
        assertEquals(data.get(0).get(2).size(), 13);
        assertEquals(data.get(0).get(3).size(), 13);

        for (int i = 0; i < 7; i++) {
            assertEquals(data.get(i).get(0).get(0), "Factory");
            assertEquals(data.get(i).get(1).get(0), "factory2");
            assertEquals(data.get(i).get(2).get(0), "factory3");
            assertEquals(data.get(i).get(3).get(0), "factory1");

            assertEquals(data.get(i).get(0).get(1), "Workspace Creations");
            assertEquals(data.get(i).get(1).get(1), "2");
            assertEquals(data.get(i).get(2).get(1), "1");
            assertEquals(data.get(i).get(3).get(1), "2");

            assertEquals(data.get(i).get(0).get(2), "Sessions");
            assertEquals(data.get(i).get(1).get(2), "2");
            assertEquals(data.get(i).get(2).get(2), "1");
            assertEquals(data.get(i).get(3).get(2), "2");

            assertEquals(data.get(i).get(0).get(3), "% Anon");
            assertEquals(data.get(i).get(1).get(3), "0.0");
            assertEquals(data.get(i).get(2).get(3), "100.0");
            assertEquals(data.get(i).get(3).get(3), "0.0");

            assertEquals(data.get(i).get(0).get(4), "% Auth");
            assertEquals(data.get(i).get(1).get(4), "100.0");
            assertEquals(data.get(i).get(2).get(4), "0.0");
            assertEquals(data.get(i).get(3).get(4), "100.0");

            assertEquals(data.get(i).get(0).get(5), "% Abandon");
            assertEquals(data.get(i).get(1).get(5), "100.0");
            assertEquals(data.get(i).get(2).get(5), "100.0");
            assertEquals(data.get(i).get(3).get(5), "0.0");

            assertEquals(data.get(i).get(0).get(6), "% Convert");
            assertEquals(data.get(i).get(1).get(6), "0.0");
            assertEquals(data.get(i).get(2).get(6), "0.0");
            assertEquals(data.get(i).get(3).get(6), "100.0");

            assertEquals(data.get(i).get(0).get(7), "% Build");
            assertEquals(data.get(i).get(1).get(7), "50.0");
            assertEquals(data.get(i).get(2).get(7), "0.0");
            assertEquals(data.get(i).get(3).get(7), "50.0");

            assertEquals(data.get(i).get(0).get(8), "% Run");
            assertEquals(data.get(i).get(1).get(8), "0.0");
            assertEquals(data.get(i).get(2).get(8), "0.0");
            assertEquals(data.get(i).get(3).get(8), "0.0");

            assertEquals(data.get(i).get(0).get(9), "% Deployed");
            assertEquals(data.get(i).get(1).get(9), "50.0");
            assertEquals(data.get(i).get(2).get(9), "0.0");
            assertEquals(data.get(i).get(3).get(9), "0.0");

            assertEquals(data.get(i).get(0).get(10), "Mins");
            assertEquals(data.get(i).get(1).get(10), "36");
            assertEquals(data.get(i).get(2).get(10), "25");
            assertEquals(data.get(i).get(3).get(10), "15");

            assertEquals(data.get(i).get(0).get(11), "First Session");
            assertTrue(data.get(i).get(1).get(11).contains("12:00:00") && data.get(i).get(1).get(11).contains(date));
            assertTrue(data.get(i).get(2).get(11).contains("14:00:00") && data.get(i).get(2).get(11).contains(date));
            assertTrue(data.get(i).get(3).get(11).contains("10:00:00") && data.get(i).get(3).get(11).contains(date));

            assertEquals(data.get(i).get(0).get(12), "Last Session");
            assertTrue(data.get(i).get(1).get(12).contains("13:00:00") && data.get(i).get(1).get(12).contains(date));
            assertTrue(data.get(i).get(2).get(12).contains("14:00:00") && data.get(i).get(2).get(12).contains(date));
            assertTrue(data.get(i).get(3).get(12).contains("11:00:00") && data.get(i).get(3).get(12).contains(date));
        }
    }

    @Test
    public void testTopFactoriesWithFactoryFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.FACTORY_URL.put(context, "factory2,factory3");

        FactoryUrlTopFactoriesServiceImpl service = new FactoryUrlTopFactoriesServiceImpl();
        List<TableData> data = service.getData(context);

        assertEquals(data.size(), 7);
        assertEquals(data.get(0).size(), 3);

        assertEquals(data.get(0).get(0).size(), 13);
        assertEquals(data.get(0).get(1).size(), 13);
        assertEquals(data.get(0).get(2).size(), 13);

        for (int i = 0; i < 7; i++) {
            assertEquals(data.get(i).get(0).get(0), "Factory");
            assertEquals(data.get(i).get(1).get(0), "factory2");
            assertEquals(data.get(i).get(2).get(0), "factory3");

            assertEquals(data.get(i).get(0).get(1), "Workspace Creations");
            assertEquals(data.get(i).get(1).get(1), "2");
            assertEquals(data.get(i).get(2).get(1), "1");

            assertEquals(data.get(i).get(0).get(2), "Sessions");
            assertEquals(data.get(i).get(1).get(2), "2");
            assertEquals(data.get(i).get(2).get(2), "1");

            assertEquals(data.get(i).get(0).get(3), "% Anon");
            assertEquals(data.get(i).get(1).get(3), "0.0");
            assertEquals(data.get(i).get(2).get(3), "100.0");

            assertEquals(data.get(i).get(0).get(4), "% Auth");
            assertEquals(data.get(i).get(1).get(4), "100.0");
            assertEquals(data.get(i).get(2).get(4), "0.0");

            assertEquals(data.get(i).get(0).get(5), "% Abandon");
            assertEquals(data.get(i).get(1).get(5), "100.0");
            assertEquals(data.get(i).get(2).get(5), "100.0");

            assertEquals(data.get(i).get(0).get(6), "% Convert");
            assertEquals(data.get(i).get(1).get(6), "0.0");
            assertEquals(data.get(i).get(2).get(6), "0.0");

            assertEquals(data.get(i).get(0).get(7), "% Build");
            assertEquals(data.get(i).get(1).get(7), "50.0");
            assertEquals(data.get(i).get(2).get(7), "0.0");

            assertEquals(data.get(i).get(0).get(8), "% Run");
            assertEquals(data.get(i).get(1).get(8), "0.0");
            assertEquals(data.get(i).get(2).get(8), "0.0");

            assertEquals(data.get(i).get(0).get(9), "% Deployed");
            assertEquals(data.get(i).get(1).get(9), "50.0");
            assertEquals(data.get(i).get(2).get(9), "0.0");

            assertEquals(data.get(i).get(0).get(10), "Mins");
            assertEquals(data.get(i).get(1).get(10), "36");
            assertEquals(data.get(i).get(2).get(10), "25");

            assertEquals(data.get(i).get(0).get(11), "First Session");
            assertTrue(data.get(i).get(1).get(11).contains("12:00:00") && data.get(i).get(1).get(11).contains(date));
            assertTrue(data.get(i).get(2).get(11).contains("14:00:00") && data.get(i).get(2).get(11).contains(date));

            assertEquals(data.get(i).get(0).get(12), "Last Session");
            assertTrue(data.get(i).get(1).get(12).contains("13:00:00") && data.get(i).get(1).get(12).contains(date));
            assertTrue(data.get(i).get(2).get(12).contains("14:00:00") && data.get(i).get(2).get(12).contains(date));
        }
    }


    private File prepareLogs() throws IOException {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1", "project1", "type1", "repo1", "factory1")
                        .withDate(date).build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user2", "project1", "type1", "repo1", "factory2")
                        .withDate(date).build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws2", "user3", "project1", "type1", "repo1", "factory3")
                        .withDate(date).build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws3", "user4", "project1", "type1", "repo1", "factory4")
                        .withDate(date).build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws4", "user5", "project1", "type1", "repo1", "factory5")
                        .withDate(date).build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws5", "user6", "project1", "type1", "repo1", "factory6")
                        .withDate(date).build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory1", "ref1").withDate(date)
                        .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factory1", "ref1").withDate(date)
                        .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factory2", "ref1").withDate(date)
                        .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factory2", "ref1").withDate(date)
                        .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-5", "factory3", "ref1").withDate(date)
                        .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-7", "factory4", "ref1").withDate(date)
                        .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-8", "factory5", "ref1").withDate(date)
                        .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-9", "factory6", "ref1").withDate(date)
                        .build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-3", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-4", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-5", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-6", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-7", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-8", "user").withDate(date).build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-9", "user").withDate(date).build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType", "brVer")
                        .withDate(date).withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-2", "user2", "true", "brType", "brVer")
                        .withDate(date).withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-3", "user3", "true", "brType", "brVer")
                        .withDate(date).withTime("12:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id4", "tmp-4", "user4", "true", "brType", "brVer")
                        .withDate(date).withTime("13:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id5", "tmp-5", "user5", "false", "brType", "brVer")
                        .withDate(date).withTime("14:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id6", "tmp-6", "user6", "false", "brType", "brVer")
                        .withDate(date).withTime("15:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id7", "tmp-7", "user7", "false", "brType", "brVer")
                        .withDate(date).withTime("16:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id8", "tmp-8", "user8", "false", "brType", "brVer")
                        .withDate(date).withTime("17:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id9", "tmp-9", "user9", "false", "brType", "brVer")
                        .withDate(date).withTime("18:00:00").build());
        events.add(
                Event.Builder.createSessionFactoryStartedEvent("id10", "tmp-10", "user10", "false", "brType", "brVer")
                     .withDate(date).withTime("19:00:00").build());

        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                        .withDate(date).withTime("10:05:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-2", "user2")
                        .withDate(date).withTime("11:10:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", "tmp-3", "user3")
                        .withDate(date).withTime("12:15:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id4", "tmp-4", "user4")
                        .withDate(date).withTime("13:21:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id5", "tmp-5", "user5")
                        .withDate(date).withTime("14:25:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id6", "tmp-6", "user6")
                        .withDate(date).withTime("15:30:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id7", "tmp-7", "user7")
                        .withDate(date).withTime("16:35:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id8", "tmp-8", "user8")
                        .withDate(date).withTime("17:40:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id9", "tmp-9", "user9")
                        .withDate(date).withTime("18:45:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id10", "tmp-10", "user10")
                        .withDate(date).withTime("19:50:00").build());

        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "", "project", "type")
                        .withDate(date).withTime("10:01:00").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "tmp-3", "", "project", "type", "paas")
                        .withDate(date).withTime("12:14:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project1", "type1")
                        .withDate(date).build());
        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-2", "user2", "project2", "type2")
                        .withDate(date).build());

        return LogGenerator.generateLog(events);
    }
}
