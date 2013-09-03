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
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapListLongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptFactorySessionType extends BaseTest {

    private File log;

    @BeforeTest
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();

        // 1 big session
        events.add(Event.Builder.createSessionFactoryStartedEvent("1", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("1", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("2", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:06:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("2", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:07:00").build());

        // 2
        events.add(Event.Builder.createSessionFactoryStartedEvent("3", "tmp-2", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("11:06:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("3", "tmp-2", "user1")
                        .withDate("2013-02-10").withTime("11:07:00").build());

        // 3
        events.add(Event.Builder.createSessionFactoryStartedEvent("4", "tmp-1", "user1", "false", "brType")
                        .withDate("2013-02-10").withTime("12:06:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("4", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("12:07:00").build());

        log = LogGenerator.generateLog(events);
    }

    @Test
    public void testEventFound() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130210");
        params.put(MetricParameter.TO_DATE.name(), "20130210");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.TEMPORARY.name());


        MapStringLongValueData valueData =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.FACTORY_SESSIONS_TYPE, log, params);


        assertEquals(valueData.size(), 2);
        assertEquals(valueData.getAll().get("true").longValue(), 2L);
        assertEquals(valueData.getAll().get("false").longValue(), 1L);
    }

    @Test
    public void testEventFoundByWs() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130210");
        params.put(MetricParameter.TO_DATE.name(), "20130210");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.TEMPORARY.name());


        MapListLongValueData valueData =
                (MapListLongValueData)executeAndReturnResult(ScriptType.FACTORY_SESSIONS_TYPE_BY_WS, log, params);

        assertEquals(valueData.size(), 3);
        assertEquals(valueData.getAll().get(new ListStringValueData(Arrays.asList("true", "tmp-1"))).longValue(), 1L);
        assertEquals(valueData.getAll().get(new ListStringValueData(Arrays.asList("false", "tmp-1"))).longValue(), 1L);
        assertEquals(valueData.getAll().get(new ListStringValueData(Arrays.asList("true", "tmp-2"))).longValue(), 1L);
    }
}

