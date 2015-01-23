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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author Anatoliy Bazko */
public class TestProductUsageTime extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        doExecute("20140101");
        doExecute("20140102");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData l = getAsList(metric, Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.USER);

        assertEquals(m.size(), 3);
        assertTrue(m.containsKey("user1"));
        assertTrue(m.containsKey("user2"));
        assertTrue(m.containsKey("user3"));

        Map<String, ValueData> data = m.get("user1");
        assertEquals(data.get(AbstractMetric.WS), StringValueData.valueOf("ws"));
        assertEquals(data.get(AbstractMetric.USER_COMPANY), StringValueData.valueOf("company"));
        assertEquals(data.get(AbstractMetric.DOMAIN), StringValueData.valueOf("gmail.com"));
        assertEquals(data.get(AbstractMetric.TIME), LongValueData.valueOf(600000));
        assertEquals(data.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("1"));
        assertEquals(data.get(AbstractMetric.DATE), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 10:00:00").getTime()));
        assertEquals(data.get(AbstractMetric.END_TIME), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 10:10:00").getTime()));
        assertEquals(data.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(0));

        data = m.get("user2");
        assertEquals(data.get(AbstractMetric.WS), StringValueData.valueOf("ws"));
        assertEquals(data.get(AbstractMetric.USER_COMPANY), StringValueData.valueOf(""));
        assertEquals(data.get(AbstractMetric.DOMAIN), StringValueData.valueOf(""));
        assertEquals(data.get(AbstractMetric.TIME), LongValueData.valueOf(600000));
        assertEquals(data.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("2"));
        assertEquals(data.get(AbstractMetric.DATE), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 23:55:00").getTime()));
        assertEquals(data.get(AbstractMetric.END_TIME), LongValueData.valueOf(fullDateFormat.parse("2014-01-02 00:05:00").getTime()));
        assertEquals(data.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(0));

        data = m.get("user3");
        assertEquals(data.get(AbstractMetric.USER), StringValueData.valueOf("user3"));
        assertEquals(data.get(AbstractMetric.WS), StringValueData.valueOf("ws"));
        assertEquals(data.get(AbstractMetric.USER_COMPANY), StringValueData.valueOf(""));
        assertEquals(data.get(AbstractMetric.DOMAIN), StringValueData.valueOf(""));
        assertEquals(data.get(AbstractMetric.TIME), LongValueData.valueOf(60000));
        assertEquals(data.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("3"));
        assertEquals(data.get(AbstractMetric.DATE), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 10:00:00").getTime()));
        assertEquals(data.get(AbstractMetric.END_TIME), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 10:01:00").getTime()));
        assertEquals(data.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(60000));
    }

    @Test
    public void testExecute2() throws Exception {
        clearDatabase();
        doExecute2("20140101");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData l = getAsList(metric, Context.EMPTY);

        assertEquals(l.getAll().size(), 2);

        Map<String, ValueData> data = ((MapValueData)l.getAll().get(0)).getAll();
        assertEquals(data.get(AbstractMetric.USER), StringValueData.valueOf("userid1"));
        assertEquals(data.get(AbstractMetric.WS), StringValueData.valueOf("tmp-workspace01"));
        assertEquals(data.get(AbstractMetric.DOMAIN), StringValueData.valueOf("gmail.com"));
        assertEquals(data.get(AbstractMetric.TIME), LongValueData.valueOf(608000));
        assertEquals(data.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("sid1"));
        assertEquals(data.get(AbstractMetric.DATE), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 09:02:02").getTime()));
        assertEquals(data.get(AbstractMetric.END_TIME), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 09:12:10").getTime()));
        assertEquals(data.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(0));

        data = ((MapValueData)l.getAll().get(1)).getAll();
        assertEquals(data.get(AbstractMetric.USER), StringValueData.valueOf("userid1"));
        assertEquals(data.get(AbstractMetric.WS), StringValueData.valueOf("tmp-workspace01"));
        assertEquals(data.get(AbstractMetric.DOMAIN), StringValueData.valueOf("gmail.com"));
        assertEquals(data.get(AbstractMetric.TIME), LongValueData.valueOf(600000));
        assertEquals(data.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("sid2"));
        assertEquals(data.get(AbstractMetric.DATE), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 10:02:00").getTime()));
        assertEquals(data.get(AbstractMetric.END_TIME), LongValueData.valueOf(fullDateFormat.parse("2014-01-01 10:12:00").getTime()));
        assertEquals(data.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(0));
    }

    private void doExecute(String date) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, initLog().getAbsolutePath());
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    private void doExecute2(String date) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, initLogWithFactoryUrlAccepted().getAbsolutePath());
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }


    private File initLog() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("user1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2014-01-01")
                                .withTime("09:00:00")
                                .build());
        events.add(Event.Builder.createUserUpdateProfile("user1", "user1@gmail.com", "user1@gmail.com", "", "", "company", "", "")
                                .withDate("2014-01-01")
                                .withTime("09:01:00")
                                .build());

        // simple session, 10 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user1")
                                      .withParam("PARAMETERS", "SESSION-ID=1")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user1")
                                      .withParam("PARAMETERS", "SESSION-ID=1")
                                      .build());

        // session ends on next day, 10 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("23:55:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user2")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("23:59:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user2")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());

        events.add(new Event.Builder().withDate("2014-01-02")
                                      .withTime("00:01:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user2")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-02")
                                      .withTime("00:05:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user2")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());


        // session with logout event, 1 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user3")
                                      .withParam("PARAMETERS", "SESSION-ID=3")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:01:00")
                                      .withParam("EVENT", "user-sso-logged-out")
                                      .withParam("USER", "user3")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "user-sso-logged-out")
                                      .withParam("USER", "user3")
                                      .build());

        return LogGenerator.generateLog(events);
    }

    private File initLogWithFactoryUrlAccepted() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("userid1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2014-01-01")
                                .withTime("09:00:00")
                                .build());
        events.add(Event.Builder.createUserUpdateProfile("user1", "user1@gmail.com", "user1@gmail.com", "", "", "company", "", "")
                                .withDate("2014-01-01")
                                .withTime("09:01:00")
                                .build());

        events.add(Event.Builder.createUserAddedToWsEvent("user1@gmail.com", "tmp-workspace01", "website")
                                .withDate("2014-01-01")
                                .withTime("09:02:00")
                                .build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "tmp-workspace01", "user")
                                .withDate("2014-01-01")
                                .withTime("09:02:01")
                                .build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-workspace01", "https://codenvy.com/factory/?id=01", "", "accounty01", "")
                                .withDate("2014-01-01")
                                .withTime("09:02:02")
                                .build());

        // simple session, 10 min + 8 sec
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("09:02:10")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "tmp-workspace01")
                                      .withParam("USER", "user1@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=sid1")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("09:12:10")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "tmp-workspace01")
                                      .withParam("USER", "user1@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=sid1")
                                      .build());

        // second session, 10 min sec
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "tmp-workspace01")
                                      .withParam("USER", "user1@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=sid2")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:12:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "tmp-workspace01")
                                      .withParam("USER", "user1@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=sid2")
                                      .build());

        return LogGenerator.generateLog(events);
    }
}
