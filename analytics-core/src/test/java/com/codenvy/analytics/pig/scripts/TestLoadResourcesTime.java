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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Alexander Reshetnyak
 */
public class TestLoadResourcesTime extends BaseTest {

    private Context context;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withParam("EVENT", "event")
                                      .withParam("USER", "user")
                                      .withParam("WS", "ws1")
                           .withParam("TIME", "1357034400000")  // 2013-01-01T12:00:00.000
                           .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event")
                                      .withParam("USER", "user")
                                      .withParam("WS", "ws2")
                                      .withDate("2013-01-01")
                                      .build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, "fake");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        context = builder.build();
    }

    @Test
    public void testExecuteTestScript() throws Exception {
        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_LOAD_RESOURCES, context);
        int count = 0;
        while (iterator.hasNext()) {
            assertTuple(iterator.next());
            count++;
        }
        assertEquals(count, 2);
    }

    private void assertTuple(Tuple t) throws ExecException {
        assertEquals(t.get(0).toString(), "event");
        assertEquals(t.get(1).toString(), "user");

        String ws = t.get(2).toString();
        String time = t.get(3).toString();
        if (ws.equals("ws1")) {
            assertEquals(time, "1357034400000");
        } else if (ws.equals("ws2")) {
            assertEquals(time, "1357027810000"); // default time @see Event.toString()
        } else {
            fail(ws + " unknown");
        }
    }
}

