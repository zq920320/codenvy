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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.AbstractLongValueResulted;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
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
public class TestNumberOfFactorySessionsAndEvent extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void init() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "", "project", "type")
                        .withDate("2013-02-10").withTime("10:01:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "", "project", "type")
                        .withDate("2013-02-10").withTime("10:02:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-2", "user2", "true", "brType")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-2", "user2")
                        .withDate("2013-02-10").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id22", "tmp-22", "user22", "true", "brType")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id22", "tmp-22", "user22")
                        .withDate("2013-02-10").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id33", "tmp-33", "user33", "true", "brType")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id33", "tmp-33", "user33")
                        .withDate("2013-02-10").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-3", "user3", "true", "brType")
                        .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "tmp-3", "", "project", "type", "paas")
                        .withDate("2013-02-10").withTime("12:02:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id4", "tmp-4", "user4", "true", "brType")
                        .withDate("2013-02-10").withTime("13:00:00").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user5", "tmp-5", "", "project", "type", "paas")
                        .withDate("2013-02-10").withTime("13:58:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id5", "tmp-5", "user5")
                        .withDate("2013-02-10").withTime("14:00:00").build());

        events.add(Event.Builder.createSessionFactoryStoppedEvent("id6", "tmp-6", "user6")
                        .withDate("2013-02-10").withTime("15:00:00").build());


        // 2 events outside of sessions
        events.add(Event.Builder.createProjectBuiltEvent("user22", "tmp-22", "", "project", "type")
                        .withDate("2013-02-10").withTime("15:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user33", "tmp-33", "", "project", "type")
                        .withDate("2013-02-10").withTime("15:00:00").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.TEMPORARY.name());
        Parameters.EVENT.put(params, "project-built,application-created,project-destroyed");
        Parameters.STORAGE_DST.put(params, "testnumberoffactorysessionsandevent");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.NUMBER_OF_FACTORY_SESSIONS_WITH_EVENT, params);
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.NUMBER_OF_FACTORY_SESSIONS_WITH_EVENT, params);

        assertTrue(iterator.hasNext());
        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), dateFormat.parse("20130210").getTime());
        assertEquals(tuple.get(1).toString(), "(value,1)");

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestLongValuedMetric();
        assertEquals(metric.getValue(context), new LongValueData(1));
    }

    @Test
    public void testUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");
        MetricFilter.WS.put(context, "tmp-1");

        Metric metric = new TestLongValuedMetric();
        assertEquals(metric.getValue(context), new LongValueData(1));

        MetricFilter.WS.put(context, "tmp-2");

        metric = new TestLongValuedMetric();
        assertEquals(metric.getValue(context), new LongValueData(0));
    }


    private class TestLongValuedMetric extends AbstractLongValueResulted {

        private TestLongValuedMetric() {
            super("testnumberoffactorysessionsandevent");
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
