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
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.AbstractUsersAddedToWorkspaces;
import com.codenvy.analytics.metrics.users.UsersAddedToWorkspaces;
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
public class TestUsersAddedToWorkspaces extends BaseTest {

    private static final String COLLECTION = TestUsersAddedToWorkspaces.class.getSimpleName().toLowerCase();

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserAddedToWsEvent("user1@gmail.com", "ws1", "", "", "", "website")
                                .withDate("2013-01-02").withTime("10:00:00").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user2@gmail.com", "ws", "", "", "", "website")
                                .withDate("2013-01-02").withTime("10:00:01").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user3@gmail.com", "ws", "", "", "", "invite")
                                .withDate("2013-01-02").withTime("10:00:02").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user4@gmail.com", "ws2", "", "", "", "invite")
                                .withDate("2013-01-02").withTime("10:00:03").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user5@gmail.com", "ws2", "", "", "", "invite")
                                .withDate("2013-01-02").withTime("10:00:04").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.EVENT, "user-added-to-ws");
        builder.put(Parameters.PARAM, "FROM");
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    @Test
    public void testDatePeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 2);
        assertEquals(values.get("website"), LongValueData.valueOf(2));
        assertEquals(values.get("invite"), LongValueData.valueOf(3));

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(2));

        metric = new TestedInviteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(3));
    }

    @Test
    public void testDatePeriodUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(MetricFilter.USER, "user1@gmail.com");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("website"), LongValueData.valueOf(1));

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));
    }

    @Test
    public void testWrongDatePeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 0);

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(0));
    }

    @Test
    public void testComplexFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com OR user1@yahoo.com");
        builder.put(Parameters.WS, "ws1 OR ws2");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("website"), LongValueData.valueOf(1));

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));
    }

    @Test
    public void testComplexFilterWhenAllParamHasTilda() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "~ user1@gmail.com OR user2@gmail.com");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("invite"), LongValueData.valueOf(3));

        metric = new TestedInviteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(3));
    }

    //------------------------- Tested classed

    private class TestedInviteAbstractUsersAddedToWorkspaces extends AbstractUsersAddedToWorkspaces {

        private TestedInviteAbstractUsersAddedToWorkspaces() {
            super(COLLECTION, new String[]{"invite"});
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

    private class TestedWebsiteAbstractUsersAddedToWorkspaces extends AbstractUsersAddedToWorkspaces {

        private TestedWebsiteAbstractUsersAddedToWorkspaces() {
            super(COLLECTION, new String[]{"website"});
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

    private class TestedUsersAddedToWorkspaces extends UsersAddedToWorkspaces {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }
}
