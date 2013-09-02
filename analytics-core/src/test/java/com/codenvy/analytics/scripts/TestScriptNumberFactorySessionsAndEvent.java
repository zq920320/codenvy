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
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
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
public class TestScriptNumberFactorySessionsAndEvent extends BaseTest {

    private File log;
    
    @BeforeTest
    public void setUp() throws Exception {
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


        log = LogGenerator.generateLog(events);
    }
    
    @Test
    public void testEventFound() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130210");
        params.put(MetricParameter.TO_DATE.name(), "20130210");
        params.put(MetricParameter.EVENT.name(), EventType.PROJECT_BUILT.toString() + "," +
                                                 EventType.APPLICATION_CREATED.toString() + "," +
                                                 EventType.PROJECT_DEPLOYED.toString());
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.TEMPORARY.name());


        LongValueData valueData =
                (LongValueData)executeAndReturnResult(ScriptType.FACTORY_SESSIONS_AND_EVENT, log, params);
        assertEquals(valueData.getAsLong(), 1);
    }
    
    @Test
    public void testEventByWsFound() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130210");
        params.put(MetricParameter.TO_DATE.name(), "20130210");
        params.put(MetricParameter.EVENT.name(), EventType.PROJECT_BUILT.toString() + "," +
                                                 EventType.APPLICATION_CREATED.toString() + "," +
                                                 EventType.PROJECT_DEPLOYED.toString());
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.TEMPORARY.name());


        MapStringLongValueData result =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.FACTORY_SESSIONS_AND_EVENT_BY_WS, log, params);
        assertEquals(result.getAll().size(), 1);
        assertTrue(result.getAll().containsKey("tmp-1"));
        assertEquals(result.getAll().get("tmp-1").longValue(), 1);
        
        params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130210");
        params.put(MetricParameter.TO_DATE.name(), "20130210");
        params.put(MetricParameter.EVENT.name(), EventType.SESSION_FACTORY_STARTED.toString() + "," +
                                                 EventType.SESSION_FACTORY_STOPPED.toString());
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.ANY.name());


        result = (MapStringLongValueData)executeAndReturnResult(ScriptType.FACTORY_SESSIONS_AND_EVENT_BY_WS, log, params);
        assertEquals(result.getAll().size(), 4);
        assertTrue(result.getAll().containsKey("tmp-1"));
        assertTrue(result.getAll().containsKey("tmp-22"));
        assertTrue(result.getAll().containsKey("tmp-33"));
        assertTrue(result.getAll().containsKey("tmp-2"));
        assertEquals(result.getAll().get("tmp-1").longValue(), 1);
        assertEquals(result.getAll().get("tmp-22").longValue(), 1);
        assertEquals(result.getAll().get("tmp-33").longValue(), 1);
        assertEquals(result.getAll().get("tmp-2").longValue(), 1);
    }
    
}

