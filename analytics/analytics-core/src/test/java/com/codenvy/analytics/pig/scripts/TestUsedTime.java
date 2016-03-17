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
package com.codenvy.analytics.pig.scripts;


import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsedTime extends BaseTest {

    private Context.Builder builder;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(new Event.Builder().withDate("2013-01-01")
                                      .withParam("EVENT", "run-queue-waiting-finished")
                                      .withParam("USER", "user")
                                      .withParam("WS", "ws")
                                      .withParam("WAITING-TIME", "300").build());

        File log = LogGenerator.generateLog(events);

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.toString());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.toString());
        builder.put(Parameters.EVENT, "run-queue-waiting-finished");
        builder.put(Parameters.PARAM, "WAITING-TIME");
        builder.put(Parameters.STORAGE_TABLE, "time_in_run_queue");
        builder.put(Parameters.LOG, log.getAbsolutePath());
    }

    @Test
    public void testExecute() throws Exception {
        pigServer.execute(ScriptType.USED_TIME, builder.build());

        Metric metric = MetricFactory.getMetric(MetricType.TIME_IN_RUN_QUEUE);
        LongValueData data = ValueDataUtil.getAsLong(metric, Context.EMPTY);

        assertEquals(data.getAsLong(), 300);
    }
}


