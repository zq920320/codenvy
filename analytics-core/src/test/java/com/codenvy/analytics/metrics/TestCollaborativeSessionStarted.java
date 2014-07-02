/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestCollaborativeSessionStarted extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.collaborativeSessionStartedEvent("ws1", "user1", "session1")
                                .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.collaborativeSessionStartedEvent("ws2", "user2", "session2")
                                .withDate("2013-02-10").withTime("10:01:00").build());
        events.add(Event.Builder.collaborativeSessionStartedEvent("ws3", "user3", "session3")
                                .withDate("2013-02-10").withTime("10:02:00").build());
        events.add(Event.Builder.collaborativeSessionStartedEvent("ws4", "user4", "session4")
                                .withDate("2013-02-10").withTime("10:03:00").build());
        events.add(Event.Builder.collaborativeSessionStartedEvent("ws5", "user5", "session5")
                                .withDate("2013-02-11").withTime("10:04:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.COLLABORATIVE_SESSIONS_STARTED).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());
    }

    @Test
    public void testUserEvents() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.COLLABORATIVE_SESSIONS_STARTED);
        LongValueData lvd = (LongValueData)metric.getValue(builder.build());

        assertEquals(lvd.getAsLong(), 4);

    }
}
