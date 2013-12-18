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
import com.codenvy.analytics.metrics.AbstractLoggedInType;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.UsersLoggedInTypes;
import com.codenvy.analytics.pig.PigServer;
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
public class TestNumberOfUsersByTypes_UsersLoggedInTypes extends BaseTest {

    private TestUsersLoggedInTypes metric;

    @BeforeClass
    public void init() throws IOException {
        Map<String, String> params = Utils.newContext();
        metric = new TestUsersLoggedInTypes();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "google")
                        .withDate("2013-01-02")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                        .withDate("2013-01-02")
                        .withTime("10:00:01")
                        .build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "jaas")
                        .withDate("2013-01-02")
                        .withTime("10:00:02")
                        .build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.PARAM.put(params, "USING");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, EventType.USER_SSO_LOGGED_IN.toString());
        Parameters.STORAGE_TABLE.put(params, "testnumberofusersbytypes_usersloggedintypes");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.NUMBER_OF_EVENTS_BY_TYPES, params);
    }

    @Test
    public void testDatePeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 2);
        assertEquals(values.get("google"), new LongValueData(2));
        assertEquals(values.get("jaas"), new LongValueData(1));

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(context).getAsString(), "3");
    }

    @Test
    public void testDatePeriodUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("google"), new LongValueData(1));

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(context).getAsString(), "1");
    }

    @Test
    public void testWrongDatePeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 0);

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(context).getAsString(), "0");
    }

    @Test
    public void testComplexFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(context)).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("google"), new LongValueData(1));

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(context).getAsString(), "1");
    }

    private class TestUsersLoggedInTypes extends UsersLoggedInTypes {
        @Override
        public String getStorageTableBaseName() {
            return "testnumberofusersbytypes_usersloggedintypes";
        }
    }

    private class TestAbstractLoggedInType extends AbstractLoggedInType {

        public TestAbstractLoggedInType(String[] types) {
            super("testnumberofusersbytypes_usersloggedintypes", types);
        }


        @Override
        public String getStorageTableBaseName() {
            return "testnumberofusersbytypes_usersloggedintypes";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
