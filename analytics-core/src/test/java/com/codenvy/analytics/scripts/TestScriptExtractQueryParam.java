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
import com.codenvy.analytics.metrics.value.MapStringSetValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptExtractQueryParam extends BaseTest {

    private HashMap<String, String> context;
    private File                    log;

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

        log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
    }

    @Test
    public void testExtractQueryParam() throws Exception {
        MapStringSetValueData valueData =
                (MapStringSetValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_QUERY_PARAM, log, context);

        assertValue(valueData, "event1", "100");
        assertValue(valueData, "event2", "200");
        assertValue(valueData, "event3", "300");
        assertValue(valueData, "event4", "400");
        assertValue(valueData, "event5", "");
        assertValue(valueData, "event6", "");
    }

    private void assertValue(MapStringSetValueData valueData, String key, String expectedValue) {
        Map<String, SetStringValueData> items = valueData.getAll();

        assertTrue(items.containsKey(key));
        assertEquals(items.get(key).size(), 1);
        assertEquals(items.get(key).getAll().iterator().next(), expectedValue);
    }
}
