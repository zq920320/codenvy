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
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersActivity extends BaseTest {

    private static final String WS                  = "ws1";
    private static final String USER                = "user1@gmail.com";
    private static final String SESSION_ID          = "8AA06F22-3755-4BDD-9242-8A6371BAB53A";
    private static final String COLLECTION          = TestUsersActivity.class.getSimpleName().toLowerCase();
    private static final String NON_SESSIONS_EVENTS =
            "~session-started,~session-finished,~session-factory-started,~session-factory-stopped";

    @BeforeClass
    public void prepare() throws Exception {
        Map<String, String> params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        // start main session
        events.add(
                Event.Builder.createSessionStartedEvent(USER, WS, "ide", SESSION_ID)
                             .withDate("2013-11-01").withTime("19:00:00,155").build());

        // event of target user in the target workspace and in time of first session
        events.add(Event.Builder.createRunStartedEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("19:08:00,600").build());
        events.add(Event.Builder.createRunFinishedEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("19:10:00,900").build());

        // event of target user in another workspace and in time of main session
        events.add(Event.Builder.createBuildStartedEvent(USER, "ws2", "project", "type", "id2")
                                .withDate("2013-11-01").withTime("19:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(USER, "ws2", "project", "type", "id2")
                                .withDate("2013-11-01").withTime("19:14:00").build());

        // event of another user in the target workspace and in time of main session
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type", "id1")
                                .withDate("2013-11-01").withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type", "id1")
                                .withDate("2013-11-01").withTime("19:10:00").build());

        // finish main session
        events.add(
                Event.Builder.createSessionFinishedEvent(USER, WS, "ide", SESSION_ID)
                             .withDate("2013-11-01").withTime("19:55:00,555").build());

        // second micro-sessions (240 sec, 120 millisec) of target user in the target workspace
        events.add(Event.Builder.createSessionStartedEvent(USER, WS, "ide", "1")
                                .withDate("2013-11-01").withTime("20:00:00,100").build());

        // event of target user in the target workspace and in time of second session
        events.add(Event.Builder.createDebugStartedEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("20:02:00,600").build());

        events.add(Event.Builder.createSessionFinishedEvent(USER, WS, "ide", "1")
                                .withDate("2013-11-01").withTime("20:04:00,220").build());

        // event of target user in the target workspace and after the second session is finished
        events.add(Event.Builder.createProjectBuiltEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("20:04:00,320").build());

        // factory session, won't be taken in account
        events.add(Event.Builder.createSessionFactoryStartedEvent("sessionId1", "tmpWs", "tmpUser", "", "")
                                .withDate("2013-11-01").withTime("19:00:00,155").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("sessionId1", "tmpWs", "tmpUser")
                                .withDate("2013-11-01").withTime("19:55:00,555").build());


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

        Metric metric = new TestedUsersActivityList();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 12);

        assertItem(value,
                   0,
                   "session-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   0, 0);

        assertItem(value,
                   1,
                   "run-started",
                   "ws2",
                   "user2@gmail.com",
                   fullDateFormatMils.parse("2013-11-01 19:08:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   2,
                   "run-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   0, 0);

        assertItem(value,
                   3,
                   "run-finished",
                   "ws2",
                   "user2@gmail.com",
                   fullDateFormatMils.parse("2013-11-01 19:10:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   4,
                   "run-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   0, 0);

        assertItem(value,
                   5,
                   "build-started",
                   "ws2",
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:12:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   6,
                   "build-finished",
                   "ws2",
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:14:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   7,
                   "session-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime(),
                   0, 0);

        assertItem(value,
                   8,
                   "session-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime(),
                   0, 0);

        assertItem(value,
                   9,
                   "debug-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   0, 0);

        assertItem(value,
                   10,
                   "session-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   0, 0);

        assertItem(value,
                   11,
                   "project-built",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,320").getTime(),
                   0, 0);

        metric = new TestedNumberOfUsersOfActivity();
        Assert.assertEquals(metric.getValue(context), LongValueData.valueOf(12));
    }

    @Test
    public void testOneSessionActivityWithHidedSessionEvents() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.SESSION_ID.put(context, SESSION_ID);
        MetricFilter.EVENT.put(context, NON_SESSIONS_EVENTS);

        Metric metric = new TestedUsersActivityList();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 6);

        long startTime = fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime();

        assertItem(value,
                   0,
                   "ide-opened",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   0,
                   startTime - fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime());

        assertItem(value,
                   1,
                   "run-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() - startTime);

        assertItem(value,
                   2,
                   "run-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() - startTime);

        assertItem(value,
                   3,
                   "debug-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() - startTime);

        assertItem(value,
                   4,
                   "ide-closed",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);

        assertItem(value,
                   5,
                   "idle",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);

        metric = new TestedNumberOfUsersOfActivity();
        Assert.assertEquals(metric.getValue(context), LongValueData.valueOf(3));
    }


    @Test
    public void testOneSessionActivity() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.SESSION_ID.put(context, SESSION_ID);

        Metric metric = new TestedUsersActivityList();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 10);

        long startTime = fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime();

        assertItem(value,
                   0,
                   "ide-opened",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime() - startTime);

        assertItem(value,
                   1,
                   "session-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime() - startTime);

        assertItem(value,
                   2,
                   "run-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() - startTime);

        assertItem(value,
                   3,
                   "run-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() - startTime);

        assertItem(value,
                   4,
                   "session-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime() - startTime);

        assertItem(value,
                   5,
                   "session-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime() - startTime);

        assertItem(value,
                   6,
                   "debug-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() - startTime);

        assertItem(value,
                   7,
                   "session-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() -
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);

        assertItem(value,
                   8,
                   "ide-closed",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);

        assertItem(value,
                   9,
                   "idle",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);


        metric = new TestedNumberOfUsersOfActivity();
        Assert.assertEquals(metric.getValue(context), LongValueData.valueOf(7));
    }

    @Test
    public void testOneSessionActivityPagination() throws Exception {
        Metric metric = new TestedUsersActivityList();

        Map<String, String> context = Utils.newContext();
        MetricFilter.SESSION_ID.put(context, SESSION_ID);
        Parameters.PAGE.put(context, 1);
        Parameters.PER_PAGE.put(context, 3);

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 4);

        long startTime = fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime();

        assertItem(value,
                   0,
                   "ide-opened",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime() - startTime);

        assertItem(value,
                   1,
                   "session-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime() - startTime);

        assertItem(value,
                   2,
                   "run-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:00:00,155").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() - startTime);

        assertItem(value,
                   3,
                   "run-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() - startTime);

        Parameters.PAGE.put(context, 2);
        value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 3);

        assertItem(value,
                   0,
                   "session-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime() - startTime);

        assertItem(value,
                   1,
                   "session-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:55:00,555").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime() - startTime);

        assertItem(value,
                   2,
                   "debug-started",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 20:00:00,100").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() - startTime);

        Parameters.PAGE.put(context, 3);
        value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 3);

        assertItem(value,
                   0,
                   "session-finished",
                   WS,
                   USER,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() -
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);

        assertItem(value,
                   1,
                   "ide-closed",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);

        assertItem(value,
                   2,
                   "idle",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,220").getTime() - startTime);
    }

    private void assertItem(ListValueData items,
                            int itemIndex,
                            String event,
                            String ws,
                            String user,
                            long date,
                            long time,
                            long cumulativeTime) {

        Map<String, ValueData> itemContent = ((MapValueData)items.getAll().get(itemIndex)).getAll();
        assertEquals(itemContent.get(ReadBasedMetric.EVENT), StringValueData.valueOf(event));
        assertEquals(itemContent.get(ReadBasedMetric.WS), ws == null ? null : StringValueData.valueOf(ws));
        assertEquals(itemContent.get(ReadBasedMetric.USER), user == null ? null : StringValueData.valueOf(user));
        assertEquals(itemContent.get(ReadBasedMetric.DATE), LongValueData.valueOf(date));
        assertEquals(itemContent.get(UsersActivityList.CUMULATIVE_TIME), LongValueData.valueOf(cumulativeTime));
        assertEquals(itemContent.get(UsersActivityList.TIME), LongValueData.valueOf(time));
    }

    // ------------------------> Tested classes

    private class TestedNumberOfUsersOfActivity extends UsersActivity {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }

    private class TestedUsersActivityList extends UsersActivityList {
        private TestedUsersActivityList() {
            super(new TestedNumberOfUsersOfActivity());
        }

        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }
}
