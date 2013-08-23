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

package com.codenvy.analytics.scripts;


import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptProductUsageSessions extends BaseTest {

    @Test
    public void testExecuteScript() throws Exception {
        List<Event> events = new ArrayList<>();

        // user1 session #1 [7m]
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:07:00").build());

        // user2 session #1 [4m]
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:29:00").build());

        // user1 session #2 [7m]
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("21:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("21:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("21:07:00").build());

        // user3 session #1 [10m]
        events.add(Event.Builder.createProjectBuiltEvent("user3", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:25:00").build());

        // session started and session finished [5m]
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        events.add(Event.Builder.createProjectBuiltEvent("user11", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:07:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user11", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:09:00").build());

        events.add(Event.Builder.createProjectBuiltEvent("user11", "tmp-ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:09:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user11", "tmp-ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:11:00").build());

        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "tmp-ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("20:13:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "tmp-ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("20:15:00").build());

        // session started and some event after [2h]
        events.add(Event.Builder.createSessionStartedEvent("user12", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user12", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user12", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:07:00").build());

        // session started and some event after [1h]
        events.add(Event.Builder.createSessionStartedEvent("user15@gmail.com", "ws1", "ide", "6").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user15@gmail.com", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        // session finished and some event before [30m]
        events.add(Event.Builder.createProjectBuiltEvent("user13", "ws1", "", "", "").withDate("2013-01-01")
                        .withTime("19:55:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user13", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:00:00").build());

        // only session started [10m]
        events.add(Event.Builder.createSessionStartedEvent("user14", "ws1", "ide", "4").withDate("2013-01-01")
                        .withTime("20:00:00").build());

        // only session finished [10m]
        events.add(
                Event.Builder.createSessionFinishedEvent("user15@gmail.com", "ws1", "ide", "5").withDate("2013-01-01")
                     .withTime("21:00:00").build());



        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());


        ListListStringValueData value =
                (ListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_SESSIONS, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 11);

        MapStringListListStringValueData map =
                (MapStringListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_SESSIONS_BY_USERS, log,
                                                                         params);

        assertEquals(map.size(), 9);
        assertUser(map, "user1", 2, 14 * 60);
        assertUser(map, "user2", 1, 4 * 60);
        assertUser(map, "user3", 1, 10 * 60);
        assertUser(map, "user11", 1, 2 * 60);
        assertUser(map, "ANONYMOUSUSER_user11", 1, 5 * 60);
        assertUser(map, "user12", 1, 7 * 60);
        assertUser(map, "user15@gmail.com", 2, 15 * 60);
        assertUser(map, "user13", 1, 5 * 60);
        assertUser(map, "user14", 1, 10 * 60);

        map =
                (MapStringListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_SESSIONS_BY_DOMAINS, log,
                                                                         params);

        assertEquals(map.size(), 1);
        assertUser(map, "gmail.com", 2, 15 * 60);
    }


    private void assertUser(MapStringListListStringValueData map, String user, int numberOfSessions,
                            int timeInSeconds) {
        assertTrue(map.getAll().containsKey(user));

        ListListStringValueData valueData = map.getAll().get(user);
        assertEquals(valueData.size(), numberOfSessions);

        int total = 0;
        for (ListStringValueData item : valueData.getAll()) {
            total += Long.valueOf(item.getAll().get(3));
        }

        assertEquals(total, timeInSeconds);
    }
}

