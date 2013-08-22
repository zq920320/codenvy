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

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
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
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestActiveUsersMetric {

    private HashMap<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user1@gmail.com", "project1", "type", "feature").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user2@gmail.com", "project2", "type", "feature").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user1@gmail.com", "project1", "type", "feature").withDate("2013-01-02").build());
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user3@gmail.com", "project2", "type", "feature").withDate("2013-01-02").build());
        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.LOG.put(context, log.getAbsolutePath());
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        DataProcessing.calculateAndStore(MetricType.ACTIVE_USERS_SET, context);

        Map<String,String> clonedContext = Utils.clone(context);
        MetricParameter.FROM_DATE.put(clonedContext, "20130102");
        MetricParameter.TO_DATE.put(clonedContext, "20130102");
        DataProcessing.calculateAndStore(MetricType.ACTIVE_USERS_SET, clonedContext);
    }

    @Test
    public void testGetValues() throws Exception {
        Metric metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET);

        SetStringValueData setVD = (SetStringValueData) metric.getValue(context);
        assertEquals(setVD.size(), 2);
        assertTrue(setVD.getAll().contains("user1@gmail.com"));
        assertTrue(setVD.getAll().contains("user2@gmail.com"));

        metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS);

        LongValueData lVD = (LongValueData) metric.getValue(context);
        assertEquals(lVD.getAsLong(), 2);
    }

    @Test
    public void testGetValuesAnotherPeriod() throws Exception {
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130102");

        Metric metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET);

        SetStringValueData setVD = (SetStringValueData) metric.getValue(context);
        assertEquals(setVD.size(), 3);
        assertTrue(setVD.getAll().contains("user1@gmail.com"));
        assertTrue(setVD.getAll().contains("user2@gmail.com"));
        assertTrue(setVD.getAll().contains("user3@gmail.com"));

        metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS);

        LongValueData lVD = (LongValueData) metric.getValue(context);
        assertEquals(lVD.getAsLong(), 3);
    }

    @Test
    public void testGetValuesWithUserFilters() throws Exception {
        context.put(MetricFilter.USER.name(), "user1@gmail.com");

        Metric metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET);

        SetStringValueData setVD = (SetStringValueData) metric.getValue(context);
        assertEquals(setVD.size(), 1);
        assertTrue(setVD.getAll().contains("user1@gmail.com"));

        metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS);

        LongValueData lVD = (LongValueData) metric.getValue(context);
        assertEquals(lVD.getAsLong(), 1);


        context.put(MetricFilter.USER.name(), "user1@gmail.com,user2@gmail.com");

        metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET);

        setVD = (SetStringValueData) metric.getValue(context);
        assertEquals(setVD.size(), 2);
        assertTrue(setVD.getAll().contains("user1@gmail.com"));
        assertTrue(setVD.getAll().contains("user2@gmail.com"));

        metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS);

        lVD = (LongValueData) metric.getValue(context);
        assertEquals(lVD.getAsLong(), 2);

        context.put(MetricFilter.USER.name(), "@gmail.com");

        metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_SET);

        setVD = (SetStringValueData) metric.getValue(context);
        assertEquals(setVD.size(), 2);
        assertTrue(setVD.getAll().contains("user1@gmail.com"));
        assertTrue(setVD.getAll().contains("user2@gmail.com"));

        metric = MetricFactory.createMetric(MetricType.ACTIVE_USERS);

        lVD = (LongValueData) metric.getValue(context);
        assertEquals(lVD.getAsLong(), 2);
    }
}
