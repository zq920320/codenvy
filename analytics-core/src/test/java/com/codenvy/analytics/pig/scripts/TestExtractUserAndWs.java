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

package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestExtractUserAndWs extends BaseTest {

    private HashMap<String, String> context = new HashMap<>();

    @BeforeClass
    public void prepare() throws Exception {
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
        events.add(Event.Builder.createUserAddedToWsEvent("default", "default", "default", "ws10", "user10", "website")
                        .withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");
        Parameters.LOG.put(context, log.getAbsolutePath());
        Parameters.STORAGE_DST.put(context, "fake");
    }

    @Test
    public void testExtractAllUsers() throws Exception {
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(user1)");
        expected.add("(user2)");
        expected.add("(user3)");
        expected.add("(user4)");
        expected.add("(user10)");
        expected.add("(default)");
        expected.add("(AnonymousUser_1)");
        expected.add("(AnonymousUser_2)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractAnonymousUsers() throws Exception {
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.ANTONYMOUS.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(AnonymousUser_1)");
        expected.add("(AnonymousUser_2)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractRegisteredUsers() throws Exception {
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.REGISTERED.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(user1)");
        expected.add("(user2)");
        expected.add("(user3)");
        expected.add("(user4)");
        expected.add("(user10)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractAllWs() throws Exception {
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(ws1)");
        expected.add("(ws10)");
        expected.add("(ws2)");
        expected.add("(tmp-1)");
        expected.add("(tmp-2)");
        expected.add("(tmp-3)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractTmpWs() throws Exception {
        Parameters.WS.put(context, Parameters.WS_TYPES.TEMPORARY.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(tmp-1)");
        expected.add("(tmp-2)");
        expected.add("(tmp-3)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractPersistentWs() throws Exception {
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(ws1)");
        expected.add("(ws10)");
        expected.add("(ws2)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractRegisteredUsersInPersistentWs() throws Exception {
        Parameters.WS.put(context, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.REGISTERED.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(ws1)");
        expected.add("(ws10)");
        expected.add("(default)");

        assertEquals(actual, expected);

        actual = new HashSet<>();

        iterator = PigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        expected = new HashSet<>();
        expected.add("(user1)");
        expected.add("(user10)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }
}
