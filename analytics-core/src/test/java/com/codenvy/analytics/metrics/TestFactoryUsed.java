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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static org.testng.AssertJUnit.assertEquals;

/** @author Anatoliy Bazko */
public class TestFactoryUsed extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createFactoryUrlAcceptedEvent(TWID1, "factory/factory/?id=1", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-20").withTime("10:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent(TWID2, "factory/f?id=1", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-20").withTime("11:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent(TWID1, "factory/factory?id=1", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-20").withTime("12:00:00").build());

        File log = LogGenerator.generateLog(events);
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131120");
        builder.put(Parameters.TO_DATE, "20131120");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());
    }

    @Test
    public void testFactoryUsedByFactoryUrl() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY, "factory/factory?id=1");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_USED);
        LongValueData v = getAsLong(metric, builder.build());

        assertEquals(v.getAsLong(), 3L);
    }


    @Test
    public void testFactoryUsedByShorterFactoryUrl() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY, "factory/f?id=1");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_USED);
        LongValueData v = getAsLong(metric, builder.build());

        assertEquals(v.getAsLong(), 3L);
    }

    @Test
    public void testFactoryUsedByFactoryId() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY_ID, "1");

        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_USED);
        LongValueData v = getAsLong(metric, builder.build());

        assertEquals(v.getAsLong(), 3L);
    }
}
