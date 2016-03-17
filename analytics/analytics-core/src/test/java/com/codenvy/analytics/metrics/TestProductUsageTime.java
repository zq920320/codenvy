/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
import com.codenvy.analytics.metrics.sessions.AbstractProductUsage;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageTime;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageUsers;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestProductUsageTime extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
    }

    @Test
    public void testUsageTimeIncludedAll() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");
        builder.put(Parameters.USER, "user1@gmail.com OR user2@gmail.com");
        builder.put(Parameters.WS, "ws1");

        Metric metric = new TestAbstractProductUsageTime(300000, 600000, true, true);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(900000));

        metric = new TestAbstractProductUsageSessions(300000, 600000, true, true);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(2));

        metric = new TestProductUsageUsers(300000, 600000, true, true);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(2));
    }

    @Test
    public void testUsageTimeExcludedRight() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");

        Metric metric = new TestAbstractProductUsageTime(300000, 600000, true, false);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(300000));

        metric = new TestAbstractProductUsageSessions(300000, 600000, true, false);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));

        metric = new TestProductUsageUsers(300000, 600000, true, false);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));
    }

    @Test
    public void testUsageTimeExcludedAll() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");

        Metric metric = new TestAbstractProductUsageTime(300000, 600000, false, false);
        assertEquals(metric.getValue(builder.build()), LongValueData.DEFAULT);

        metric = new TestAbstractProductUsageSessions(300000, 600000, false, false);
        assertEquals(metric.getValue(builder.build()), LongValueData.DEFAULT);

        metric = new TestProductUsageUsers(300000, 600000, false, false);
        assertEquals(metric.getValue(builder.build()), LongValueData.DEFAULT);
    }

    private void prepareData() throws Exception {
        addRegisteredUser(UID1, "user1@gmail.com");
        addRegisteredUser(UID2, "user2@gmail.com");
        addPersistentWs(WID1, "ws1");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, initLog().getAbsolutePath());
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }


    private File initLog() throws Exception {
        List<Event> events = new ArrayList<>();

        // 10 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=1")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:10:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user1@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=1")
                                      .build());

        // 5 min
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user2@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:05:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws1")
                                      .withParam("USER", "user2@gmail.com")
                                      .withParam("PARAMETERS", "SESSION-ID=2")
                                      .build());

        return LogGenerator.generateLog(events);
    }

    // ------------------------> Tested classes

    private class TestProductUsageUsers extends AbstractProductUsageUsers {
        public TestProductUsageUsers(long min, long max, boolean includeMin, boolean includeMax) {
            super("fake", min, max, includeMin, includeMax);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestAbstractProductUsageTime extends AbstractProductUsageTime {
        public TestAbstractProductUsageTime(long min, long max, boolean includeMin, boolean includeMax) {
            super("fake", min, max, includeMin, includeMax);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestAbstractProductUsageSessions extends AbstractProductUsage {
        public TestAbstractProductUsageSessions(long min, long max, boolean includeMin, boolean includeMax) {
            super("fake", min, max, includeMin, includeMax);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
