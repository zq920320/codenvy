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

import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUserProfileMetric {

    @BeforeMethod
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.LOG.put(context, log.getAbsolutePath());

        DataProcessing.calculateAndStore(MetricType.USER_UPDATE_PROFILE, context);
        DataProcessing.calculateAndStore(MetricType.USERS_COMPLETED_PROFILE, context);

        events = new ArrayList<>();
        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f3", "l3", "company", "22", "2")
                        .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile("user3@gmail.com", "f4", "l4", "company", "22", "2")
                        .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile("user4@gmail.com", "f4", "l4", "company", "22", "")
                        .withDate("2013-01-02").build());
        log = LogGenerator.generateLog(events);

        MetricParameter.FROM_DATE.put(context, "20130102");
        MetricParameter.TO_DATE.put(context, "20130102");
        MetricParameter.LOG.put(context, log.getAbsolutePath());

        DataProcessing.calculateAndStore(MetricType.USER_UPDATE_PROFILE, context);
        DataProcessing.calculateAndStore(MetricType.USERS_COMPLETED_PROFILE, context);
    }

    @Test
    public void testUser1Profile() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USERS.put(context, "user1@gmail.com");

        UserUpdateProfileMetric metric =
                (UserUpdateProfileMetric)MetricFactory.createMetric(MetricType.USER_UPDATE_PROFILE);
        ListStringValueData value = (ListStringValueData)metric.getValue(context);

        assertEquals(metric.getEmail(value), "user1@gmail.com");
        assertEquals(metric.getFirstName(value), "f3");
        assertEquals(metric.getLastName(value), "l3");
        assertEquals(metric.getCompany(value), "company");
        assertEquals(metric.getPhone(value), "22");
    }

    @Test
    public void testDefaultResult() throws Exception {
        UserUpdateProfileMetric metric =
                (UserUpdateProfileMetric)MetricFactory.createMetric(MetricType.USER_UPDATE_PROFILE);

        Map<String, String> context = Utils.newContext();
        ListStringValueData value = (ListStringValueData)metric.getValue(context);
        assertEquals(value.size(), 0);
    }

    @Test
    public void testUnExistedUserProfile() throws Exception {
        UserUpdateProfileMetric metric =
                (UserUpdateProfileMetric)MetricFactory.createMetric(MetricType.USER_UPDATE_PROFILE);

        Map<String, String> context = Utils.newContext();
        MetricFilter.USERS.put(context, "xxx@dot.com");

        ListStringValueData value = (ListStringValueData)metric.getValue(context);

        assertEquals(value.size(), 0);
    }

    @Test
    public void testUser2Profile() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USERS.put(context, "user2@gmail.com");

        UserUpdateProfileMetric metric =
                (UserUpdateProfileMetric)MetricFactory.createMetric(MetricType.USER_UPDATE_PROFILE);
        ListStringValueData value = (ListStringValueData)metric.getValue(context);

        assertEquals(metric.getEmail(value), "user2@gmail.com");
        assertEquals(metric.getFirstName(value), "f2");
        assertEquals(metric.getLastName(value), "l2");
        assertEquals(metric.getCompany(value), "company");
        assertEquals(metric.getPhone(value), "11");
    }

    public void testCompletedProfiles() throws Exception {
        Metric metric = MetricFactory.createMetric(MetricType.USERS_COMPLETED_PROFILE);

        Map<String, String> context = Utils.newContext();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");

        ValueData value = metric.getValue(context);
        assertEquals(value.getAsLong(), 2);

        MetricParameter.FROM_DATE.put(context, "20130102");
        MetricParameter.TO_DATE.put(context, "20130102");

        value = metric.getValue(context);
        assertEquals(value.getAsLong(), 4);
    }
}
