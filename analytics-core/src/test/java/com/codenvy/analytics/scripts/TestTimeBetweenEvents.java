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
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestTimeBetweenEvents extends BaseTest {

    private File                log;
    private Map<String, String> context;

    @BeforeTest
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();

        // 6m
        events.add(
                Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:00:00").build());
        events.add(
                Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:06:00").build());

        // failed session, there is no 'run-finished' event
        events.add(
                Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:07:00").build());

        // 2m
        events.add(
                Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:08:00").build());
        events.add(
                Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:10:00").build());

        // 1m
        events.add(
                Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:11:00").build());
        events.add(
                Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:12:00").build());

        // failed session, there is no 'run-started' event
        events.add(
                Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project", "type").withDate("2013-01-01")
                     .withTime("19:13:00").build());


        log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
    }

    @Test
    public void testExecuteTestScript() throws Exception {
        ListListStringValueData valueData =
                (ListListStringValueData)executeAndReturnResult(ScriptType.TEST_TIME_BETWEEN_PAIRS_OF_EVENTS,
                                                                log,
                                                                context);

        assertEquals(valueData.size(), 3);

        int sessions = 0;

        for (ListStringValueData listValueData : valueData.getAll()) {
            List<String> items = listValueData.getAll();

            if (items.get(3).equals("60")) {
                assertTrue(items.get(2).contains("19:11:00"));
                sessions |= 0b1;
            } else if (items.get(3).equals("120")) {
                assertTrue(items.get(2).contains("19:08:00"));
                sessions |= 0b10;
            } else if (items.get(3).equals("360")) {
                assertTrue(items.get(2).contains("19:00:00"));
                sessions |= 0b100;
            }
        }

        assertEquals(sessions, 0b111);
    }

    @Test
    public void testTimeBetweenEvents() throws Exception {
        MetricParameter.EVENT.put(context, "run");
        LongValueData valueData = (LongValueData)executeAndReturnResult(ScriptType.TIME_BETWEEN_EVENTS,
                                                                        log,
                                                                        context);

        assertEquals(valueData.getAsLong(), 540L);
    }

    @Test
    public void testTimeBetweenEventsByUsers() throws Exception {
        MetricParameter.EVENT.put(context, "run");
        MapStringLongValueData valueData =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.TIME_BETWEEN_EVENTS_BY_USERS,
                                                               log,
                                                               context);

        assertEquals(valueData.size(), 1);
        assertEquals(valueData.getAll().get("user1@gmail.com").longValue(), 540L);
    }

    @Test
    public void testTimeBetweenEventsByDomains() throws Exception {
        MetricParameter.EVENT.put(context, "run");
        MapStringLongValueData valueData =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.TIME_BETWEEN_EVENTS_BY_DOMAINS,
                                                               log,
                                                               context);

        assertEquals(valueData.size(), 1);
        assertEquals(valueData.getAll().get("gmail.com").longValue(), 540L);
    }
}


