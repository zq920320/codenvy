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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageSessions;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageTime;
import com.codenvy.analytics.metrics.sessions.AbstractProductUsageUsers;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsageTime extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        // sessions #1 - 240s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                                .withDate("2013-11-01").withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                                .withDate("2013-11-01").withTime("19:04:00").build());

        // sessions #2 - 300s
        events.add(Event.Builder.createSessionStartedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                                .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                                .withTime("20:05:00").build());

        // sessions #3 - 120s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                                .withDate("2013-11-01").withTime("18:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                                .withDate("2013-11-01").withTime("18:02:00").build());

        // by mistake
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                                .withTime("20:25:00").build());

        // session will be ignored,
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                                .withDate("2013-11-01").withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                                .withDate("2013-11-01").withTime("20:05:00").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());
        builder.put(Parameters.STORAGE_TABLE, "testproductusagesessions");
        builder.put(Parameters.STORAGE_TABLE_USERS_STATISTICS, "testproductusagesessions-stat");
        builder.put(Parameters.STORAGE_TABLE_USERS_PROFILES, "testproductusagesessions-profiles");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void testDateAndDoubleUserFilterMinIncludeMaxInclude() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, "user@gmail.com,ANONYMOUSUSER_user11");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(540000L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));

        metric = new TestProductUsageUsers(300000, 360000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testDateAndDoubleUserWsFilterMinIncludeMaxInclude() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, "user@gmail.com,ANONYMOUSUSER_user11");
        builder.put(Parameters.WS, "ws1");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(540000L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));

        metric = new TestProductUsageUsers(300000, 360000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));
    }

    @Test
    public void testDateAndUserFilterMinIncludeMaxInclude() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, "user@gmail.com");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(300000L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));

        metric = new TestProductUsageUsers(300000, 360000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));
    }


    @Test
    public void testDateFilterMinIncludeMaxInclude() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(540000L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));

        metric = new TestProductUsageUsers(300000, 360000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));
    }

    @Test
    public void testDateFilterMinIncludeMaxExclude() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, true, false);
        assertEquals(metric.getValue(builder.build()), new LongValueData(240000L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, true, false);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));

        metric = new TestProductUsageUsers(300000, 360000, true, false);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));
    }

    @Test
    public void testDateFilterMinExcludeMaxExclude() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, false, false);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, false, false);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));

        metric = new TestProductUsageUsers(300000, 360000, false, false);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));
    }

    @Test
    public void testDateFilterMinExcludeMaxInclude() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, false, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(300000L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, false, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));

        metric = new TestProductUsageUsers(300000, 360000, false, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));
    }

    @Test
    public void testDateFilterMinIncludeMaxIncludeNoData() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131102");
        builder.put(Parameters.TO_DATE, "20131102");

        Metric metric = new TestAbstractProductUsageTime(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));

        metric = new TestAbstractProductUsageSessions(240000, 300000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));

        metric = new TestProductUsageUsers(300000, 360000, true, true);
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));
    }

    public class TestProductUsageUsers extends AbstractProductUsageUsers {

        public TestProductUsageUsers(long min, long max, boolean includeMin, boolean includeMax) {
            super("testproductusagesessions", min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageCollectionName() {
            return "testproductusagesessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }


    public class TestAbstractProductUsageTime extends AbstractProductUsageTime {

        public TestAbstractProductUsageTime(long min, long max, boolean includeMin, boolean includeMax) {
            super("testproductusagesessions", min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageCollectionName() {
            return "testproductusagesessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestAbstractProductUsageSessions extends AbstractProductUsageSessions {

        public TestAbstractProductUsageSessions(long min, long max, boolean includeMin, boolean includeMax) {
            super("testproductusagesessions", min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageCollectionName() {
            return "testproductusagesessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
