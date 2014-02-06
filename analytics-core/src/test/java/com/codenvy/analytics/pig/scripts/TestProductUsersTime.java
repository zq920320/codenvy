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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.sessions.AbstractProductTime;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageCondition;
import com.codenvy.analytics.metrics.sessions.AbstractTimelineProductUsageCondition;
import com.codenvy.analytics.metrics.sessions.ProductUsageSessionsList;
import com.codenvy.analytics.metrics.top.AbstractTopUsers;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsersTime extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        Map<String, String> params = Utils.newContext();

        List<Event> events = new ArrayList<>();
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

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testproductuserstime");
        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(params, "testproductuserstime-stat");
        Parameters.STORAGE_TABLE_USERS_PROFILES.put(params, "testproductuserstime-profiles");
        Parameters.LOG.put(params, log.getAbsolutePath());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);
    }

    @Test
    public void testProductUsersTime() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");
        Parameters.SORT.put(context, "-time");

        Metric metric = new TestedProductUsersTime();
        ListValueData value = (ListValueData)metric.getValue(context);

        List<ValueData> all = value.getAll();
        MapValueData valueData = (MapValueData)all.get(0);
        assertEquals(valueData.getAll().get("user").getAsString(), "user1@gmail.com");
        assertEquals(valueData.getAll().get("time").getAsString(), "480");
        assertEquals(valueData.getAll().get("sessions").getAsString(), "2");

        valueData = (MapValueData)all.get(1);
        assertEquals(valueData.getAll().get("user").getAsString(), "user3@gmail.com");
        assertEquals(valueData.getAll().get("time").getAsString(), "420");
        assertEquals(valueData.getAll().get("sessions").getAsString(), "1");

        valueData = (MapValueData)all.get(2);
        assertEquals(valueData.getAll().get("user").getAsString(), "user2@gmail.com");
        assertEquals(valueData.getAll().get("time").getAsString(), "60");
        assertEquals(valueData.getAll().get("sessions").getAsString(), "1");
    }

    @Test
    public void testTopEntities() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        Metric metric = new TestedAbstractTopUsers();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 3);
        MapValueData item = (MapValueData)value.getAll().get(0);
        assertEquals(item.getAll().get("entity").getAsString(), "user1@gmail.com");
        assertEquals(item.getAll().get("sessions").getAsString(), "2");
        assertEquals(item.getAll().get("by_1_day").getAsString(), "480");
        assertEquals(item.getAll().get("by_lifetime").getAsString(), "480");

        item = (MapValueData)value.getAll().get(1);
        assertEquals(item.getAll().get("entity").getAsString(), "user3@gmail.com");
        assertEquals(item.getAll().get("sessions").getAsString(), "1");
        assertEquals(item.getAll().get("by_1_day").getAsString(), "420");
        assertEquals(item.getAll().get("by_lifetime").getAsString(), "420");

        item = (MapValueData)value.getAll().get(2);
        assertEquals(item.getAll().get("entity").getAsString(), "user2@gmail.com");
        assertEquals(item.getAll().get("sessions").getAsString(), "1");
        assertEquals(item.getAll().get("by_1_day").getAsString(), "60");
        assertEquals(item.getAll().get("by_lifetime").getAsString(), "60");
    }

    @Test
    public void testConditions1() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        Metric metric = new TestedAbstractProductUsageCondition(0, 450, true, true, "$and", 0, 2, true, true);
        LongValueData value = (LongValueData)metric.getValue(context);

        assertEquals(2, value.getAsLong());

        metric = new TestedAbstractTimelineProductUsageCondition(0, 450, true, true, "$and", 0, 2, true, true);
        ListValueData listVD = (ListValueData)metric.getValue(context);

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
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        Metric metric = new TestedAbstractProductUsageCondition(0, 450, true, true, "$or", 0, 2, true, true);
        LongValueData value = (LongValueData)metric.getValue(context);

        assertEquals(3, value.getAsLong());
    }

    @Test
    public void testConditions3() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        Metric metric = new TestedAbstractProductUsageCondition(0, 600, true, true, "$and", 0, 2, true, true);
        LongValueData value = (LongValueData)metric.getValue(context);

        assertEquals(3, value.getAsLong());
    }

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
            return "testproductuserstime";
        }
    }

    private class TestedProductUsersTime extends AbstractProductTime {

        public TestedProductUsersTime() {
            super(MetricType.PRODUCT_USERS_TIME);
        }

        @Override
        public String[] getTrackedFields() {
            return new String[]{ProductUsageSessionsList.USER,
                                ProductUsageSessionsList.TIME,
                                SESSIONS};
        }

        @Override
        public String getStorageCollectionName() {
            return "testproductuserstime";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestedAbstractTopUsers extends AbstractTopUsers {

        public TestedAbstractTopUsers() {
            super(MetricType.TOP_USERS_BY_1DAY, new TestedProductUsersTime(), 1);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
