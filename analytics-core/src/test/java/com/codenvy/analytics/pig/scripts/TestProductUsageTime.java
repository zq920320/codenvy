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
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/** @author Anatoliy Bazko */
public class TestProductUsageTime extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        doExecute("20140101");
        doExecute("20140102");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
        ListValueData data = ValueDataUtil.getAsList(metric, Context.EMPTY);

        assertEquals(data.size(), 3);

        Iterator<ValueData> iterator = data.getAll().iterator();
        while (iterator.hasNext()) {
            Map<String, ValueData> m = treatAsMap(iterator.next());

            if (m.get(AbstractMetric.USER).equals(StringValueData.valueOf("user1"))) {
                assertEquals(m.get(AbstractMetric.WS), StringValueData.valueOf("ws"));
                assertEquals(m.get(AbstractMetric.USER_COMPANY), StringValueData.valueOf("company"));
                assertEquals(m.get(AbstractMetric.DOMAIN), StringValueData.valueOf("gmail.com"));
                assertEquals(m.get(AbstractMetric.TIME), LongValueData.valueOf(600000));
                assertEquals(m.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("1"));
                assertEquals(m.get(AbstractMetric.DATE), LongValueData.valueOf(1388563200000L));
                assertEquals(m.get(AbstractMetric.END_TIME), LongValueData.valueOf(1388563800000L));
                assertEquals(m.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(0));

            } else if (m.get(AbstractMetric.USER).equals(StringValueData.valueOf("user2"))) {
                assertEquals(m.get(AbstractMetric.USER), StringValueData.valueOf("user2"));
                assertEquals(m.get(AbstractMetric.WS), StringValueData.valueOf("ws"));
                assertEquals(m.get(AbstractMetric.USER_COMPANY), StringValueData.valueOf(""));
                assertEquals(m.get(AbstractMetric.DOMAIN), StringValueData.valueOf(""));
                assertEquals(m.get(AbstractMetric.TIME), LongValueData.valueOf(300000));
                assertEquals(m.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("2"));
                assertEquals(m.get(AbstractMetric.DATE), LongValueData.valueOf(1388563200000L));
                assertEquals(m.get(AbstractMetric.END_TIME), LongValueData.valueOf(1388563500000L));
                assertEquals(m.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(0));

            } else if (m.get(AbstractMetric.USER).equals(StringValueData.valueOf("user3"))) {
                assertEquals(m.get(AbstractMetric.USER), StringValueData.valueOf("user3"));
                assertEquals(m.get(AbstractMetric.WS), StringValueData.valueOf("ws"));
                assertEquals(m.get(AbstractMetric.USER_COMPANY), StringValueData.valueOf(""));
                assertEquals(m.get(AbstractMetric.DOMAIN), StringValueData.valueOf(""));
                assertEquals(m.get(AbstractMetric.TIME), LongValueData.valueOf(60000));
                assertEquals(m.get(AbstractMetric.SESSION_ID), StringValueData.valueOf("3"));
                assertEquals(m.get(AbstractMetric.DATE), LongValueData.valueOf(1388563200000L));
                assertEquals(m.get(AbstractMetric.END_TIME), LongValueData.valueOf(1388563260000L));
                assertEquals(m.get(AbstractMetric.LOGOUT_INTERVAL), LongValueData.valueOf(60000));
            } else {
                fail();
            }
        }
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

        // simple session, 5 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user1")
                                      .withParam("PARAMETERS", "USAGE-TIME=0,START-TIME=1388563200000,SESSION-ID=1")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user1")
                                      .withParam("PARAMETERS", "USAGE-TIME=600000,START-TIME=1388563200000,SESSION-ID=1")
                                      .build());

        // session ends on next day, 10 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user2")
                                      .withParam("PARAMETERS", "USAGE-TIME=0,START-TIME=1388563200000,SESSION-ID=2")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-02")
                                      .withTime("10:05:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user2")
                                      .withParam("PARAMETERS", "USAGE-TIME=300000,START-TIME=1388563200000,SESSION-ID=2")
                                      .build());

        // session with logout event, 1 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user3")
                                      .withParam("PARAMETERS", "USAGE-TIME=0,START-TIME=1388563200000,SESSION-ID=3")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:01:00")
                                      .withParam("EVENT", "user-sso-logged-out")
                                      .withParam("USER", "user3")
                                      .build());

        return LogGenerator.generateLog(events);
    }
}
