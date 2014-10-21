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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestAddLogoutInterval extends BaseTest {

    private Context.Builder builder;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(new Event.Builder().withDate("2014-01-01")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "session-usage")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user1")
                                      .withParam("PARAMETERS", "USAGE-TIME=60000,START-TIME=1388563200000,SESSION-ID=1")
                                      .build());
        events.add(Event.Builder.createUserSSOLoggedOutEvent("user1").withDate("2014-01-01").withTime("10:02:00").build());
        events.add(Event.Builder.createUserSSOLoggedOutEvent("user1").withDate("2014-01-01").withTime("10:03:00").build());


        File log = LogGenerator.generateLog(events);

        builder = new Context.Builder();

        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void testUseCloserLogoutInterval() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_TIME_TOTAL);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 120000L);
    }
}


