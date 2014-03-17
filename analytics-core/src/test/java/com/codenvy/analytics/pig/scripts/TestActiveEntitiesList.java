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
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.DBObject;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActiveEntitiesList extends BaseTest {

    private static final String COLLECTION = TestActiveEntitiesList.class.getSimpleName().toLowerCase();

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                                .withDate("2013-01-01")
                                .withTime("10:00:01")
                                .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2@gmail.com")
                                .withDate("2013-01-01")
                                .withTime("10:00:02")
                                .build());
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());

        events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2@gmail.com")
                                .withDate("2013-01-02")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user1@gmail.com")
                                .withDate("2013-01-02")
                                .withTime("10:00:01")
                                .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws4", "user4@gmail.com")
                                .withDate("2013-01-02")
                                .withTime("10:00:02")
                                .build());
        log = LogGenerator.generateLog(events);

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = new TestedSetValueResulted();
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                               new StringValueData("ws2"))));
        metric = new TestedActiveUsersMetric();
        assertEquals(metric.getValue(builder.build()), new LongValueData(2));
    }

    @Test
    public void testDatePeriodFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = new TestedSetValueResulted();
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                               new StringValueData("ws2"),
                                                               new StringValueData("ws3"),
                                                               new StringValueData("ws4"))));

        metric = new TestedActiveUsersMetric();
        assertEquals(metric.getValue(builder.build()), new LongValueData(3));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = new TestedSetValueResulted();
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                               new StringValueData("ws3"))));

        metric = new TestedActiveUsersMetric();
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));
    }

    @Test
    public void testDoubleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com,user2@gmail.com");

        Metric metric = new TestedSetValueResulted();
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                               new StringValueData("ws2"),
                                                               new StringValueData("ws3"))));

        metric = new TestedActiveUsersMetric();
        assertEquals(metric.getValue(builder.build()), new LongValueData(2));
    }

    @Test
    public void testSeveralFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com,user2@gmail.com");
        builder.put(Parameters.WS, "ws2");

        Metric metric = new TestedSetValueResulted();
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws2"))));

        metric = new TestedActiveUsersMetric();
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));
    }

    // =======================> Tested Metrics

    private class TestedSetValueResulted extends AbstractSetValueResulted {

        public TestedSetValueResulted() {
            super(COLLECTION, "ws");
        }

        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestedActiveUsersMetric extends AbstractActiveEntities {

        public TestedActiveUsersMetric() {
            super(COLLECTION, new TestedBasedMetric(), "user");
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestedBasedMetric extends ReadBasedMetric {

        public TestedBasedMetric() {
            super(COLLECTION);
        }

        @Override
        public String[] getTrackedFields() {
            return new String[0];
        }

        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }

        @Override
        public DBObject[] getSpecificDBOperations(Context clauses) {
            return new DBObject[0];
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
