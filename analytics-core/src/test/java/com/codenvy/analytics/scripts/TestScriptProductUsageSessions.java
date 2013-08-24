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

        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-01-01").withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-01-01").withTime("20:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        // should be ignored because of temporary workspace
        events.add(Event.Builder.createSessionStartedEvent("user@gmail.com", "tmp-1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(
                Event.Builder.createSessionFinishedEvent("user@gmail.com", "tmp-1", "ide", "3").withDate("2013-01-01")
                     .withTime("20:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                        .withDate("2013-01-01").withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                        .withDate("2013-01-01").withTime("20:05:00").build());


        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        MetricParameter.FROM_DATE.put(params, "20130101");
        MetricParameter.TO_DATE.put(params, "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());

        ListListStringValueData value =
                (ListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_SESSIONS, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 2);

        MapStringListListStringValueData map =
                (MapStringListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_SESSIONS_BY_USERS,
                                                                         log,
                                                                         params);

        assertEquals(map.size(), 2);
        assertUser(map, "ANONYMOUSUSER_user11", 1, 5 * 60);
        assertUser(map, "user@gmail.com", 1, 5 * 60);

        map =
                (MapStringListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_SESSIONS_BY_DOMAINS,
                                                                         log,
                                                                         params);

        assertEquals(map.size(), 1);
        assertUser(map, "gmail.com", 1, 5 * 60);
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

