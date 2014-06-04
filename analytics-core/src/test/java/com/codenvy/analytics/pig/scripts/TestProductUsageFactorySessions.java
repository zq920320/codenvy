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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsageFactorySessions extends BaseTest {

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
                        .createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "http://referrer1", "org1", "affiliate1")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(
                Event.Builder
                        .createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "http://referrer2", "org2", "affiliate1")
                        .withDate("2013-02-10").withTime("11:00:01").build());
        events.add(
                Event.Builder
                        .createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "http://referrer3", "org3", "affiliate2")
                        .withDate("2013-02-10").withTime("11:00:02").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1")
                                .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1")
                                .withDate("2013-02-10").withTime("12:01:00").build());

        // run event for session #1
        events.add(Event.Builder.createRunStartedEvent("user1", "tmp-1", "project", "type", "id1")
                                .withDate("2013-02-10").withTime("10:03:00").build());


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

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_TEMPORARY_WORKSPACES, MetricType.TEMPORARY_WORKSPACES_CREATED).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, builder.build());
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_PRODUCT_USAGE_TIME_TOTAL);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1800000L));

        metric = MetricFactory.getMetric(MetricType.FACTORY_SESSIONS);
        assertEquals(metric.getValue(builder.build()), new LongValueData(3));

        metric = MetricFactory.getMetric(MetricType.AUTHENTICATED_FACTORY_SESSIONS);
        assertEquals(metric.getValue(builder.build()), new LongValueData(2));

        metric = MetricFactory.getMetric(MetricType.CONVERTED_FACTORY_SESSIONS);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));
    }

    @Test
    public void testUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(MetricFilter.REFERRER, "referrer1");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_PRODUCT_USAGE_TIME_TOTAL);
        assertEquals(metric.getValue(builder.build()), new LongValueData(300000L));
    }

    @Test
    public void testAbstractFactorySessions() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_SESSIONS_BELOW_10_MIN);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));
    }

    @Test
    public void testShouldReturnCountSessionsWithRun() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_SESSIONS_WITH_RUN);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));
    }

    @Test
    public void testShouldReturnAllTWC() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.TEMPORARY_WORKSPACES_CREATED);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(2));
    }

    @Test
    public void testShouldReturnTWCForSpecificOrgId() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(MetricFilter.ORG_ID, "org1");

        Metric metric = MetricFactory.getMetric(MetricType.TEMPORARY_WORKSPACES_CREATED);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));
    }

    @Test
    public void testShouldReturnTWCForSpecificAffiliateId() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(MetricFilter.AFFILIATE_ID, "affiliate1");

        Metric metric = MetricFactory.getMetric(MetricType.TEMPORARY_WORKSPACES_CREATED);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(2));
    }

    @Test
    public void testSummarizedFactorySessions() throws Exception {
        Summaraziable metric = (Summaraziable)MetricFactory.getMetric(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
        ListValueData summaryValue = (ListValueData)metric.getSummaryValue(Context.EMPTY);

        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "3");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "1800000");
        assertEquals(summary.get(UsersStatisticsList.AUTHENTICATED_SESSION).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.CONVERTED_SESSION).getAsString(), "1");
    }

    @Test
    public void testSummarizedFactoryStatistics() throws Exception {
        Summaraziable metric = (Summaraziable)MetricFactory.getMetric(MetricType.FACTORY_STATISTICS_LIST);
        ListValueData summaryValue = (ListValueData)metric.getSummaryValue(Context.EMPTY);

        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> summary = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(summary.get(UsersStatisticsList.SESSIONS).getAsString(), "3");
        assertEquals(summary.get(UsersStatisticsList.TIME).getAsString(), "1800000");
        assertEquals(summary.get(UsersStatisticsList.AUTHENTICATED_SESSION).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.CONVERTED_SESSION).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.RUNS).getAsString(), "1");
        assertEquals(summary.get(UsersStatisticsList.BUILDS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEBUGS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.DEPLOYS).getAsString(), "0");
        assertEquals(summary.get(UsersStatisticsList.WS_CREATED).getAsString(), "2");
        assertEquals(summary.get(UsersStatisticsList.ENCODED_FACTORY).getAsString(), "0");
    }
}
