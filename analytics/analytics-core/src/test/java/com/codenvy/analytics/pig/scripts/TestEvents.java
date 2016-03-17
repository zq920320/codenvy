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
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestEvents extends BaseTest {

    private Context.Builder builder;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(new Event.Builder().withDate("2013-01-01")
                                      .withParam("EVENT", "build-queue-waiting-finished")
                                      .withParam("USER", "user")
                                      .withParam("WS", "ws")
                                      .withParam("WAITING-TIME", "1")
                                      .withParam("PARAMETERS", "PARAM-A=a").build());

        File log = LogGenerator.generateLog(events);

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.toString());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.toString());
        builder.put(Parameters.EVENT, "build-queue-waiting-finished");
        builder.put(Parameters.STORAGE_TABLE, "time_in_build_queue");
        builder.put(Parameters.LOG, log.getAbsolutePath());
    }

    @Test
    public void testExecute() throws Exception {
        pigServer.execute(ScriptType.EVENTS, builder.build());

        DBCollection collection = mongoDb.getCollection("time_in_build_queue");
        DBObject object = collection.findOne();

        assertEquals(object.get(AbstractMetric.USER), "user");
        assertEquals(object.get(AbstractMetric.WS), "ws");
        assertEquals(object.get("waiting_time"), 1L);
        assertEquals(object.get("param_a"), "a");
    }
}


