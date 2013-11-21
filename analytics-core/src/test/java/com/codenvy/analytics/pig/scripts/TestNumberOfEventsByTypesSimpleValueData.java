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
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AggregatedParametrizedResultMetric;
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
public class TestNumberOfEventsByTypesSimpleValueData extends BaseTest {

    private Map<String, String> params;
    private TestMetric          metric;

    @BeforeClass
    public void init() throws IOException {
        params = Utils.newContext();
        metric = new TestMetric();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1@yahoo.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:01")
                        .build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.PARAM.put(params, "WS");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, EventType.TENANT_CREATED.toString());
        Parameters.METRIC.put(params, "testnumberofeventsbytypessimplevaluedata");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.NUMBER_OF_EVENTS_BY_TYPES, params);

        events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1@yahoo.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:01")
                        .build());
        log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.NUMBER_OF_EVENTS_BY_TYPES, params);
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.NUMBER_OF_EVENTS_BY_TYPES, params);

        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.size(), 2);
        assertEquals(tuple.get(0), dateFormat.parse("20130102").getTime());
        assertEquals(tuple.get(1).toString(), "(ws1,1)");

        tuple = iterator.next();
        assertEquals(tuple.size(), 2);
        assertEquals(tuple.get(0), dateFormat.parse("20130102").getTime());
        assertEquals(tuple.get(1).toString(), "(ws2,1)");

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testSingleDateFilterSingleParam() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1");

        assertEquals(metric.getValue(context), new LongValueData(1L));
    }

    @Test
    public void testSingleDateFilterDoubleParam() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1,ws2");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }


    @Test
    public void testDatePeriodFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1");
        MetricFilter.USER.put(context, "user1@gmail.com");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testSingleUserFilterShouldReturnZero() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1");
        MetricFilter.USER.put(context, "user2@gmail.com");

        assertEquals(metric.getValue(context), new LongValueData(0L));
    }

    @Test
    public void testDoubleUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1");
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testComplexFilterSingleParam() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1");
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");
        MetricFilter.WS.put(context, "ws1,ws2");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testComplexFilterDoubleParam() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        Parameters.PARAM.put(context, "ws1,ws2");
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");
        MetricFilter.WS.put(context, "ws1,ws2");

        assertEquals(metric.getValue(context), new LongValueData(4L));
    }

    public class TestMetric extends AggregatedParametrizedResultMetric {

        private TestMetric() {
            super("testnumberofeventsbytypessimplevaluedata");
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
