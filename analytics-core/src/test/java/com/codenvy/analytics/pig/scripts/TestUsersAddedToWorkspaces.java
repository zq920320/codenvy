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
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
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
        Map<String, String> params = Utils.newContext();

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

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.PARAM.put(params, "FROM");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, "user-added-to-ws");
        Parameters.STORAGE_TABLE.put(params, COLLECTION);
        Parameters.LOG.put(params, log.getAbsolutePath());

        pigServer.execute(ScriptType.EVENTS_BY_TYPE, params);
    }

    @Test
    public void testDatePeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 2);
        assertEquals(values.get("website"), LongValueData.valueOf(2));
        assertEquals(values.get("invite"), LongValueData.valueOf(3));

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context), LongValueData.valueOf(2));

        metric = new TestedInviteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context), LongValueData.valueOf(3));
    }

    @Test
    public void testDatePeriodUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("website"), LongValueData.valueOf(1));

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context), LongValueData.valueOf(1));
    }

    @Test
    public void testWrongDatePeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 0);

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context), LongValueData.valueOf(0));
    }

    @Test
    public void testComplexFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");
        MetricFilter.WS.put(context, "ws1,ws2");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("website"), LongValueData.valueOf(1));

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context), LongValueData.valueOf(1));
    }

    @Test
    public void testComplexFilterWhenAllParamHasTilda() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "~user1@gmail.com,~user2@gmail.com");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("invite"), LongValueData.valueOf(3));

        metric = new TestedInviteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context), LongValueData.valueOf(3));
    }

    @Test
    public void testComplexFilterWhenSomeParamHasTilda() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com,~user3@gmail.com");

        Metric metric = new TestedUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("website"), LongValueData.valueOf(1));

        metric = new TestedWebsiteAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context), LongValueData.valueOf(1));
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
