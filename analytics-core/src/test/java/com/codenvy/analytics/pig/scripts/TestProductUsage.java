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
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsage extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void prepare() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        // sessions #1 - 240s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-01-01").withTime("19:04:00").build());

        // sessions #2 - 300s
        events.add(Event.Builder.createSessionStartedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        // sessions #3 - 120s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                        .withDate("2013-01-01").withTime("18:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                        .withDate("2013-01-01").withTime("18:02:00").build());

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
        Parameters.STORAGE_DST.put(params, "testproductusagesessions");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.PRODUCT_USAGE_SESSIONS, params);

        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.size(), 3);
        assertEquals(tuple.get(0), timeFormat.parse("20130101 20:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(user,user@gmail.com)");
        assertEquals(tuple.get(2).toString(), "(value,300)");

        assertTrue(iterator.hasNext());

        tuple = iterator.next();
        assertEquals(tuple.size(), 3);
        assertEquals(tuple.get(0), timeFormat.parse("20130101 19:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(user,ANONYMOUSUSER_user11)");
        assertEquals(tuple.get(2).toString(), "(value,240)");

        tuple = iterator.next();
        assertEquals(tuple.size(), 3);
        assertEquals(tuple.get(0), timeFormat.parse("20130101 18:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(user,ANONYMOUSUSER_user11)");
        assertEquals(tuple.get(2).toString(), "(value,120)");

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testDateAndDoubleUserFilterMinIncludeMaxInclude() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");
        MetricFilter.USER.put(context, "user@gmail.com,ANONYMOUSUSER_user11");

        Metric metric = new TestProductUsageTime(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(9L));

        metric = new TestProductUsageSessions(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(2L));

        metric = new TesttProductUsageUsers(300, 360, true, true);
        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testDateAndDoubleUserWsFilterMinIncludeMaxInclude() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");
        MetricFilter.USER.put(context, "user@gmail.com,ANONYMOUSUSER_user11");
        MetricFilter.WS.put(context, "ws1");

        Metric metric = new TestProductUsageTime(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(9L));

        metric = new TestProductUsageSessions(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(2L));

        metric = new TesttProductUsageUsers(300, 360, true, true);
        assertEquals(metric.getValue(context), new LongValueData(1L));
    }

    @Test
    public void testDateAndUserFilterMinIncludeMaxInclude() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");
        MetricFilter.USER.put(context, "user@gmail.com");

        Metric metric = new TestProductUsageTime(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(5L));

        metric = new TestProductUsageSessions(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(1L));

        metric = new TesttProductUsageUsers(300, 360, true, true);
        assertEquals(metric.getValue(context), new LongValueData(1L));
    }


    @Test
    public void testDateFilterMinIncludeMaxInclude() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        Metric metric = new TestProductUsageTime(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(9L));

        metric = new TestProductUsageSessions(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(2L));

        metric = new TesttProductUsageUsers(300, 360, true, true);
        assertEquals(metric.getValue(context), new LongValueData(1L));
    }

    @Test
    public void testDateFilterMinIncludeMaxExclude() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        Metric metric = new TestProductUsageTime(240, 300, true, false);
        assertEquals(metric.getValue(context), new LongValueData(4L));

        metric = new TestProductUsageSessions(240, 300, true, false);
        assertEquals(metric.getValue(context), new LongValueData(1L));

        metric = new TesttProductUsageUsers(300, 360, true, false);
        assertEquals(metric.getValue(context), new LongValueData(1L));
    }

    @Test
    public void testDateFilterMinExcludeMaxExclude() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        Metric metric = new TestProductUsageTime(240, 300, false, false);
        assertEquals(metric.getValue(context), new LongValueData(0L));

        metric = new TestProductUsageSessions(240, 300, false, false);
        assertEquals(metric.getValue(context), new LongValueData(0L));

        metric = new TesttProductUsageUsers(300, 360, false, false);
        assertEquals(metric.getValue(context), new LongValueData(0L));
    }

    @Test
    public void testDateFilterMinExcludeMaxInclude() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        Metric metric = new TestProductUsageTime(240, 300, false, true);
        assertEquals(metric.getValue(context), new LongValueData(5L));

        metric = new TestProductUsageSessions(240, 300, false, true);
        assertEquals(metric.getValue(context), new LongValueData(1L));

        metric = new TesttProductUsageUsers(300, 360, false, true);
        assertEquals(metric.getValue(context), new LongValueData(0L));
    }

    @Test
    public void testDateFilterMinIncludeMaxIncludeNoData() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");

        Metric metric = new TestProductUsageTime(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(0L));

        metric = new TestProductUsageSessions(240, 300, true, true);
        assertEquals(metric.getValue(context), new LongValueData(0L));

        metric = new TesttProductUsageUsers(300, 360, true, true);
        assertEquals(metric.getValue(context), new LongValueData(0L));
    }

    public class TesttProductUsageUsers extends AbstractProductUsageUsers {

        public TesttProductUsageUsers(long min, long max, boolean includeMin, boolean includeMax) {
            super("testproductusagesessions", min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageTable() {
            return "testproductusagesessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }


    public class TestProductUsageTime extends AbstractProductUsageTime {

        public TestProductUsageTime(long min, long max, boolean includeMin, boolean includeMax) {
            super("testproductusagesessions", min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageTable() {
            return "testproductusagesessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public class TestProductUsageSessions extends AbstractProductUsageSessions {

        public TestProductUsageSessions(long min, long max, boolean includeMin, boolean includeMax) {
            super("testproductusagesessions", min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageTable() {
            return "testproductusagesessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
