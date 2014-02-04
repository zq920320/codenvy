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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.sessions.factory.AbstractFactoryAction;
import com.codenvy.analytics.metrics.sessions.factory.ProductUsageFactorySessionsList;
import com.codenvy.analytics.metrics.top.AbstractTopFactories;
import com.codenvy.analytics.metrics.top.AbstractTopMetrics;
import com.codenvy.analytics.metrics.top.AbstractTopReferrers;
import com.codenvy.analytics.metrics.top.AbstractTopSessions;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestTopMetrics extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        Map<String, String> params = Utils.newContext();

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
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl0", "referrer1", "org1", "affiliate1")
                     .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "referrer2", "org2", "affiliate1")
                     .withDate("2013-02-10").withTime("11:00:01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "referrer2", "org3", "affiliate2")
                     .withDate("2013-02-10").withTime("11:00:02").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1")
                        .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1")
                        .withDate("2013-02-10").withTime("12:01:00").build());

        // run event for session #1
        events.add(Event.Builder.createRunStartedEvent("user1", "tmp-1", "project", "type", "id1")
                        .withDate("2013-02-10").withTime("10:03:00").build());

        events.add(Event.Builder.createProjectDeployedEvent("user1", "tmp-1", "session", "project", "type",
                                                            "local")
                        .withDate("2013-02-10")
                        .withTime("10:04:00")
                        .build());

        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "session", "project", "type")
                        .withDate("2013-02-10")
                        .withTime("10:04:00")
                        .build());


        // create user
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-3", "anonymoususer_1", "website")
                        .withDate("2013-02-10").build());

        events.add(Event.Builder.createUserChangedNameEvent("anonymoususer_1", "user4@gmail.com").withDate("2013-02-10")
                        .build());

        events.add(Event.Builder.createUserCreatedEvent("user-id2", "user4@gmail.com").withDate("2013-02-10").build());


        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testtopmetrics_acceptedfactories");
        Parameters.LOG.put(params, log.getAbsolutePath());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, params);

        Parameters.WS.put(params, Parameters.WS_TYPES.TEMPORARY.name());
        Parameters.STORAGE_TABLE.put(params, "testtopmetrics");
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, params);
    }

    @Test
    public void testAbstractTopSessions() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        AbstractTopSessions metric = new TestAbstractTopSessions(MetricType.TOP_FACTORY_SESSIONS_BY_LIFETIME,
                                                                 AbstractTopMetrics.LIFE_TIME_PERIOD);

        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 3);

        List<ValueData> all = value.getAll();
        checkTopSessionDataItem((MapValueData)all.get(0), "900", "factoryUrl1", "referrer2", "0", "0");
        checkTopSessionDataItem((MapValueData)all.get(1), "600", "factoryUrl1", "referrer2", "0", "1");
        checkTopSessionDataItem((MapValueData)all.get(2), "300", "factoryUrl0", "referrer1", "1", "1");
    }

    @Test
    public void testAbstractTopFactories() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        AbstractTopMetrics metric =
                new TestAbstractTopFactories(MetricType.TOP_FACTORIES_BY_LIFETIME, AbstractTopMetrics.LIFE_TIME_PERIOD);

        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 2);

        List<ValueData> all = value.getAll();
        checkTopFactoriesDataItem((MapValueData)all.get(0), "factoryUrl1", "1", "1", "1500", "0.0", "0.0", "0.0",
                                  "50.0", "50.0", "100.0", "0.0", "" + timeFormat.parse("20130210 10:20:00").getTime(),
                                  "" + timeFormat.parse("20130210 11:00:00").getTime());
        checkTopFactoriesDataItem((MapValueData)all.get(1), "factoryUrl0", "1", "0", "300", "100.0", "100.0", "100.0",
                                  "0.0", "100.0", "0.0", "100.0", "" + timeFormat.parse("20130210 10:00:00").getTime(),
                                  "" + timeFormat.parse("20130210 10:00:00").getTime());
    }

    private void checkTopSessionDataItem(MapValueData item, String time, String factory, String referrer,
                                         String convertedSession, String authenticatedSession) {
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.FACTORY).getAsString(), factory);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.REFERRER).getAsString(), referrer);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.CONVERTED_SESSION).getAsString(),
                     convertedSession);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.AUTHENTICATED_SESSION).getAsString(),
                     authenticatedSession);
    }

    private void checkTopFactoriesDataItem(MapValueData item,
                                           String factory,
                                           String wsCreated,
                                           String userCreated,
                                           String time,
                                           String buildRate,
                                           String runRate,
                                           String deployRate,
                                           String anonymousFactorySessionRate,
                                           String authenticatedFactorySessionRate,
                                           String abandonFactorySessionRate,
                                           String convertedFactorySessionRate,
                                           String firstSessionDate,
                                           String lastSessionDate) {
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.FACTORY).getAsString(), factory);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.WS_CREATED).getAsString(), wsCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.USER_CREATED).getAsString(), userCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(AbstractTopFactories.BUILD_RATE).getAsString(), buildRate);
        assertEquals(item.getAll().get(AbstractTopFactories.RUN_RATE).getAsString(), runRate);
        assertEquals(item.getAll().get(AbstractTopFactories.DEPLOY_RATE).getAsString(), deployRate);
        assertEquals(item.getAll().get(AbstractTopFactories.ANONYMOUS_FACTORY_SESSION_RATE).getAsString(),
                     anonymousFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopFactories.AUTHENTICATED_FACTORY_SESSION_RATE).getAsString(),
                     authenticatedFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopFactories.ABANDON_FACTORY_SESSION_RATE).getAsString(),
                     abandonFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopFactories.CONVERTED_FACTORY_SESSION_RATE).getAsString(),
                     convertedFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopFactories.FIRST_SESSION_DATE).getAsString(), firstSessionDate);
        assertEquals(item.getAll().get(AbstractTopFactories.LAST_SESSION_DATE).getAsString(), lastSessionDate);
    }

    @Test
    public void testAbstractTopReferrers() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        AbstractTopMetrics metric =
                new TestAbstractTopReferrers(MetricType.TOP_REFERRERS_BY_LIFETIME, AbstractTopMetrics.LIFE_TIME_PERIOD);

        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 2);

        List<ValueData> all = value.getAll();
        checkTopReferrersDataItem((MapValueData)all.get(0), "referrer2", "1", "1", "1500", "0.0", "0.0", "0.0", "50.0",
                                  "50.0", "100.0", "0.0", "" + timeFormat.parse("20130210 10:20:00").getTime(),
                                  "" + timeFormat.parse("20130210 11:00:00").getTime());
        checkTopReferrersDataItem((MapValueData)all.get(1), "referrer1", "1", "0", "300", "100.0", "100.0", "100.0",
                                  "0.0", "100.0", "0.0", "100.0", "" + timeFormat.parse("20130210 10:00:00").getTime(),
                                  "" + timeFormat.parse("20130210 10:00:00").getTime());
    }

    @Test
    public void testFactoriesRun() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestFactoriesRun();
        assertEquals(metric.getValue(context).getAsString(), "1");
    }

    @Test
    public void testFactoriesRunWithFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.FACTORY.put(context, "factoryUrl0");

        Metric metric = new TestFactoriesRun();
        assertEquals(metric.getValue(context).getAsString(), "1");

        MetricFilter.FACTORY.put(context, "factoryUrl1");
        metric = new TestFactoriesRun();
        assertEquals(metric.getValue(context).getAsString(), "0");
    }

    private void checkTopReferrersDataItem(MapValueData item,
                                           String referrer,
                                           String wsCreated,
                                           String userCreated,
                                           String time,
                                           String buildRate,
                                           String runRate,
                                           String deployRate,
                                           String anonymousFactorySessionRate,
                                           String authenticatedFactorySessionRate,
                                           String abandonFactorySessionRate,
                                           String convertedFactorySessionRate,
                                           String firstSessionDate,
                                           String lastSessionDate) {
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.REFERRER).getAsString(), referrer);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.WS_CREATED).getAsString(), wsCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.USER_CREATED).getAsString(), userCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(AbstractTopReferrers.BUILD_RATE).getAsString(), buildRate);
        assertEquals(item.getAll().get(AbstractTopReferrers.RUN_RATE).getAsString(), runRate);
        assertEquals(item.getAll().get(AbstractTopReferrers.DEPLOY_RATE).getAsString(), deployRate);
        assertEquals(item.getAll().get(AbstractTopReferrers.ANONYMOUS_FACTORY_SESSION_RATE).getAsString(),
                     anonymousFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopReferrers.AUTHENTICATED_FACTORY_SESSION_RATE).getAsString(),
                     authenticatedFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopReferrers.ABANDON_FACTORY_SESSION_RATE).getAsString(),
                     abandonFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopReferrers.CONVERTED_FACTORY_SESSION_RATE).getAsString(),
                     convertedFactorySessionRate);
        assertEquals(item.getAll().get(AbstractTopReferrers.FIRST_SESSION_DATE).getAsString(), firstSessionDate);
        assertEquals(item.getAll().get(AbstractTopReferrers.LAST_SESSION_DATE).getAsString(), lastSessionDate);
    }

    private class TestAbstractTopSessions extends AbstractTopSessions {

        public TestAbstractTopSessions(MetricType metricType, int dayCount) {
            super(metricType, dayCount);
        }

        @Override
        public String getDescription() {
            return null;
        }


        @Override
        public String getStorageCollectionName() {
            return "testtopmetrics";
        }
    }

    private class TestAbstractTopFactories extends AbstractTopFactories {

        public TestAbstractTopFactories(MetricType metricType, int dayCount) {
            super(metricType, dayCount);
        }

        @Override
        public String getDescription() {
            return null;
        }


        @Override
        public String getStorageCollectionName() {
            return "testtopmetrics";
        }
    }

    private class TestFactoriesRun extends AbstractFactoryAction {

        public TestFactoriesRun() {
            super(MetricType.FACTORIES_RUN, ProductUsageFactorySessionsList.RUN);
        }

        @Override
        public String getStorageCollectionName() {
            return "testtopmetrics";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestAbstractTopReferrers extends AbstractTopReferrers {

        public TestAbstractTopReferrers(MetricType metricType, int dayCount) {
            super(metricType, dayCount);
        }

        @Override
        public String getDescription() {
            return null;
        }


        @Override
        public String getStorageCollectionName() {
            return "testtopmetrics";
        }
    }
}