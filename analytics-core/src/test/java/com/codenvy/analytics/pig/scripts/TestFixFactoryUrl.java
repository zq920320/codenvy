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

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mongodb.util.MyAsserts.assertFalse;
import static com.mongodb.util.MyAsserts.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestFixFactoryUrl extends BaseTest {

    private Context context;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withParam("EVENT", "event1")
                                      .withParam("FACTORY-URL", "http://www.com/factory?test=1")
                                      .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event2")
                                      .withParam("FACTORY-URL", "http://www.com/factory/?test=2")
                                      .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event3")
                                      .withParam("AFFILIATE-ID", "300")
                                      .withParam("FACTORY-URL", "http://www.com/factory?test=3")
                                      .withDate("2013-01-01").build());

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
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_FIX_FACTORY_URL, context);

        assertTrue(iterator.hasNext());
        assertEquals(iterator.next().get(0), "http://www.com/factory?test=1");
        assertEquals(iterator.next().get(0), "http://www.com/factory?test=2");
        assertEquals(iterator.next().get(0), "http://www.com/factory?test=3");
        assertFalse(iterator.hasNext());
    }
}
