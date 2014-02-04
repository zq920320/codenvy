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
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.sessions.factory.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsageFactorySessions extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void init() throws Exception {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-2", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-2", "user1")
                        .withDate("2013-02-10").withTime("10:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-3", "anonymoususer_1", "false", "brType")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", "tmp-3", "anonymoususer_1")
                        .withDate("2013-02-10").withTime("11:15:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project", "type")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "referrer1", "org1", "affiliate1")
                     .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "referrer2", "org2", "affiliate1")
                     .withDate("2013-02-10").withTime("11:00:01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "referrer3", "org3", "affiliate2")
                     .withDate("2013-02-10").withTime("11:00:02").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1")
                        .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1")
                        .withDate("2013-02-10").withTime("12:01:00").build());

        // run event for session #1
        events.add(Event.Builder.createRunStartedEvent("user1", "tmp-1", "project", "type", "id1")
                        .withDate("2013-02-10").withTime("10:03:00").build());


        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testproductusagefactorysessions_acceptedfactories");
        Parameters.LOG.put(params, log.getAbsolutePath());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, params);

        Parameters.WS.put(params, Parameters.WS_TYPES.TEMPORARY.name());
        Parameters.STORAGE_TABLE.put(params, "testproductusagefactorysessions");
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, params);

        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testproductusagefactorysessions-tmpws");
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, params);
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestFactorySessionsProductUsageTotal();
        assertEquals(metric.getValue(context), new LongValueData(1800L));

        metric = new TestFactorySessions();
        assertEquals(metric.getValue(context), new LongValueData(3));

        metric = new TestAuthenticatedFactorySessions();
        assertEquals(metric.getValue(context), new LongValueData(2));

        metric = new TestConvertedFactorySessions();
        assertEquals(metric.getValue(context), new LongValueData(1));
    }

    @Test
    public void testUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.REFERRER.put(context, "referrer1");

        Metric metric = new TestFactorySessionsProductUsageTotal();
        assertEquals(metric.getValue(context), new LongValueData(300L));
    }

    @Test
    public void testAbstractFactorySessions() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestAbstractFactorySessions("testproductusagefactorysessions", 0, 600, true, true);
        assertEquals(metric.getValue(context), new LongValueData(2));
    }

    @Test
    public void testShouldReturnCountSessionsWithRun() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestAbstractFactorySessionsWithEvent();
        assertEquals(metric.getValue(context), LongValueData.valueOf(1));
    }

    @Test
    public void testShouldReturnAllTWC() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestTemporaryWorkspacesCreated();
        assertEquals(metric.getValue(context), LongValueData.valueOf(2));
    }

    @Test
    public void testShouldReturnTWCForSpecificOrgId() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.ORG_ID.put(context, "org1");

        Metric metric = new TestTemporaryWorkspacesCreated();
        assertEquals(metric.getValue(context), LongValueData.valueOf(1));
    }

    @Test
    public void testShouldReturnTWCForSpecificAffiliateId() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.AFFILIATE_ID.put(context, "affiliate1");

        Metric metric = new TestTemporaryWorkspacesCreated();
        assertEquals(metric.getValue(context), LongValueData.valueOf(2));
    }


    private class TestAbstractFactorySessionsWithEvent extends AbstractFactorySessionsWithEvent {
        public TestAbstractFactorySessionsWithEvent() {
            super("testproductusagefactorysessions");
        }

        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessions";
        }

        @Override
        public String[] getTrackedFields() {
            return new String[]{"run"};
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestTemporaryWorkspacesCreated extends TemporaryWorkspacesCreated {
        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessions-tmpws";
        }
    }

    private class TestFactorySessions extends FactorySessions {
        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessions";
        }
    }

    private class TestFactorySessionsProductUsageTotal extends FactorySessionsProductUsageTotal {

        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessions";
        }
    }

    private class TestAuthenticatedFactorySessions extends AuthenticatedFactorySessions {
        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessions";
        }
    }

    private class TestConvertedFactorySessions extends ConvertedFactorySessions {
        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessions";
        }
    }


    private class TestAbstractFactorySessions extends AbstractFactorySessions {

        protected TestAbstractFactorySessions(String metricName, long min, long max, boolean includeMin,
                                              boolean includeMax) {
            super(metricName, min, max, includeMin, includeMax);
        }

        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessions";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
