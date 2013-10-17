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
import com.codenvy.analytics.scripts.executor.pig.PigExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestTimeBetweenEvents extends BaseTest {

    private Map<String, String> context = new HashMap<>();

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


        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.LOG.put(context, log.getAbsolutePath());
        MetricParameter.CASSANDRA_STORAGE.put(context, "fake");
        MetricParameter.CASSANDRA_COLUMN_FAMILY.put(context, "fake");
    }

    @Test
    public void testExecuteTestScript() throws Exception {
        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigExecutor.executeAndReturn(ScriptType.TEST_TIME_BETWEEN_PAIRS_OF_EVENTS, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(60)");
        expected.add("(120)");
        expected.add("(360)");

        assertEquals(expected, actual);
    }
}


