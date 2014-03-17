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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.metrics.top.AbstractTopFactories;
import com.codenvy.analytics.metrics.top.AbstractTopMetrics;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestBrokenFactoryUrl extends BaseTest {

    private static final String COLLECTION          = TestBrokenFactoryUrl.class.getSimpleName().toLowerCase();
    private static final String COLLECTION_ACCEPTED =
            TestBrokenFactoryUrl.class.getSimpleName().toLowerCase() + "accepted";

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        // broken event, factory url contains new line character
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factoryUrl1\n", "referrer2", "org3", "affiliate2")
                             .withDate("2013-02-10").withTime("11:00:03").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-4", "anonymoususer_2")
                                .withDate("2013-02-10").withTime("12:01:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id4", "tmp-4", "anonymoususer_2", "false", "brType")
                                .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id4", "tmp-4", "anonymoususer_2")
                                .withDate("2013-02-10").withTime("11:15:00").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, COLLECTION_ACCEPTED);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.put(Parameters.WS, Parameters.WS_TYPES.TEMPORARY.name());
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }

    @Test
    public void testAbstractTopFactories() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        AbstractTopMetrics metric =
                new TestAbstractTopFactories(MetricType.TOP_FACTORIES_BY_LIFETIME, AbstractTopMetrics.LIFE_TIME_PERIOD);

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 0);
    }

    // ------------------------> Tested Metrics

    private class TestAbstractTopFactories extends AbstractTopFactories {

        public TestAbstractTopFactories(MetricType metricType, int dayCount) {
            super(metricType, dayCount);
        }

        @Override
        public String getDescription() {
            return null;
        }


        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }
}