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

package com.codenvy.analytics.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.scripts.executor.pig.PigExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestExtractQueryParam extends BaseTest {

    private HashMap<String, String> context = new HashMap<>();

    @BeforeTest
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withParam("EVENT", "event1")
                                      .withParam("FACTORY-URL", "http://www.com?test=1&affiliateid=100")
                                      .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event2")
                                      .withParam("FACTORY-URL", "http://www.com?test=1&affiliateid=200&orgid=500")
                                      .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event3")
                                      .withParam("AFFILIATE-ID", "300")
                                      .withParam("FACTORY-URL", "http://www.com?test=1&affiliateid=100")
                                      .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event4")
                                      .withParam("FACTORY-URL", "http://www.com?affiliateid=400")
                                      .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event5")
                                      .withParam("AFFILIATE-ID", "")
                                      .withParam("FACTORY-URL", "http://www.com?affiliateid=400")
                                      .withDate("2013-01-01").build());
        events.add(new Event.Builder().withParam("EVENT", "event6")
                                      .withParam("FACTORY-URL", "http://www.com")
                                      .withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.LOG.put(context, log.getAbsolutePath());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.CASSANDRA_COLUMN_FAMILY.put(context, "fake");
        MetricParameter.CASSANDRA_STORAGE.put(context, "fake");
    }

    @Test
    public void testExtractQueryParam() throws Exception {
        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = PigExecutor.executeAndReturn(ScriptType.TEST_EXTRACT_QUERY_PARAM, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(event1,100)");
        expected.add("(event2,200)");
        expected.add("(event3,300)");
        expected.add("(event4,400)");
        expected.add("(event5,)");
        expected.add("(event6,)");

        assertEquals(actual, expected);
    }
}
