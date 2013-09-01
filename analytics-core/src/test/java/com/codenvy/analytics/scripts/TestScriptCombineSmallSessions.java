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
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptCombineSmallSessions extends BaseTest {

    @Test
    public void testExecuteScript() throws Exception {
        List<Event> events = new ArrayList<>();

        // 6m
        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:02:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("19:03:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("19:04:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("19:05:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("19:06:00").build());

        // 2m
        events.add(Event.Builder.createSessionStartedEvent("user2", "ws1", "ide", "5").withDate("2013-01-01")
                        .withTime("19:20:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2", "ws1", "ide", "5").withDate("2013-01-01")
                        .withTime("19:22:00").build());

        // 5m
        events.add(Event.Builder.createSessionStartedEvent("user3", "ws1", "ide", "6").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user3", "ws1", "ide", "6").withDate("2013-01-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user3", "ws1", "ide", "6").withDate("2013-01-01")
                        .withTime("20:10:00").build());


        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.ANY.name());

        ListListStringValueData valueData =
                (ListListStringValueData)executeAndReturnResult(ScriptType.TEST_COMBINE_SMALL_SESSIONS, log, params);

        List<ListStringValueData> items = valueData.getAll();

        assertEquals(items.size(), 3);

        assertUser(items, "user1", 360);
        assertUser(items, "user2", 120);
        assertUser(items, "user3", 300);
    }

    private void assertUser(List<ListStringValueData> items, String user, int expectedTime) {
        long time = -1;

        for (ListStringValueData item : items) {
            if (item.getAll().get(1).equals(user)) {
                time = Long.valueOf(item.getAll().get(3));
            }
        }

        assertEquals(expectedTime, time);
    }
}

