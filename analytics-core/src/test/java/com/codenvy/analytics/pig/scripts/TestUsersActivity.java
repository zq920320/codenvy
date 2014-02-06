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
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.users.UsersActivity;
import com.codenvy.analytics.metrics.users.UsersActivityList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersActivity extends BaseTest {

    private Map<String, String> params;

    private static final String COLLECTION              = TestUsersActivity.class.getSimpleName().toLowerCase();
    private static final String FIRST_TARGET_SESSION_ID = "8AA06F22-3755-4BDD-9242-8A6371BAB53A";
    private static final String TARGET_USER             = "user1@gmail.com";
    private static final String TARGET_WORKSPACE        = "ws1";

    @BeforeClass
    public void prepare() throws Exception {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        // start main session
        events.add(
                Event.Builder.createSessionStartedEvent(TARGET_USER, TARGET_WORKSPACE, "ide", FIRST_TARGET_SESSION_ID)
                     .withDate("2013-11-01")
                     .withTime("19:00:00,155").build());

        // event of target user in the target workspace and in time of first session
        events.add(Event.Builder.createRunStartedEvent(TARGET_USER, TARGET_WORKSPACE, "project", "type", "id1")
                        .withDate("2013-11-01")
                        .withTime("19:08:00,600").build());
        events.add(Event.Builder.createRunFinishedEvent(TARGET_USER, TARGET_WORKSPACE, "project", "type", "id1")
                        .withDate("2013-11-01")
                        .withTime("19:10:00,900").build());

        // event of target user in another workspace and in time of main session
        events.add(Event.Builder.createBuildStartedEvent(TARGET_USER, "ws2", "project", "type", "id2")
                        .withDate("2013-11-01")
                        .withTime("19:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(TARGET_USER, "ws2", "project", "type", "id2")
                        .withDate("2013-11-01")
                        .withTime("19:14:00").build());

        // event of another user in the target workspace and in time of main session
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type", "id1")
                        .withDate("2013-11-01")
                        .withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type", "id1")
                        .withDate("2013-11-01")
                        .withTime("19:10:00").build());

        // session (1200 sec, 120 millisec) of another user in the target workspace and in time of first session
        events.add(Event.Builder.createSessionStartedEvent("user2@gmail.com", TARGET_WORKSPACE, "ide", "2")
                        .withDate("2013-11-01").withTime("19:06:00,100").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2@gmail.com", TARGET_WORKSPACE, "ide", "2")
                        .withDate("2013-11-01").withTime("19:20:00,220").build());

        // finish main session
        events.add(
                Event.Builder.createSessionFinishedEvent(TARGET_USER, TARGET_WORKSPACE, "ide", FIRST_TARGET_SESSION_ID)
                     .withDate("2013-11-01")
                     .withTime("19:55:00,555").build());

        // second micro-sessions (240 sec, 120 millisec) of target user in the target workspace
        events.add(Event.Builder.createSessionStartedEvent(TARGET_USER, TARGET_WORKSPACE, "ide", "1")
                        .withDate("2013-11-01")
                        .withTime("20:00:00,100").build());
        events.add(Event.Builder.createSessionFinishedEvent(TARGET_USER, TARGET_WORKSPACE, "ide", "1")
                        .withDate("2013-11-01")
                        .withTime("20:04:00,220").build());

        // event of target user in the target workspace and in time of second session
        events.add(Event.Builder.createDebugStartedEvent(TARGET_USER, TARGET_WORKSPACE, "project", "type", "id1")
                        .withDate("2013-11-01")
                        .withTime("20:02:00,600").build());

        // event of target user in the target workspace and after the second session is finished
        events.add(Event.Builder.createProjectBuiltEvent(TARGET_USER, TARGET_WORKSPACE, "project", "type", "id1")
                        .withDate("2013-11-01")
                        .withTime("20:04:00,320").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());

        Parameters.STORAGE_TABLE.put(params, COLLECTION);
        Parameters.LOG.put(params, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_ACTIVITY, params);

        String ProductUsageSessionsTableName =
                ((ReadBasedMetric)MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST))
                        .getStorageCollectionName();
        Parameters.STORAGE_TABLE.put(params, ProductUsageSessionsTableName);

        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(params, "testuserssessions-stat");
        Parameters.STORAGE_TABLE_USERS_PROFILES.put(params, "testuserssessions-profiles");
        Parameters.LOG.put(params, log.getAbsolutePath());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);
    }

    @Test
    public void testActivity() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        Metric metric = new TestUsersActivityList();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 14);

        assertItem(value,
                   0,
                   "session-started",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   "127.0.0.1 2013-11-01 19:00:00,155,000[main] [INFO] [HelloWorld 1010]  - EVENT#session-started# "
                   + "SESSION-ID#" + FIRST_TARGET_SESSION_ID + "# WS#" + TARGET_WORKSPACE + "# USER#" + TARGET_USER +
                   "# WINDOW#ide# ",
                   fullTimeFormat.parse("2013-11-01 19:00:00,155").getTime());

        assertItem(value,
                   1,
                   "session-started",
                   TARGET_WORKSPACE,
                   "user2@gmail.com",
                   null,
                   fullTimeFormat.parse("2013-11-01 19:06:00,100").getTime());

        assertItem(value,
                   2,
                   "run-started",
                   "ws2",
                   "user2@gmail.com",
                   null,
                   fullTimeFormat.parse("2013-11-01 19:08:00,000").getTime());

        assertItem(value,
                   3,
                   "run-started",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 19:08:00,600").getTime());

        assertItem(value,
                   4,
                   "run-finished",
                   "ws2",
                   "user2@gmail.com",
                   null,
                   fullTimeFormat.parse("2013-11-01 19:10:00,000").getTime());

        assertItem(value,
                   5,
                   "run-finished",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 19:10:00,900").getTime());

        assertItem(value,
                   6,
                   "build-started",
                   "ws2",
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 19:12:00,000").getTime());

        assertItem(value,
                   7,
                   "build-finished",
                   "ws2",
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 19:14:00,000").getTime());

        assertItem(value,
                   8,
                   "session-finished",
                   TARGET_WORKSPACE,
                   "user2@gmail.com",
                   null,
                   fullTimeFormat.parse("2013-11-01 19:20:00,220").getTime());

        assertItem(value,
                   9,
                   "session-finished",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   "127.0.0.1 2013-11-01 19:55:00,555,000[main] [INFO] [HelloWorld 1010]  - EVENT#session-finished# "
                   + "SESSION-ID#" + FIRST_TARGET_SESSION_ID + "# WS#" + TARGET_WORKSPACE + "# USER#" + TARGET_USER +
                   "# WINDOW#ide# ",
                   fullTimeFormat.parse("2013-11-01 19:55:00,555").getTime());

        assertItem(value,
                   10,
                   "session-started",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 20:00:00,100").getTime());

        assertItem(value,
                   11,
                   "debug-started",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 20:02:00,600").getTime());

        assertItem(value,
                   12,
                   "session-finished",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 20:04:00,220").getTime());

        assertItem(value,
                   13,
                   "project-built",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 20:04:00,320").getTime());

        metric = new TestNumberOfUsersOfActivity();
        Assert.assertEquals(metric.getValue(context).getAsString(), "14");
    }

    @Test
    public void testOneSessionActivity() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.SESSION_ID.put(context, FIRST_TARGET_SESSION_ID);

        Metric metric = new TestUsersActivityList();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 3);

        assertItem(value,
                   0,
                   "run-started",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 19:08:00,600").getTime());

        assertItem(value,
                   1,
                   "run-finished",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 19:10:00,900").getTime());

        assertItem(value,
                   2,
                   "debug-started",
                   TARGET_WORKSPACE,
                   TARGET_USER,
                   null,
                   fullTimeFormat.parse("2013-11-01 20:02:00,600").getTime());

        metric = new TestNumberOfUsersOfActivity();
        Assert.assertEquals(metric.getValue(context), LongValueData.valueOf(3));
    }


    private void assertItem(ListValueData items,
                            int itemIndex,
                            String event,
                            String ws,
                            String user,
                            String message,
                            long date) {

        Map<String, ValueData> itemContent = ((MapValueData)items.getAll().get(itemIndex)).getAll();
        assertEquals(itemContent.get("event"), StringValueData.valueOf(event));
        assertEquals(itemContent.get("ws"), StringValueData.valueOf(ws));
        assertEquals(itemContent.get("user"), StringValueData.valueOf(user));

        if (message != null) {
            assertEquals(itemContent.get("message"), StringValueData.valueOf(message));
        }

        Date date1 = new Date(((LongValueData)itemContent.get("date")).getAsLong());
        System.out.println(fullTimeFormat.format(date1));
        assertEquals(itemContent.get("date"), LongValueData.valueOf(date));
    }

    // ------------------------> Tested classes

    private class TestNumberOfUsersOfActivity extends UsersActivity {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }

    private class TestUsersActivityList extends UsersActivityList {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }
}
