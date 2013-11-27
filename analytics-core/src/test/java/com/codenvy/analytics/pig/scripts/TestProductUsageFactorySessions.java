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
public class TestProductUsageFactorySessions extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void init() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-1", "anonymoususer_1", "false", "brType")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", "tmp-1", "anonymoususer_1")
                        .withDate("2013-02-10").withTime("11:15:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project", "type")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "referrer1")
                        .withDate("2013-02-10").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_DST.put(params, "testproductusagefactorysessions_factories");
        Parameters.LOG.put(params, log.getAbsolutePath());
        PigServer.execute(ScriptType.FACTORY_ACCEPTED_LIST, params);

        Parameters.WS.put(params, Parameters.WS_TYPES.TEMPORARY.name());
        Parameters.STORAGE_DST.put(params, "testproductusagefactorysessions");
        Parameters.STORAGE_SRC.put(params, "testproductusagefactorysessions_factories");
        PigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, params);
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, params);

        assertTrue(iterator.hasNext());
        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 10:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(value,300)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 10:20:00").getTime());
        assertEquals(tuple.get(1).toString(), "(value,600)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 11:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(value,900)");

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestLongValuedMetric();
        assertEquals(metric.getValue(context), new LongValueData(1800));
    }

    @Test
    public void testUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.REFERRER.put(context, "referrer1");

        Metric metric = new TestLongValuedMetric();
        assertEquals(metric.getValue(context), new LongValueData(1800));
    }

    @Test
    public void testConvFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.CONVERTED_FACTORY_SESSION.put(context, "true");

        Metric metric = new TestLongValuedMetric();
        assertEquals(metric.getValue(context), new LongValueData(300));
    }

    @Test
    public void testAuthFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.AUTHENTICATED_FACTORY_SESSION.put(context, "false");

        Metric metric = new TestLongValuedMetric();
        assertEquals(metric.getValue(context), new LongValueData(900));
    }

    @Test
    public void testAbstractFactorySessions() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestAbstractFactorySessions("testproductusagefactorysessions", 0, 600, true, true);
        assertEquals(metric.getValue(context), new LongValueData(2));
    }

    private class TestLongValuedMetric extends AbstractLongValueResulted {

        private TestLongValuedMetric() {
            super("testproductusagefactorysessions");
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestAbstractFactorySessions extends AbstractFactorySessions {

        protected TestAbstractFactorySessions(String metricName, long min, long max, boolean includeMin,
                                              boolean includeMax) {
            super(metricName, min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageTable() {
            return "testproductusagefactorysessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
