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
import com.codenvy.analytics.datamodel.ValueDataUtil;
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

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        // broken event, factory url contains new line character
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factoryUrl1\n", "referrer2", "org3", "affiliate2")
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
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS,
                                                MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }

    @Test
    public void testAbstractTopFactories() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_LIFETIME.toString());
        
        Metric metric = MetricFactory.getMetric(MetricType.TOP_FACTORIES);
        ListValueData valueData = ValueDataUtil.getAsList(metric, builder.build());

        assertEquals(valueData.size(), 0);
    }
}