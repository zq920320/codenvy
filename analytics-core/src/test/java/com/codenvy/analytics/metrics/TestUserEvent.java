/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.metrics.user_event.UserEvent;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

/**
 * 
 *
 * @author Alexander Reshetnyak
 */
public class TestUserEvent  extends BaseTest {

    @BeforeClass
    public void init() throws IOException, Exception {
        Map<String, String> params = Utils.newContext();
        
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserEvent("user-event-n1", 
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N1_1", "data_1"))
                                                 .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createUserEvent("user-event-n2", 
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N2_1", "data_2"),
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N2_2", "data_3"))
                                                 .withDate("2013-02-10").withTime("10:05:00").build());
        events.add(Event.Builder.createUserEvent("user-event-n3", 
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N3_1", "data_4"),
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N3_2", "data_5"),
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N3_3", "data_6"))
                                                 .withDate("2013-02-10").withTime("10:10:00").build());
        events.add(Event.Builder.createUserEvent("user-event-n2", 
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N2_1", "data_7"),
                                                 new AbstractMap.SimpleEntry("USER_PARAM_N2_2", "data_8"))
                                                 .withDate("2013-02-10").withTime("10:15:00").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testuserevent");
        Parameters.LOG.put(params, log.getAbsolutePath());
        Parameters.EVENT.put(params, "user-event");
        Parameters.PARAM.put(params, "ITEM");
        pigServer.execute(ScriptType.USER_EVENTS, params);
    }
    
    @Test
    public void testUserEvents() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        Metric metric = new TestMetricUserEvent();
        ListValueData lvd = (ListValueData) metric.getValue(context);
        
        MapValueData vd = (MapValueData) lvd.getAll().get(0);
        assertEquals(vd.getAll().get("user_event_name").getAsString(), "user-event-n3");
        assertEquals(vd.getAll().get("count"), new LongValueData(1L));
        
        vd = (MapValueData) lvd.getAll().get(1);
        assertEquals(vd.getAll().get("user_event_name").getAsString(), "user-event-n2");
        assertEquals(vd.getAll().get("count"), new LongValueData(2L));
        
        vd = (MapValueData) lvd.getAll().get(2);
        assertEquals(vd.getAll().get("user_event_name").getAsString(), "user-event-n1");
        assertEquals(vd.getAll().get("count"), new LongValueData(1L));
    }
    
    
    private class TestMetricUserEvent extends UserEvent {
     
        @Override
        public String getStorageCollectionName() {
            return getStorageCollectionName("testuserevent");
        }
    }
}
