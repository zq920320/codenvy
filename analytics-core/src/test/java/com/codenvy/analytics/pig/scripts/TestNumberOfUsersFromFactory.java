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
import com.codenvy.analytics.metrics.AggregatedResultMetric;
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
public class TestNumberOfUsersFromFactory extends BaseTest {

    private TestMetric          metric;
    private Map<String, String> params;

    @BeforeClass
    public void prepare() throws IOException {
        metric = new TestMetric();
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-1", "Anonymoususer_1", "website")
                        .withDate("2013-01-01").build());

        events.add(Event.Builder.createUserChangedNameEvent("user1@gmail.com", "user3@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_2", "user5@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_1", "user4@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_2", "user5@gmail.com").withDate("2013-01-01")
                        .build());

        events.add(Event.Builder.createUserCreatedEvent("user-id1", "user3@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id2", "user4@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id3", "Anonymoususer_1").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id4", "Anonymoususer_2").withDate("2013-01-01").build());


        File log = LogGenerator.generateLog(events);

        Parameters.LOG.put(params, log.getAbsolutePath());
        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.METRIC.put(params, "testnumberofusersfromfactory");

        PigServer.execute(ScriptType.NUMBER_OF_USERS_FROM_FACTORY, params);
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.NUMBER_OF_USERS_FROM_FACTORY, params);

        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), dateFormat.parse("20130101").getTime());
        assertEquals(tuple.get(1).toString(), "(value,1)");

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testSingleDateFilterSingleParam() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        assertEquals(metric.getValue(context), new LongValueData(1L));
    }

    @Test
    public void testSingleDateFilterDoubleParam() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130102");
        Parameters.TO_DATE.put(context, "20130102");

        assertEquals(metric.getValue(context), new LongValueData(0L));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user4@gmail.com");

        assertEquals(metric.getValue(context), new LongValueData(1L));
    }

    @Test
    public void testSingleUserFilterShouldReturnZero() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user2@gmail.com");

        assertEquals(metric.getValue(context), new LongValueData(0L));
    }

    @Test
    public void testNonExistedFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");
        MetricFilter.WS.put(context, "ws1");

        assertEquals(metric.getValue(context), new LongValueData(0L));
    }

    public class TestMetric extends AggregatedResultMetric {

        private TestMetric() {
            super("testnumberofusersfromfactory");
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
