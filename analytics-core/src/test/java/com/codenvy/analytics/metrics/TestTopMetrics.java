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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.sessions.factory.ProductUsageFactorySessionsList;
import com.codenvy.analytics.metrics.top.TopFactories;
import com.codenvy.analytics.metrics.top.TopReferrers;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestTopMetrics extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
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
                Event.Builder
                        .createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl0", "http://referrer1", "org1", "affiliate1")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(
                Event.Builder
                        .createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "http://referrer2", "org2", "affiliate1")
                        .withDate("2013-02-10").withTime("11:00:01").build());
        events.add(
                Event.Builder
                        .createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "http://referrer2", "org3", "affiliate2")
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


        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }

    @Test
    public void testAbstractTopSessions() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_LIFETIME.toString());
        
        Metric metric = MetricFactory.getMetric(MetricType.TOP_FACTORY_SESSIONS);

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 3);

        List<ValueData> all = value.getAll();
        checkTopSessionDataItem((MapValueData)all.get(0), "900000", "id3", "factoryUrl1", "referrer2", "0", "0");
        checkTopSessionDataItem((MapValueData)all.get(1), "600000", "id2", "factoryUrl1", "referrer2", "0", "1");
        checkTopSessionDataItem((MapValueData)all.get(2), "300000", "id1", "factoryUrl0", "referrer1", "1", "1");
    }

    @Test
    public void testAbstractTopFactories() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        
        Metric metric = MetricFactory.getMetric(MetricType.TOP_FACTORIES);  // passed days count should be lifetime by default

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 2);

        List<ValueData> all = value.getAll();

        checkTopFactoriesDataItem((MapValueData)all.get(0), "factoryUrl1", "1", "1", "2", "1500000", "0.0", "0.0", "0.0",
                                  "50.0", "50.0", "100.0", "0.0"
                                 );
        checkTopFactoriesDataItem((MapValueData)all.get(1), "factoryUrl0", "1", "0", "1", "300000", "100.0", "100.0",
                                  "100.0",
                                  "0.0", "100.0", "0.0", "100.0"
                                 );
    }

    private void checkTopSessionDataItem(MapValueData item, String time, String sessionId, String factory, String referrer,
                                         String convertedSession, String authenticatedSession) {
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.SESSION_ID).getAsString(), sessionId);
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
                                           String sessions,
                                           String time,
                                           String buildRate,
                                           String runRate,
                                           String deployRate,
                                           String anonymousFactorySessionRate,
                                           String authenticatedFactorySessionRate,
                                           String abandonFactorySessionRate,
                                           String convertedFactorySessionRate) {
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.FACTORY).getAsString(), factory);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.WS_CREATED).getAsString(), wsCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.USER_CREATED).getAsString(), userCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.SESSIONS).getAsString(), sessions);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(TopFactories.BUILD_RATE).getAsString(), buildRate);
        assertEquals(item.getAll().get(TopFactories.RUN_RATE).getAsString(), runRate);
        assertEquals(item.getAll().get(TopFactories.DEPLOY_RATE).getAsString(), deployRate);
        assertEquals(item.getAll().get(TopFactories.ANONYMOUS_FACTORY_SESSION_RATE).getAsString(),
                     anonymousFactorySessionRate);
        assertEquals(item.getAll().get(TopFactories.AUTHENTICATED_FACTORY_SESSION_RATE).getAsString(),
                     authenticatedFactorySessionRate);
        assertEquals(item.getAll().get(TopFactories.ABANDON_FACTORY_SESSION_RATE).getAsString(),
                     abandonFactorySessionRate);
        assertEquals(item.getAll().get(TopFactories.CONVERTED_FACTORY_SESSION_RATE).getAsString(),
                     convertedFactorySessionRate);
    }

    @Test
    public void testAbstractTopReferrers() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_LIFETIME.toString());

        Metric metric = MetricFactory.getMetric(MetricType.TOP_REFERRERS);

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 2);

        List<ValueData> all = value.getAll();
        checkTopReferrersDataItem((MapValueData)all.get(0), "referrer2", "1", "1", "2", "1500000", "0.0", "0.0", "0.0",
                                  "50.0",
                                  "50.0", "100.0", "0.0"
                                 );
        checkTopReferrersDataItem((MapValueData)all.get(1), "referrer1", "1", "0", "1", "300000", "100.0", "100.0", "100.0",
                                  "0.0", "100.0", "0.0", "100.0"
                                 );
    }

    @Test
    public void testFactoriesRun() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_SESSIONS_WITH_RUN);
        assertEquals(metric.getValue(builder.build()).getAsString(), "1");
    }

    @Test
    public void testFactoriesRunWithFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(MetricFilter.FACTORY, "factoryUrl0");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_SESSIONS_WITH_RUN);
        assertEquals(metric.getValue(builder.build()).getAsString(), "1");

        builder.put(MetricFilter.FACTORY, "factoryUrl1");
        metric = MetricFactory.getMetric(MetricType.FACTORY_SESSIONS_WITH_RUN);
        assertEquals(metric.getValue(builder.build()).getAsString(), "0");
    }

    private void checkTopReferrersDataItem(MapValueData item,
                                           String referrer,
                                           String wsCreated,
                                           String userCreated,
                                           String sessions,
                                           String time,
                                           String buildRate,
                                           String runRate,
                                           String deployRate,
                                           String anonymousFactorySessionRate,
                                           String authenticatedFactorySessionRate,
                                           String abandonFactorySessionRate,
                                           String convertedFactorySessionRate) {
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.REFERRER).getAsString(), referrer);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.WS_CREATED).getAsString(), wsCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.USER_CREATED).getAsString(), userCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.SESSIONS).getAsString(), sessions);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(TopReferrers.BUILD_RATE).getAsString(), buildRate);
        assertEquals(item.getAll().get(TopReferrers.RUN_RATE).getAsString(), runRate);
        assertEquals(item.getAll().get(TopReferrers.DEPLOY_RATE).getAsString(), deployRate);
        assertEquals(item.getAll().get(TopReferrers.ANONYMOUS_FACTORY_SESSION_RATE).getAsString(),
                     anonymousFactorySessionRate);
        assertEquals(item.getAll().get(TopReferrers.AUTHENTICATED_FACTORY_SESSION_RATE).getAsString(),
                     authenticatedFactorySessionRate);
        assertEquals(item.getAll().get(TopReferrers.ABANDON_FACTORY_SESSION_RATE).getAsString(),
                     abandonFactorySessionRate);
        assertEquals(item.getAll().get(TopReferrers.CONVERTED_FACTORY_SESSION_RATE).getAsString(),
                     convertedFactorySessionRate);
    }
}
