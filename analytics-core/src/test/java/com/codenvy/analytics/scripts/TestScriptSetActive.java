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
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.MapStringSetValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptSetActive extends BaseTest {

    private Map<String, String> context;
    private File                log;

    @BeforeClass
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "session", "project1", "type")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "session", "project1", "type")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws2", "session", "project2", "type")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("", "", "session", "project1", "type").withDate("2013-01-01")
                        .build());
        log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.EVENT.put(context, "*");
    }

    @Test
    public void testSetActiveUsers() throws Exception {
        MetricParameter.FIELD.put(context, "user");

        SetStringValueData valueData = (SetStringValueData)executeAndReturnResult(ScriptType.SET_ACTIVE, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("user1@gmail.com"));
        assertTrue(valueData.getAll().contains("user2@gmail.com"));
    }

    @Test
    public void testSetActiveWs() throws Exception {
        MetricParameter.FIELD.put(context, "ws");

        SetStringValueData valueData = (SetStringValueData)executeAndReturnResult(ScriptType.SET_ACTIVE, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("ws1"));
        assertTrue(valueData.getAll().contains("ws2"));
    }

    @Test
    public void testActiveUsersByDomains() throws Exception {
        MetricParameter.FIELD.put(context, "user");
        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.SET_ACTIVE_BY_DOMAINS, log, context);


        assertEquals(valueData.size(), 1);
        assertTrue(valueData.getAll().containsKey("gmail.com"));

        SetStringValueData setValueData = valueData.getAll().get("gmail.com");
        assertEquals(setValueData.size(), 2);
        assertTrue(setValueData.getAll().contains("user1@gmail.com"));
        assertTrue(setValueData.getAll().contains("user2@gmail.com"));
    }

    @Test
    public void testActiveUsersByWs() throws Exception {
        MetricParameter.FIELD.put(context, "user");
        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.SET_ACTIVE_BY_WS, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().containsKey("ws1"));
        assertTrue(valueData.getAll().containsKey("ws2"));

        SetStringValueData setValueData = valueData.getAll().get("ws1");
        assertEquals(setValueData.size(), 1);
        assertTrue(setValueData.getAll().contains("user1@gmail.com"));

        setValueData = valueData.getAll().get("ws2");
        assertEquals(setValueData.size(), 2);
        assertTrue(setValueData.getAll().contains("user1@gmail.com"));
        assertTrue(setValueData.getAll().contains("user2@gmail.com"));
    }

    @Test
    public void testActiveUsersByUsers() throws Exception {
        MetricParameter.FIELD.put(context, "user");
        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.SET_ACTIVE_BY_USERS, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().containsKey("user1@gmail.com"));
        assertTrue(valueData.getAll().containsKey("user2@gmail.com"));

        SetStringValueData setValueData = valueData.getAll().get("user1@gmail.com");
        assertEquals(setValueData.size(), 1);
        assertTrue(setValueData.getAll().contains("user1@gmail.com"));

        setValueData = valueData.getAll().get("user2@gmail.com");
        assertEquals(setValueData.size(), 1);
        assertTrue(setValueData.getAll().contains("user2@gmail.com"));
    }

    @Test
    public void testActiveWsByDomains() throws Exception {
        MetricParameter.FIELD.put(context, "ws");
        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.SET_ACTIVE_BY_DOMAINS, log, context);

        assertEquals(valueData.size(), 1);
        assertTrue(valueData.getAll().containsKey("gmail.com"));

        SetStringValueData setValueData = valueData.getAll().get("gmail.com");
        assertEquals(setValueData.size(), 2);
        assertTrue(setValueData.getAll().contains("ws1"));
        assertTrue(setValueData.getAll().contains("ws2"));
    }

    @Test
    public void testActiveWsByUsers() throws Exception {
        MetricParameter.FIELD.put(context, "ws");
        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.SET_ACTIVE_BY_USERS, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().containsKey("user1@gmail.com"));
        assertTrue(valueData.getAll().containsKey("user2@gmail.com"));

        SetStringValueData setValueData = valueData.getAll().get("user1@gmail.com");
        assertEquals(setValueData.size(), 2);
        assertTrue(setValueData.getAll().contains("ws1"));
        assertTrue(setValueData.getAll().contains("ws2"));

        setValueData = valueData.getAll().get("user2@gmail.com");
        assertEquals(setValueData.size(), 1);
        assertTrue(setValueData.getAll().contains("ws2"));
    }

    @Test
    public void testActiveWsByWs() throws Exception {
        MetricParameter.FIELD.put(context, "ws");
        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.SET_ACTIVE_BY_WS, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().containsKey("ws1"));
        assertTrue(valueData.getAll().containsKey("ws2"));

        SetStringValueData setValueData = valueData.getAll().get("ws1");
        assertEquals(setValueData.size(), 1);
        assertTrue(setValueData.getAll().contains("ws1"));

        setValueData = valueData.getAll().get("ws2");
        assertEquals(setValueData.size(), 1);
        assertTrue(setValueData.getAll().contains("ws2"));
    }

    @Test
    public void testActiveWsByUrl() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1",
                                                            "referrer1").withDate("2013-01-01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1",
                                                            "referrer2").withDate("2013-01-01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl2",
                                                            "referrer2").withDate("2013-01-01").build());
        File fLog = LogGenerator.generateLog(events);

        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));
        MetricParameter.STORE_DIR.put(context, Utils.getStoreDirFor(MetricType.FACTORY_URL_ACCEPTED));
        Utils.initLoadStoreDirectories(context);

        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.FACTORY_URL_ACCEPTED_BY_FACTORY_URL, fLog, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().containsKey("factoryUrl1"));
        assertTrue(valueData.getAll().containsKey("factoryUrl2"));

        SetStringValueData setValueData = valueData.getAll().get("factoryUrl1");
        assertEquals(setValueData.size(), 2);
        assertTrue(setValueData.getAll().contains("tmp-1"));
        assertTrue(setValueData.getAll().contains("tmp-2"));

        setValueData = valueData.getAll().get("factoryUrl2");
        assertEquals(setValueData.size(), 1);
        assertTrue(setValueData.getAll().contains("tmp-3"));
    }
}
