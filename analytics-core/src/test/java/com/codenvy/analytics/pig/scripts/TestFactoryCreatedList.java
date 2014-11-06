/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 * @author Dmytro Nochevnov
 * */
public class TestFactoryCreatedList extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        addRegisteredUser(UID1, "user1@gmail.com");
        addRegisteredUser(UID2, "user2@gmail.com");
        addRegisteredUser(UID3, "user3@gmail.com");
        addRegisteredUser(UID4, "user4@gmail.com");

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createFactoryCreatedEvent("user1@gmail.com", "ws1",
                                                           "project1",
                                                           "type1",
                                                           "repo1",
                                                           "http://codenvy.com/factory?id=1",
                                                           "",
                                                           "").withDate("2013-01-01").withTime("13:00:00").build());
        events.add(Event.Builder.createFactoryCreatedEvent("user2@gmail.com", "ws1",
                                                           "project1",
                                                           "type1",
                                                           "repo1",
                                                           "factory2",
                                                           "",
                                                           "").withDate("2013-01-01").withTime("14:00:00").build());
        events.add(Event.Builder.createFactoryCreatedEvent("user3@gmail.com", "ws2",
                                                           "project1",
                                                           "type1",
                                                           "repo1",
                                                           "factory3",
                                                           "",
                                                           "").withDate("2013-01-01").withTime("15:00:00").build());
        events.add(Event.Builder.createFactoryCreatedEvent("user4@gmail.com", "ws3",
                                                           "project1",
                                                           "type1",
                                                           "repo1",
                                                           "factory4",
                                                           "",
                                                           "").withDate("2013-01-01").withTime("16:00:00").build());
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_FACTORIES, MetricType.CREATED_FACTORIES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_FACTORIES, builder.build());
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("http://codenvy.com/factory?id=1"),
                                                       new StringValueData("factory2"),
                                                       new StringValueData("factory3"),
                                                       new StringValueData("factory4"))));
    }


    @Test
    public void testSingleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(
                             Arrays.<ValueData>asList(new StringValueData("http://codenvy.com/factory?id=1"))));
    }

    @Test
    public void testSeveralFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, "user1@gmail.com OR user2@gmail.com OR user3@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("http://codenvy.com/factory?id=1"),
                                                       new StringValueData("factory2"),
                                                       new StringValueData("factory3"))));
    }

    @Test
    public void testEncodedFactoriesFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.ENCODED_FACTORY, 1);

        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(
                             Arrays.<ValueData>asList(new StringValueData("http://codenvy.com/factory?id=1"))));
    }

    @Test
    public void testNonEncodedFactoriesFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.ENCODED_FACTORY, 0);

        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(Arrays.<ValueData>asList(new StringValueData("factory2"),
                                                       new StringValueData("factory3"),
                                                       new StringValueData("factory4"))));
    }

    @Test
    public void testFactoryIdFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.FACTORY_ID, "1");

        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()),
                     new SetValueData(
                         Arrays.<ValueData>asList(new StringValueData("http://codenvy.com/factory?id=1"))));
    }
}
