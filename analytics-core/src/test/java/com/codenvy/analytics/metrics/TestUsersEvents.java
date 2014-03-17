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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.users.UsersEvents;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
public class TestUsersEvents extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(
                Event.Builder.createUserEvent("user1", "action1").withDate("2013-02-10").withTime("10:00:00").build());
        events.add(
                Event.Builder.createUserEvent("user1", "action2").withDate("2013-02-10").withTime("10:01:00").build());
        events.add(
                Event.Builder.createUserEvent("user1", "action3").withDate("2013-02-10").withTime("10:02:00").build());
        events.add(
                Event.Builder.createUserEvent("user1", "action3").withDate("2013-02-10").withTime("10:03:00").build());
        events.add(
                Event.Builder.createUserEvent("user1", "action4").withDate("2013-02-10").withTime("10:04:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, "testusersevents");
        builder.put(Parameters.EVENT, "users-events");
        builder.put(Parameters.PARAM, "ACTION");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_EVENTS, builder.build());
    }

    @Test
    public void testUserEvents() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = new TestMetricUserEvent();
        ListValueData lvd = (ListValueData)metric.getValue(builder.build());

        assertEquals(lvd.size(), 4);

        for (ValueData object : lvd.getAll()) {
            MapValueData value = (MapValueData)object;
            if (value.getAll().get("action").getAsString().equals("action1")) {
                assertEquals(value.getAll().get("count"), new LongValueData(1L));

            } else if (value.getAll().get("action").getAsString().equals("action2")) {
                assertEquals(value.getAll().get("count"), new LongValueData(1L));

            } else if (value.getAll().get("action").getAsString().equals("action3")) {
                assertEquals(value.getAll().get("count"), new LongValueData(2L));

            } else if (value.getAll().get("action").getAsString().equals("action4")) {
                assertEquals(value.getAll().get("count"), new LongValueData(1L));

            } else {
                fail();
            }
        }
    }


    private class TestMetricUserEvent extends UsersEvents {
        @Override
        public String getStorageCollectionName() {
            return getStorageCollectionName("testusersevents");
        }
    }
}
