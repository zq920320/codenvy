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
import java.io.IOException;
import java.util.*;

import static org.testng.Assert.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsageSessions extends BaseTest {

    private Map<String, String> params = new HashMap<>();

    @BeforeClass
    public void setUp() throws IOException {
        List<Event> events = new ArrayList<>();

        // sessions #1 - 300s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-01-01").withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-01-01").withTime("20:05:00").build());

        // sessions #2 - 300s
        events.add(Event.Builder.createSessionStartedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:05:00").build());
        // by mistake
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:15:00").build());

        // session will be ignored,
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                        .withDate("2013-01-01").withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                        .withDate("2013-01-01").withTime("20:05:00").build());


        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.CASSANDRA_STORAGE.put(params, "fake");
        Parameters.METRIC.put(params, "fake");
        Parameters.LOG.put(params, log.getAbsolutePath());
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.PRODUCT_USAGE_SESSIONS, params);

        Set<String> expected = new HashSet<>();
        expected.add("(user,user@gmail.com),(value,300)");
        expected.add("(user,ANONYMOUSUSER_user11),(value,300)");

        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.size(), 4);
        assertEquals(tuple.get(1).toString(), "(date,20130101)");
        expected.remove(tuple.get(2).toString() + "," + tuple.get(3).toString());

        tuple = iterator.next();
        assertEquals(tuple.size(), 4);
        assertEquals(tuple.get(1).toString(), "(date,20130101)");
        expected.remove(tuple.get(2).toString() + "," + tuple.get(3).toString());

        assertTrue(expected.isEmpty());
        assertFalse(iterator.hasNext());
    }
}
