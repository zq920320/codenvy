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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestNumberOfUsersByTypes_UsersAddedToWorkspaces extends BaseTest {


    @BeforeClass
    public void init() throws IOException {
        Map<String, String> params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserAddedToWsEvent("user1@gmail.com", "ws1", "", "", "", "website")
                        .withDate("2013-01-02")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createUserAddedToWsEvent("user2@gmail.com", "ws", "", "", "", "website")
                        .withDate("2013-01-02")
                        .withTime("10:00:01")
                        .build());
        events.add(Event.Builder.createUserAddedToWsEvent("user3@gmail.com", "ws", "", "", "", "invite")
                        .withDate("2013-01-02")
                        .withTime("10:00:02")
                        .build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.PARAM.put(params, "FROM");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, "user-added-to-ws");
        Parameters.STORAGE_TABLE.put(params, "testnumberofusersbytypes_usersaddedtoworkspaces");
        Parameters.LOG.put(params, log.getAbsolutePath());

        pigServer.execute(ScriptType.EVENTS_BY_TYPE, params);
    }

    @Test
    public void testDatePeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");

        Metric metric = new TestUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 2);
        assertEquals(values.get("website"), new LongValueData(2));
        assertEquals(values.get("invite"), new LongValueData(1));

        metric = new TestAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context).getAsString(), "2");
    }

    @Test
    public void testDatePeriodUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com");

        Metric metric = new TestUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("website"), new LongValueData(1));

        metric = new TestAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context).getAsString(), "1");
    }

    @Test
    public void testWrongDatePeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        Metric metric = new TestUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 0);

        metric = new TestAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context).getAsString(), "0");
    }

    @Test
    public void testComplexFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");
        MetricFilter.WS.put(context, "ws1,ws2");

        Metric metric = new TestUsersAddedToWorkspaces();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("website"), new LongValueData(1));

        metric = new TestAbstractUsersAddedToWorkspaces();
        assertEquals(metric.getValue(context).getAsString(), "1");
    }


    private class TestAbstractUsersAddedToWorkspaces extends AbstractUsersAddedToWorkspaces {

        private TestAbstractUsersAddedToWorkspaces() {
            super("testnumberofusersbytypes_usersaddedtoworkspaces", new String[]{"website"});
        }

        @Override
        public String getStorageCollectionName() {
            return "testnumberofusersbytypes_usersaddedtoworkspaces";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class TestUsersAddedToWorkspaces extends UsersAddedToWorkspaces {
        @Override
        public String getStorageCollectionName() {
            return "testnumberofusersbytypes_usersaddedtoworkspaces";
        }
    }
}
