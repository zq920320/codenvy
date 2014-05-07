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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
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

    private Context.Builder builder;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("USER", "user1")
                                      .withParam("WS", "ws1").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "[user2,user3]")
                                      .withParam("WS", "Tmp-2").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "user4")
                                      .withParam("WS", "Tmp-1").withDate("2013-01-01").build());

        // param contains value ' WS' and workspace name contains ' '
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("USER", "AnonymousUser_1")
                                      .withParam("WS", "ws 2").withParam("COMPANY", "Mints. WS")
                                      .withParam("PHONE", "123456").withDate("2013-01-01").build());

        // param contains value 'WS'
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "AnonymousUser_2")
                                      .withParam("WS", "Tmp-3").withParam("COMPANY", "Mints.WS")
                                      .withParam("PHONE", "123456").withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserAddedToWsEvent("default", "default", "default", "ws10", "user10", "website")
                                .withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, "fake");
        builder.put(Parameters.LOG, log.getAbsolutePath());
    }

    @Test
    public void testExtractAllUsers() throws Exception {
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, builder.build());
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
        expected.add("(anonymoususer_1)");
        expected.add("(anonymoususer_2)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractAnonymousUsers() throws Exception {
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANONYMOUS.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, builder.build());
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(anonymoususer_1)");
        expected.add("(anonymoususer_2)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractRegisteredUsers() throws Exception {
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, builder.build());
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
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, builder.build());
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(ws1)");
        expected.add("(ws10)");
        expected.add("(ws 2)");
        expected.add("(tmp-1)");
        expected.add("(tmp-2)");
        expected.add("(tmp-3)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractTmpWs() throws Exception {
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.TEMPORARY.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, builder.build());
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
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, builder.build());
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(ws1)");
        expected.add("(ws10)");
        expected.add("(ws 2)");
        expected.add("(default)");

        assertEquals(actual, expected);
    }

    @Test
    public void testExtractRegisteredUsersInPersistentWs() throws Exception {
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());

        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_WS, builder.build());
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(ws1)");
        expected.add("(ws10)");
        expected.add("(default)");

        assertEquals(actual, expected);

        actual = new HashSet<>();

        iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_USER, builder.build());
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
