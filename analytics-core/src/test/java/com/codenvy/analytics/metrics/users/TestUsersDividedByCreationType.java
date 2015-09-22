/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
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
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestUsersDividedByCreationType extends BaseTest {

    public static final String TEST_DATE_BEFORE = "20130101";
    public static final String TEST_DATE = "20130102";
    public static final String TEST_USER1 = "user1@gmail.com";
    public static final String TEST_USER2 = "user2@gmail.com";
    public static final String USER = "user";
    public static final String TEST_USER3 = "user3@gmail.com";
    public static final String TEST_USER4 = "user4@gmail.com";

    @BeforeClass
    public void init() throws Exception {
        addRegisteredUser(UID1, TEST_USER1);
        addRegisteredUser(UID2, TEST_USER2);
        addRegisteredUser(UID3, TEST_USER3);
        addRegisteredUser(UID4, TEST_USER4);

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent(UID1, TEST_USER1, TEST_USER1)
                                .withDate("2013-01-02", "09:00:00").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER1, UsersLoggedInWithGoogle.GOOGLE)
                                .withDate("2013-01-02", "10:00:00").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER1, UsersLoggedInWithForm.JAAS)
                                .withDate("2013-01-02", "11:00:00").build());

        events.add(Event.Builder.createUserCreatedEvent(UID2, TEST_USER2, TEST_USER2)
                                .withDate("2013-01-01", "09:00:02").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER2, UsersLoggedInWithGoogle.GOOGLE)
                                .withDate("2013-01-01", "10:00:01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER2, UsersLoggedInWithGitHub.GITHUB)
                                .withDate("2013-01-02", "10:00:02").build());

        events.add(Event.Builder.createUserSSOLoggedInEvent(USER, UsersLoggedInWithForm.SYSLDAP)
                                .withDate("2013-01-02", "11:00:03").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(USER, UsersLoggedInWithForm.SYSLDAP)
                                .withDate("2013-01-02", "11:00:05").build());

        events.add(Event.Builder.createUserCreatedEvent(UID3, TEST_USER3, TEST_USER3)
                                .withDate("2013-01-02", "09:00:05").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER3, UsersLoggedInWithForm.EMAIL)
                                .withDate("2013-01-02", "10:00:05").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER3, UsersLoggedInWithGoogle.GOOGLE)
                                .withDate("2013-01-02", "11:00:05").build());

        events.add(Event.Builder.createUserCreatedEvent(UID4, TEST_USER4, TEST_USER4)
                                .withDate("2013-01-02", "09:08:02").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER4, UsersLoggedInWithGitHub.GITHUB)
                                .withDate("2013-01-02", "10:00:07").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER4, UsersLoggedInWithGoogle.GOOGLE)
                                .withDate("2013-01-02", "11:00:07").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, TEST_DATE_BEFORE);
        builder.put(Parameters.TO_DATE, TEST_DATE_BEFORE);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_USERS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_USERS, builder.build());

        builder.put(Parameters.FROM_DATE, TEST_DATE);
        builder.put(Parameters.TO_DATE, TEST_DATE);
        pigServer.execute(ScriptType.CREATED_USERS, builder.build());
    }

    @Test
    public void testUsersCreatedFromEmailMetric() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_CREATED_FROM_EMAIL);
        assertEquals(metric.getValue(Context.EMPTY).getAsString(), "1");

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.USER);
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID3), "Actual value: " + m.toString());
    }

    @Test
    public void testUsersCreatedFromGoogleMetric() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_CREATED_FROM_GOOGLE);
        assertEquals(metric.getValue(Context.EMPTY).getAsString(), "2");

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.USER);
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey(UID1), "Actual value: " + m.toString());
        assertTrue(m.containsKey(UID2), "Actual value: " + m.toString());
    }

    @Test
    public void testUsersCreatedFromGoogleMetricAtCertainDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, TEST_DATE);
        builder.put(Parameters.TO_DATE, TEST_DATE);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_CREATED_FROM_GOOGLE);
        assertEquals(metric.getValue(builder.build()).getAsString(), "1");

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.USER);
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1), "Actual value: " + m.toString());
    }

    @Test
    public void testUsersCreatedFromGitHubMetric() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_CREATED_FROM_GITHUB);
        assertEquals(metric.getValue(Context.EMPTY).getAsString(), "1");

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.USER);
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID4), "Actual value: " + m.toString());
    }
}
