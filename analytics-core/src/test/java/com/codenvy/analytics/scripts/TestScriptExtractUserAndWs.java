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
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptExtractUserAndWs extends BaseTest {

    private HashMap<String, String> context;
    private File                    log;

    @BeforeTest
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("USER", "user1")
                                      .withParam("WS", "ws1").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "[user2,user3]")
                                      .withParam("WS", "tmp-2").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "user4")
                                      .withParam("WS", "tmp-1").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("USER", "AnonymousUser_1")
                                      .withParam("WS", "ws2").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "AnonymousUser_2")
                                      .withParam("WS", "tmp-3").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withDate("2013-01-01").build());

        log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "2013-01-01");
        MetricParameter.TO_DATE.put(context, "2013-01-01");
    }

    @Test
    public void testExtractAllUsers() throws Exception {
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_USER, log, context);

        assertEquals(valueData.size(), 7);
        assertTrue(valueData.getAll().contains("user1"));
        assertTrue(valueData.getAll().contains("user2"));
        assertTrue(valueData.getAll().contains("user3"));
        assertTrue(valueData.getAll().contains("user4"));
        assertTrue(valueData.getAll().contains("default"));
        assertTrue(valueData.getAll().contains("AnonymousUser_1"));
        assertTrue(valueData.getAll().contains("AnonymousUser_2"));
    }

    @Test
    public void testExtractAnonymousUsers() throws Exception {
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANTONYMOUS.name());

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_USER, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("AnonymousUser_1"));
        assertTrue(valueData.getAll().contains("AnonymousUser_2"));
    }

    @Test
    public void testExtractRegisteredUsers() throws Exception {
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_USER, log, context);

        assertEquals(valueData.size(), 5);
        assertTrue(valueData.getAll().contains("user1"));
        assertTrue(valueData.getAll().contains("user2"));
        assertTrue(valueData.getAll().contains("user3"));
        assertTrue(valueData.getAll().contains("user4"));
        assertTrue(valueData.getAll().contains("default"));
    }

    @Test
    public void testExtractAllWs() throws Exception {
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_WS, log, context);

        assertEquals(valueData.size(), 6);
        assertTrue(valueData.getAll().contains("ws1"));
        assertTrue(valueData.getAll().contains("ws2"));
        assertTrue(valueData.getAll().contains("tmp-1"));
        assertTrue(valueData.getAll().contains("tmp-2"));
        assertTrue(valueData.getAll().contains("tmp-3"));
        assertTrue(valueData.getAll().contains("default"));
    }

    @Test
    public void testExtractTmpWs() throws Exception {
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_WS, log, context);

        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().contains("tmp-1"));
        assertTrue(valueData.getAll().contains("tmp-2"));
        assertTrue(valueData.getAll().contains("tmp-3"));
    }

    @Test
    public void testExtractPersistentWs() throws Exception {
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_WS, log, context);

        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().contains("ws1"));
        assertTrue(valueData.getAll().contains("ws2"));
        assertTrue(valueData.getAll().contains("default"));
    }

    @Test
    public void testExtractRegisteredUsersInPersistentWs() throws Exception {
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.PERSISTENT.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_WS, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("ws1"));
        assertTrue(valueData.getAll().contains("default"));

        valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_USER, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("user1"));
        assertTrue(valueData.getAll().contains("default"));
    }
}
