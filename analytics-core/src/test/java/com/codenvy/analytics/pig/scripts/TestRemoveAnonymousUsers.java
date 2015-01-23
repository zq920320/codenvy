/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
public class TestRemoveAnonymousUsers extends BaseTest {

    private File log;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "AnonymousUser_000001", "AnonymousUser_000001")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());

        events.add(Event.Builder.createUserCreatedEvent("uid3", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-01-02").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid4", "AnonymousUser_000002", "AnonymousUser_000002")
                                .withDate("2013-01-02").withTime("10:00:00,000").build());

        events.add(Event.Builder.createUserCreatedEvent("uid5", "user3@gmail.com", "user3@gmail.com")
                                .withDate("2013-01-03").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid6", "AnonymousUser_000003", "AnonymousUser_000003")
                                .withDate("2013-01-03").withTime("10:00:00,000").build());

        events.add(Event.Builder.createUserCreatedEvent("uid7", "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-01-04").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid8", "AnonymousUser_000004", "AnonymousUser_000004")
                                .withDate("2013-01-04").withTime("10:00:00,000").build());

        events.add(Event.Builder.createUserCreatedEvent("uid9", "user5@gmail.com", "user5@gmail.com")
                                .withDate("2013-01-05").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid10", "AnonymousUser_000005", "AnonymousUser_000005")
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
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());
    }

    @Test(priority=1)
    public void test20130102() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.FROM_DATE, "20130102");
        collectionsManagement.removeAnonymousUsers(builder.build(), 3);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        ListValueData value = ValueDataUtil.getAsList(metric, Context.EMPTY);
        assertEquals(value.size(), 10);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");

        assertEquals(m.size(), 10);
        assertTrue(m.containsKey("uid1"));
        assertTrue(m.containsKey("uid2"));
        assertTrue(m.containsKey("uid3"));
        assertTrue(m.containsKey("uid4"));
        assertTrue(m.containsKey("uid5"));
        assertTrue(m.containsKey("uid6"));
        assertTrue(m.containsKey("uid7"));
        assertTrue(m.containsKey("uid8"));
        assertTrue(m.containsKey("uid9"));
        assertTrue(m.containsKey("uid10"));
    }

    @Test(priority=2)
    public void test20130104() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130104");
        builder.put(Parameters.FROM_DATE, "20130104");
        collectionsManagement.removeAnonymousUsers(builder.build(), 3);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        ListValueData value = ValueDataUtil.getAsList(metric, Context.EMPTY);
        assertEquals(value.size(), 9);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");

        assertEquals(m.size(), 9);
        assertTrue(m.containsKey("uid1"));
        assertTrue(m.containsKey("uid3"));
        assertTrue(m.containsKey("uid4"));
        assertTrue(m.containsKey("uid5"));
        assertTrue(m.containsKey("uid6"));
        assertTrue(m.containsKey("uid7"));
        assertTrue(m.containsKey("uid8"));
        assertTrue(m.containsKey("uid9"));
        assertTrue(m.containsKey("uid10"));
    }

    @Test(priority=3)
    public void test20130105() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130105");
        builder.put(Parameters.FROM_DATE, "20130105");
        collectionsManagement.removeAnonymousUsers(builder.build(), 3);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        ListValueData value = ValueDataUtil.getAsList(metric, Context.EMPTY);
        assertEquals(value.size(), 8);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");

        assertEquals(m.size(), 8);
        assertTrue(m.containsKey("uid1"));
        assertTrue(m.containsKey("uid3"));
        assertTrue(m.containsKey("uid5"));
        assertTrue(m.containsKey("uid6"));
        assertTrue(m.containsKey("uid7"));
        assertTrue(m.containsKey("uid8"));
        assertTrue(m.containsKey("uid9"));
        assertTrue(m.containsKey("uid10"));
    }
}
