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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codenvy.analytics.pig.scripts.util.Event.Builder.*;
import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActiveFactoriesSet extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("uid1", "a1", "a1").withDate("2013-01-01").withTime("10:00:00,000").build());

        events.add(createFactoryUrlAcceptedEvent("tmp-1", "factory1", "", "", "").withDate("2013-01-01").withTime("13:00:00").build());
        events.add(createSessionFactoryStartedEvent("id1", "tmp-1", "a1", "", "").withDate("2013-01-01").withTime("13:01:00").build());
        events.add(createSessionFactoryStoppedEvent("id1", "tmp-1", "a1").withDate("2013-01-01").withTime("13:02:00").build());

        events.add(createSessionFactoryStartedEvent("id1", "tmp-1", "a1", "", "").withDate("2013-01-02").withTime("13:01:00").build());
        events.add(createSessionFactoryStoppedEvent("id1", "tmp-1", "a1").withDate("2013-01-02").withTime("13:02:00").build());

        events.add(createFactoryUrlAcceptedEvent("tmp-2", "factory2", "", "", "").withDate("2013-01-03").withTime("13:00:00").build());
        events.add(createSessionFactoryStartedEvent("id2", "tmp-2", "a1", "", "").withDate("2013-01-03").withTime("13:01:00").build());
        events.add(createSessionFactoryStoppedEvent("id2", "tmp-2", "a1").withDate("2013-01-03").withTime("13:02:00").build());

        events.add(createFactoryUrlAcceptedEvent("tmp-3", "factory3", "", "", "").withDate("2013-01-04").withTime("13:00:00").build());
        events.add(createSessionFactoryStartedEvent("id3", "tmp-3", "a1", "", "").withDate("2013-01-04").withTime("13:01:00").build());
        events.add(createSessionFactoryStoppedEvent("id3", "tmp-3", "a1").withDate("2013-01-04").withTime("13:02:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());

        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20130103");
        builder.put(Parameters.TO_DATE, "20130103");
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20130104");
        builder.put(Parameters.TO_DATE, "20130104");
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS,
                                                MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST)
                                     .getParamsAsMap());

        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder.put(Parameters.FROM_DATE, "20130103");
        builder.put(Parameters.TO_DATE, "20130103");
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder.put(Parameters.FROM_DATE, "20130104");
        builder.put(Parameters.TO_DATE, "20130104");
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }

    @Test
    public void testOneDayFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()), new SetValueData(Arrays.<ValueData>asList(new StringValueData("factory1"))));
    }


    @Test
    public void testTwoDaysFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()), new SetValueData(Arrays.<ValueData>asList(new StringValueData("factory1"))));
    }

    @Test
    public void testSeveralDaysFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130104");

        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_FACTORIES_SET);
        assertEquals(metric.getValue(builder.build()), new SetValueData(Arrays.<ValueData>asList(new StringValueData("factory1"),
                                                                                                 new StringValueData("factory2"),
                                                                                                 new StringValueData("factory3"))));
    }
}
