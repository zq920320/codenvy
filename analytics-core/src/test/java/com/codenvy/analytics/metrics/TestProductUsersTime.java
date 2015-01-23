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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageCondition;
import com.codenvy.analytics.metrics.sessions.AbstractTimelineProductUsageCondition;
import com.codenvy.analytics.pig.scripts.ScriptType;
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

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsersTime extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        prepareData();

        makeAllUsersRegistered(ScriptType.PRODUCT_USAGE_SESSIONS.toString().toLowerCase());
        makeAllWsPersisted(ScriptType.PRODUCT_USAGE_SESSIONS.toString().toLowerCase());
    }

    @Test
    public void testProductUsersTime() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USERS_TIME);
        List<ValueData> value = getAsList(metric, builder.build()).getAll();

        assertEquals(value.size(), 3);
        assertTrue(value.contains(MapValueData.valueOf("user=user1_12345678901234,time=480000,sessions=2")));
        assertTrue(value.contains(MapValueData.valueOf("user=user2_12345678901234,time=60000,sessions=1")));
        assertTrue(value.contains(MapValueData.valueOf("user=user3_12345678901234,time=420000,sessions=1")));
    }

    @Test
    public void testTopEntities() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.PASSED_DAYS_COUNT, "by_1_day");

        Metric metric = MetricFactory.getMetric(MetricType.TOP_USERS);
        ListValueData data = getAsList(metric, builder.build());

        assertEquals(data.size(), 3);
        List<ValueData> all = data.getAll();

        Map<String, ValueData> m = treatAsMap(all.get(0));
        assertEquals(m.get("entity"), StringValueData.valueOf("user1_12345678901234"));
        assertEquals(m.get("sessions"), LongValueData.valueOf(2));
        assertEquals(m.get("by_1_day"), LongValueData.valueOf(480000));
        assertEquals(m.get("by_lifetime"), LongValueData.valueOf(480000));

        m = treatAsMap(all.get(1));
        assertEquals(m.get("entity"), StringValueData.valueOf("user3_12345678901234"));
        assertEquals(m.get("sessions"), LongValueData.valueOf(1));
        assertEquals(m.get("by_1_day"), LongValueData.valueOf(420000));
        assertEquals(m.get("by_lifetime"), LongValueData.valueOf(420000));

        m = treatAsMap(all.get(2));
        assertEquals(m.get("entity"), StringValueData.valueOf("user2_12345678901234"));
        assertEquals(m.get("sessions"), LongValueData.valueOf(1));
        assertEquals(m.get("by_1_day"), LongValueData.valueOf(60000));
        assertEquals(m.get("by_lifetime"), LongValueData.valueOf(60000));
    }

    @Test
    public void testConditions1() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestedAbstractProductUsageCondition(0, 450000, true, true, "$and", 0, 2, true, true);
        LongValueData l = getAsLong(metric, builder.build());

        assertEquals(l.getAsLong(), 2);

        metric = new TestedAbstractTimelineProductUsageCondition(0, 450000, true, true, "$and", 0, 2, true, true);
        ListValueData data = getAsList(metric, builder.build());

        assertEquals(data.size(), 1);
        List<ValueData> all = data.getAll();

        Map<String, ValueData> m = treatAsMap(all.get(0));
        assertEquals(m.get("by_1_day"), LongValueData.valueOf(2));
        assertEquals(m.get("by_7_days"), LongValueData.valueOf(0));
        assertEquals(m.get("by_30_days"), LongValueData.valueOf(0));
        assertEquals(m.get("by_60_days"), LongValueData.valueOf(0));
        assertEquals(m.get("by_90_days"), LongValueData.valueOf(0));
        assertEquals(m.get("by_365_days"), LongValueData.valueOf(0));
    }

    @Test
    public void testConditions2() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestedAbstractProductUsageCondition(0, 450000, true, true, "$or", 0, 2, true, true);
        LongValueData l = getAsLong(metric, builder.build());

        assertEquals(l.getAsLong(), 3);
    }

    @Test
    public void testConditions3() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestedAbstractProductUsageCondition(0, 600000, true, true, "$and", 0, 2, true, true);
        LongValueData l = getAsLong(metric, builder.build());

        assertEquals(l.getAsLong(), 3);
    }

    private void prepareData() throws IOException, ParseException {
        File log = initLog();

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    private File initLog() throws IOException {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:05:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=1")
                                      .build());

        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());
        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:03:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());

        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user2_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=3")
                                      .build());
        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:01:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user2_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=3")
                                      .build());

        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user3_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=4")
                                      .build());
        events.add(new Event.Builder().withDate("2013-11-01")
                                      .withTime("20:07:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user3_12345678901234")
                                      .withParam("PARAMETERS", "SESSION-ID=4")
                                      .build());

        return LogGenerator.generateLog(events);
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
    }
}
