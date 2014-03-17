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
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.AbstractLoggedInType;
import com.codenvy.analytics.metrics.users.UsersLoggedInTypes;
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
public class TestNumberOfUsersByTypes_UsersLoggedInTypes extends BaseTest {

    private TestUsersLoggedInTypes metric;

    @BeforeClass
    public void init() throws Exception {
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


        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());
        builder.put(Parameters.STORAGE_TABLE, "testnumberofusersbytypes_usersloggedintypes");
        builder.put(Parameters.EVENT, "user-sso-logged-in");
        builder.put(Parameters.PARAM, "USING");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    @Test
    public void testDatePeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 2);
        assertEquals(values.get("google"), new LongValueData(2));
        assertEquals(values.get("jaas"), new LongValueData(1));

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(builder.build()).getAsString(), "3");
    }

    @Test
    public void testDatePeriodUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("google"), new LongValueData(1));

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(builder.build()).getAsString(), "1");
    }

    @Test
    public void testWrongDatePeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 0);

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(builder.build()).getAsString(), "0");
    }

    @Test
    public void testComplexFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com,user1@yahoo.com");

        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("google"), new LongValueData(1));

        TestAbstractLoggedInType metric = new TestAbstractLoggedInType(new String[]{"google", "jaas"});
        assertEquals(metric.getValue(builder.build()).getAsString(), "1");
    }

    private class TestUsersLoggedInTypes extends UsersLoggedInTypes {
        @Override
        public String getStorageCollectionName() {
            return "testnumberofusersbytypes_usersloggedintypes";
        }
    }

    private class TestAbstractLoggedInType extends AbstractLoggedInType {

        public TestAbstractLoggedInType(String[] types) {
            super("testnumberofusersbytypes_usersloggedintypes", types);
        }


        @Override
        public String getStorageCollectionName() {
            return "testnumberofusersbytypes_usersloggedintypes";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
