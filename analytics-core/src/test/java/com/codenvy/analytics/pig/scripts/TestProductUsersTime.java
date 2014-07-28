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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageCondition;
import com.codenvy.analytics.metrics.sessions.AbstractTimelineProductUsageCondition;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsersTime extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com","[user1@gmail.com]").withDate("2013-11-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "user2@gmail.com","[user2@gmail.com]").withDate("2013-11-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid3", "user3@gmail.com","[user3@gmail.com]").withDate("2013-11-01").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1@gmail.com").withDate("2013-11-01").build());

        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                                .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                                .withTime("20:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                                .withTime("21:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                                .withTime("21:03:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user2@gmail.com", "ws1", "ide", "3").withDate("2013-11-01")
                                .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2@gmail.com", "ws1", "ide", "3").withDate("2013-11-01")
                                .withTime("20:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user3@gmail.com", "ws1", "ide", "4").withDate("2013-11-01")
                                .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user3@gmail.com", "ws1", "ide", "4").withDate("2013-11-01")
                                .withTime("20:07:00").build());
        File log = LogGenerator.generateLog(events);


        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void testProductUsersTime() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.SORT, "-time");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USERS_TIME);
        ListValueData value = (ListValueData)metric.getValue(builder.build());

        List<ValueData> all = value.getAll();
        MapValueData valueData = (MapValueData)all.get(0);
        assertEquals(valueData.getAll().get("user").getAsString(), "uid1");
        assertEquals(valueData.getAll().get("time").getAsString(), "480000");
        assertEquals(valueData.getAll().get("sessions").getAsString(), "2");

        valueData = (MapValueData)all.get(1);
        assertEquals(valueData.getAll().get("user").getAsString(), "uid3");
        assertEquals(valueData.getAll().get("time").getAsString(), "420000");
        assertEquals(valueData.getAll().get("sessions").getAsString(), "1");

        valueData = (MapValueData)all.get(2);
        assertEquals(valueData.getAll().get("user").getAsString(), "uid2");
        assertEquals(valueData.getAll().get("time").getAsString(), "60000");
        assertEquals(valueData.getAll().get("sessions").getAsString(), "1");
    }

    @Test
    public void testTopEntities() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.PASSED_DAYS_COUNT, "by_1_day");

        Metric metric = MetricFactory.getMetric(MetricType.TOP_USERS);
        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 3);
        MapValueData item = (MapValueData)value.getAll().get(0);
        assertEquals(item.getAll().get("entity").getAsString(), "uid1");
        assertEquals(item.getAll().get("sessions").getAsString(), "2");
        assertEquals(item.getAll().get("by_1_day").getAsString(), "480000");
        assertEquals(item.getAll().get("by_lifetime").getAsString(), "480000");

        item = (MapValueData)value.getAll().get(1);
        assertEquals(item.getAll().get("entity").getAsString(), "uid3");
        assertEquals(item.getAll().get("sessions").getAsString(), "1");
        assertEquals(item.getAll().get("by_1_day").getAsString(), "420000");
        assertEquals(item.getAll().get("by_lifetime").getAsString(), "420000");

        item = (MapValueData)value.getAll().get(2);
        assertEquals(item.getAll().get("entity").getAsString(), "uid2");
        assertEquals(item.getAll().get("sessions").getAsString(), "1");
        assertEquals(item.getAll().get("by_1_day").getAsString(), "60000");
        assertEquals(item.getAll().get("by_lifetime").getAsString(), "60000");
    }

    @Test
    public void testConditions1() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestedAbstractProductUsageCondition(0, 450000, true, true, "$and", 0, 2, true, true);
        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(2, value.getAsLong());

        metric = new TestedAbstractTimelineProductUsageCondition(0, 450000, true, true, "$and", 0, 2, true, true);
        ListValueData listVD = (ListValueData)metric.getValue(builder.build());

        assertEquals(1, listVD.size());
        MapValueData entities = (MapValueData)listVD.getAll().get(0);
        assertEquals("2", entities.getAll().get("by_1_day").getAsString());
        assertEquals("0", entities.getAll().get("by_7_days").getAsString());
        assertEquals("0", entities.getAll().get("by_30_days").getAsString());
        assertEquals("0", entities.getAll().get("by_60_days").getAsString());
        assertEquals("0", entities.getAll().get("by_60_days").getAsString());
        assertEquals("0", entities.getAll().get("by_365_days").getAsString());
    }

    @Test
    public void testConditions2() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");


        Metric metric = new TestedAbstractProductUsageCondition(0, 450000, true, true, "$or", 0, 2, true, true);
        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(3, value.getAsLong());
    }

    @Test
    public void testConditions3() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");


        Metric metric = new TestedAbstractProductUsageCondition(0, 600000, true, true, "$and", 0, 2, true, true);
        LongValueData value = (LongValueData)metric.getValue(builder.build());

        assertEquals(3, value.getAsLong());
    }

    // --------------------> Tested classes

    private class TestedAbstractTimelineProductUsageCondition extends AbstractTimelineProductUsageCondition {

        protected TestedAbstractTimelineProductUsageCondition(long minTime, long maxTime,
                                                              boolean includeMinTime,
                                                              boolean includeMaxTime, String operator, long minSessions,
                                                              long maxSessions, boolean includeMinSessions,
                                                              boolean includeMaxSessions) {
            super(MetricType.TIMELINE_PRODUCT_USAGE_CONDITION_ABOVE_300_MIN,
                  new Metric[]{new TestedAbstractProductUsageCondition(minTime, maxTime,
                                                                       includeMinTime,
                                                                       includeMaxTime, operator, minSessions,
                                                                       maxSessions, includeMinSessions,
                                                                       includeMaxSessions)});
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestedAbstractProductUsageCondition extends AbstractProductUsageCondition {

        public TestedAbstractProductUsageCondition(long minTime, long maxTime,
                                                   boolean includeMinTime,
                                                   boolean includeMaxTime, String operator, long minSessions,
                                                   long maxSessions, boolean includeMinSessions,
                                                   boolean includeMaxSessions) {
            super(MetricType.PRODUCT_USAGE_CONDITION_ABOVE_300_MIN,
                  minTime, maxTime, includeMinTime, includeMaxTime,
                  operator, minSessions, maxSessions,
                  includeMinSessions, includeMaxSessions);
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getStorageCollectionName() {
            return MetricType.PRODUCT_USAGE_SESSIONS.toString().toLowerCase();
        }
    }
}
