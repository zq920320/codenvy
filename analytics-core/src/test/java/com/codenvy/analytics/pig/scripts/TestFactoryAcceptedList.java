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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestFactoryAcceptedList extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void init() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory1", "referrer1")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory2", "referrer1")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory3", "referrer1")
                        .withDate("2013-02-10").withTime("11:30:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory2", "referrer2")
                        .withDate("2013-02-10").withTime("13:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory3", "referrer2")
                        .withDate("2013-02-10").withTime("14:00:00").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.TEMPORARY.name());
        Parameters.STORAGE_TABLE.put(params, "testfactoryacceptedlist");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.FACTORY_ACCEPTED_LIST, params);
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.FACTORY_ACCEPTED_LIST, params);

        assertTrue(iterator.hasNext());
        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 10:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(factory,factory1)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 11:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(factory,factory2)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 11:30:00").getTime());
        assertEquals(tuple.get(1).toString(), "(factory,factory3)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 13:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(factory,factory2)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130210 14:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(factory,factory3)");

        assertFalse(iterator.hasNext());
    }
}
