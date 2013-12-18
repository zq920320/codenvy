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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersSessions extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void prepare() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        // sessions #1 - 240s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-11-01").withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws1", "ide", "1")
                        .withDate("2013-11-01").withTime("19:04:00").build());

        // sessions #2 - 300s
        events.add(Event.Builder.createSessionStartedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                        .withTime("20:05:00").build());

        // sessions #3 - 120s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                        .withDate("2013-11-01").withTime("18:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                        .withDate("2013-11-01").withTime("18:02:00").build());

        // by mistake
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                        .withTime("20:25:00").build());

        // session will be ignored,
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                        .withDate("2013-11-01").withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "tmp-1", "ide", "4")
                        .withDate("2013-11-01").withTime("20:05:00").build());


        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testuserssessions");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);
    }

    @Test
    public void shouldReturnListOfSessions() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");
        MetricFilter.USER.put(context, "user@gmail.com");

        Metric metric = new TestListValueResulted();

        ListValueData valueData = (ListValueData)metric.getValue(context);
        assertEquals(1, valueData.size());

        MapValueData items = (MapValueData)valueData.getAll().get(0);
        assertEquals(items.getAll().get("start_time").getAsString(), "2013-11-01 20:00:00");
        assertEquals(items.getAll().get("end_time").getAsString(), "2013-11-01 20:05:00");
        assertEquals(items.getAll().get("session_id").getAsString(), "2");
        assertEquals(items.getAll().get("value").getAsString(), "300");

        metric = new TestNumberOfUsersOfSessions();
        assertEquals(metric.getValue(context).getAsString(), "1");
    }

    @Test
    public void shouldReturnEmptyList() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");
        MetricFilter.USER.put(context, "user@gmail");

        Metric metric = new TestListValueResulted();

        ListValueData valueData = (ListValueData)metric.getValue(context);
        assertEquals(0, valueData.size());

        metric = new TestNumberOfUsersOfSessions();
        assertEquals(metric.getValue(context).getAsString(), "0");
    }

    public class TestNumberOfUsersOfSessions extends NumberOfUsersSessions {
        @Override
        public String getStorageTableBaseName() {
            return "testuserssessions-raw";
        }
    }

    public class TestListValueResulted extends AbstractListValueResulted {

        public TestListValueResulted() {
            super(MetricType.USERS_SESSIONS);
        }

        @Override
        public String getStorageTableBaseName() {
            return "testuserssessions-raw";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
