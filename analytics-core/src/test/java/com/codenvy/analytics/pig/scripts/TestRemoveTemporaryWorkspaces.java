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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Reshetnyak
 */
public class TestRemoveTemporaryWorkspaces extends BaseTest {

    private File log;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("tmp-1", "wsid2", "AnonymousUser_000001")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("tmp-2", "wsid3", "user2@gmail.com")
                                .withDate("2013-01-02").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("tmp-3", "wsid4", "AnonymousUser_000002")
                                .withDate("2013-01-02").withTime("10:00:00,000").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws2", "wsid5", "user3@gmail.com")
                                .withDate("2013-01-03").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("tmp-4", "wsid6", "AnonymousUser_000003")
                                .withDate("2013-01-03").withTime("10:00:00,000").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws3", "wsid7", "user4@gmail.com")
                                .withDate("2013-01-04").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("tmp-5", "wsid8", "AnonymousUser_000004")
                                .withDate("2013-01-04").withTime("10:00:00,000").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws4", "wsid9", "user5@gmail.com")
                                .withDate("2013-01-05").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("tmp-6", "wsid10", "AnonymousUser_000005")
                                .withDate("2013-01-05").withTime("10:00:00,000").build());

        log = LogGenerator.generateLog(events);

        compute("20130101");
        compute("20130102");
        compute("20130103");
        compute("20130104");
        compute("20130105");
    }

    private void compute(String date) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());
    }

    @Test(priority=1)
    public void test20130102() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.FROM_DATE, "20130102");
        collectionsManagement.removeTemporaryWorkspaces(builder.build(), 3);

        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST);
        ListValueData value = ValueDataUtil.getAsList(metric, Context.EMPTY);
        assertEquals(value.size(), 10);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");

        assertEquals(m.size(), 10);
        assertTrue(m.containsKey("wsid1"));
        assertTrue(m.containsKey("wsid2"));
        assertTrue(m.containsKey("wsid3"));
        assertTrue(m.containsKey("wsid4"));
        assertTrue(m.containsKey("wsid5"));
        assertTrue(m.containsKey("wsid6"));
        assertTrue(m.containsKey("wsid7"));
        assertTrue(m.containsKey("wsid8"));
        assertTrue(m.containsKey("wsid9"));
        assertTrue(m.containsKey("wsid10"));
    }

    @Test(priority=2)
    public void test20130104() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130104");
        builder.put(Parameters.FROM_DATE, "20130104");
        collectionsManagement.removeTemporaryWorkspaces(builder.build(), 3);

        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST);
        ListValueData value = ValueDataUtil.getAsList(metric, Context.EMPTY);
        assertEquals(value.size(), 9);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");

        assertEquals(m.size(), 9);
        assertTrue(m.containsKey("wsid1"));
        assertTrue(m.containsKey("wsid3"));
        assertTrue(m.containsKey("wsid4"));
        assertTrue(m.containsKey("wsid5"));
        assertTrue(m.containsKey("wsid6"));
        assertTrue(m.containsKey("wsid7"));
        assertTrue(m.containsKey("wsid8"));
        assertTrue(m.containsKey("wsid9"));
        assertTrue(m.containsKey("wsid10"));
    }

    @Test(priority=3)
    public void test20130105() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130105");
        builder.put(Parameters.FROM_DATE, "20130105");
        collectionsManagement.removeTemporaryWorkspaces(builder.build(), 3);

        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST);
        ListValueData value = ValueDataUtil.getAsList(metric, Context.EMPTY);
        assertEquals(value.size(), 7);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");

        assertEquals(m.size(), 7);
        assertTrue(m.containsKey("wsid1"));
        assertTrue(m.containsKey("wsid5"));
        assertTrue(m.containsKey("wsid6"));
        assertTrue(m.containsKey("wsid7"));
        assertTrue(m.containsKey("wsid8"));
        assertTrue(m.containsKey("wsid9"));
        assertTrue(m.containsKey("wsid10"));
    }
}