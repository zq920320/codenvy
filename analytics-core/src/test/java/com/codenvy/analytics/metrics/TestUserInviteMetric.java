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

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUserInviteMetric {

    private HashMap<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws1", "", "a@dot.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws1", "", "b@dot.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserInviteEvent("user2@gmail.com", "ws1", "", "c@dot.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws1", "", "d@dot.com").withDate("2013-01-02")
                        .build());
        events.add(Event.Builder.createUserInviteEvent("user3@gmail.com", "ws1", "", "e@dot.com").withDate("2013-01-02")
                        .build());
        events.add(Event.Builder.createUserInviteEvent("user3@gmail.com", "ws1", "", "f@dot.com").withDate("2013-01-02")
                        .build());
        events.add(
                Event.Builder.createUserAddedToWsEvent("user5@gmail.com", "ws1", "", "ws1", "user5@gmail.com", "invite")
                     .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createUserAddedToWsEvent("user6@gmail.com", "ws1", "", "ws1", "user6@gmail.com", "invite")
                     .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createUserAddedToWsEvent("user7@gmail.com", "ws1", "", "ws1", "user7@gmail.com", "invite")
                     .withDate("2013-01-01").build());


        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.LOG.put(context, log.getAbsolutePath());
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        DataProcessing.calculateAndStore(MetricType.USER_INVITE, context);
        DataProcessing.calculateAndStore(MetricType.USER_ADDED_TO_WORKSPACE, context);

        Map<String, String> clonedContext = Utils.clone(context);
        MetricParameter.FROM_DATE.put(clonedContext, "20130102");
        MetricParameter.TO_DATE.put(clonedContext, "20130102");
        DataProcessing.calculateAndStore(MetricType.USER_INVITE, clonedContext);
        DataProcessing.calculateAndStore(MetricType.USER_ADDED_TO_WORKSPACE, clonedContext);
    }

    @Test
    public void testGetValues() throws Exception {
        Metric metric = MetricFactory.createMetric(MetricType.USER_INVITE);
        LongValueData vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 3);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 3);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE_PERCENT);
        DoubleValueData dVD = (DoubleValueData)metric.getValue(context);
        assertEquals(dVD.getAsDouble(), 100.);
    }

    @Test
    public void testGetValuesAnotherPeriod() throws Exception {
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130102");

        Metric metric = MetricFactory.createMetric(MetricType.USER_INVITE);
        LongValueData vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 6);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 3);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE_PERCENT);
        DoubleValueData dVD = (DoubleValueData)metric.getValue(context);
        assertEquals(dVD.getAsDouble(), 50.);
    }

    @Test
    public void testGetValuesWithUserFilters() throws Exception {
        context.put(MetricFilter.USERS.name(), "user1@gmail.com");
        Metric metric = MetricFactory.createMetric(MetricType.USER_INVITE);
        LongValueData vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 2);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 0);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE_PERCENT);
        DoubleValueData dVD = (DoubleValueData)metric.getValue(context);
        assertEquals(dVD.getAsDouble(), 0.);

        context.put(MetricFilter.USERS.name(), "user1@gmail.com,user2@gmail.com");
        metric = MetricFactory.createMetric(MetricType.USER_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 3);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 0);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE_PERCENT);
        dVD = (DoubleValueData)metric.getValue(context);
        assertEquals(dVD.getAsDouble(), 0.);

        MetricFilter.USERS.remove(context);
        MetricFilter.DOMAINS.put(context, "gmail.com");
        metric = MetricFactory.createMetric(MetricType.USER_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 3);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 3);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE_PERCENT);
        dVD = (DoubleValueData)metric.getValue(context);
        assertEquals(dVD.getAsDouble(), 100.);

        MetricFilter.DOMAINS.remove(context);
        MetricFilter.USERS.put(context, "user5@gmail.com,user6@gmail.com");

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE);
        vd = (LongValueData)metric.getValue(context);
        assertEquals(vd.getAsLong(), 2);

        metric = MetricFactory.createMetric(MetricType.USER_ACCEPT_INVITE_PERCENT);
        dVD = (DoubleValueData)metric.getValue(context);
        assertEquals(dVD.getAsDouble(), Double.POSITIVE_INFINITY);

    }
}
