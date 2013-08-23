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
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapListLongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptNumberOfEventsWithType extends BaseTest {

    @Test
    public void testNumberOfAllEvents() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "github").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2", "google").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user3", "jaas").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2013-01-01").withTime(
                "20:00:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.EVENT.put(context, EventType.USER_SSO_LOGGED_IN.toString());
        MetricParameter.PARAM.put(context, "USING");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());


        MapStringLongValueData valueData =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.NUMBER_EVENTS_WITH_TYPE, log, context);

        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().containsKey("google"));
        assertTrue(valueData.getAll().containsKey("github"));
        assertTrue(valueData.getAll().containsKey("jaas"));
        assertEquals(valueData.getAll().get("google").longValue(), 3);
        assertEquals(valueData.getAll().get("github").longValue(), 1);
        assertEquals(valueData.getAll().get("jaas").longValue(), 1);

    }

    @Test
    public void testNumberOfAllEventsByUser() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "github").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2", "google").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user3", "jaas").withDate("2013-01-01").build());
        events.add(
                Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2013-01-01").withTime("20:00:00")
                     .build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.EVENT.put(context, EventType.USER_SSO_LOGGED_IN.toString());
        MetricParameter.PARAM.put(context, "USING");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());

        MapListLongValueData valueData =
                (MapListLongValueData)executeAndReturnResult(ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_USERS, log, context);

        ListStringValueData key1 = new ListStringValueData(Arrays.asList("google", "user1"));
        ListStringValueData key2 = new ListStringValueData(Arrays.asList("github", "user1"));
        ListStringValueData key3 = new ListStringValueData(Arrays.asList("google", "user2"));
        ListStringValueData key4 = new ListStringValueData(Arrays.asList("jaas", "user3"));

        assertEquals(valueData.size(), 4);
        assertTrue(valueData.getAll().containsKey(key1));
        assertTrue(valueData.getAll().containsKey(key2));
        assertTrue(valueData.getAll().containsKey(key3));
        assertTrue(valueData.getAll().containsKey(key4));
        assertEquals(valueData.getAll().get(key1).longValue(), 2);
        assertEquals(valueData.getAll().get(key2).longValue(), 1);
        assertEquals(valueData.getAll().get(key3).longValue(), 1);
        assertEquals(valueData.getAll().get(key4).longValue(), 1);

    }

    @Test
    public void testNumberOfAllEventsByDomain() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(
                Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "google").withDate("2013-01-01").build());
        events.add(
                Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "github").withDate("2013-01-01").build());
        events.add(
                Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user3@gmail.com", "jaas").withDate("2013-01-01").build());
        events.add(
                Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "google").withDate("2013-01-01").withTime(
                        "20:00:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.EVENT.put(context, EventType.USER_SSO_LOGGED_IN.toString());
        MetricParameter.PARAM.put(context, "USING");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());

        MapListLongValueData valueData =
                (MapListLongValueData)executeAndReturnResult(ScriptType.NUMBER_EVENTS_WITH_TYPE_BY_DOMAINS, log,
                                                             context);

        ListStringValueData key1 = new ListStringValueData(Arrays.asList("google", "gmail.com"));
        ListStringValueData key2 = new ListStringValueData(Arrays.asList("github", "gmail.com"));
        ListStringValueData key3 = new ListStringValueData(Arrays.asList("jaas", "gmail.com"));

        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().containsKey(key1));
        assertTrue(valueData.getAll().containsKey(key2));
        assertTrue(valueData.getAll().containsKey(key3));
        assertEquals(valueData.getAll().get(key1).longValue(), 3);
        assertEquals(valueData.getAll().get(key2).longValue(), 1);
        assertEquals(valueData.getAll().get(key3).longValue(), 1);

    }
}
