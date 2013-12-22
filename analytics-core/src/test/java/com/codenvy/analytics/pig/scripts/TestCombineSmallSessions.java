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
public class TestCombineSmallSessions extends BaseTest {

    private HashMap<String, String> context = new HashMap<>();

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        // 6m, single big session
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

        // 1m
        events.add(Event.Builder.createSessionStartedEvent("user4", "ws1", "ide", "7").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user4", "ws1", "ide", "7").withDate("2013-01-01")
                        .withTime("20:00:30").build());


        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        Parameters.LOG.put(context, log.getAbsolutePath());
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.name());
        Parameters.USER.put(context, Parameters.USER_TYPES.ANY.name());
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");
        Parameters.STORAGE_TABLE.put(context, "fake");
    }

    @Test
    public void testExecuteScript() throws Exception {
        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_COMBINE_SMALL_SESSIONS, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(user1,360)");
        expected.add("(user2,120)");
        expected.add("(user3,300)");
        expected.add("(user4,60)");

        assertEquals(actual, expected);
    }
}

