/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.users.UsersActivityList;
import com.codenvy.analytics.pig.scripts.ScriptType;
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
public class TestUsersActivityMetric extends BaseTest {

    private static final String WS      = "ws1";
    private static final String WS_ID   = "wsid1";
    private static final String USER    = "user1@gmail.com";
    private static final String USER_ID = "uid1";

    private static final String WS2      = "ws2";
    private static final String WS2_ID   = "wsid2";
    private static final String USER2    = "user2@gmail.com";
    private static final String USER2_ID = "uid2";

    private static final String TMP_WS      = "tmp-ws";
    private static final String TMP_WS_ID   = "wsid3";
    private static final String TMP_USER    = "anonymoususer_5xhz40";
    private static final String TMP_USER_ID = "uid3";

    private static final String SESSION_ID = "8AA06F22-3755-4BDD-9242-8A6371BAB53A";

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent(USER_ID, USER, USER)
                                .withDate("2013-11-01").withTime("08:40:01").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WS, WS_ID, USER)
                                .withDate("2013-11-01").withTime("08:40:02").build());

        events.add(Event.Builder.createUserCreatedEvent(USER2_ID, USER2, USER2)
                                .withDate("2013-11-01").withTime("08:40:03").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WS2, WS2_ID, USER2)
                                .withDate("2013-11-01").withTime("08:40:04").build());

        events.add(Event.Builder.createUserCreatedEvent(TMP_USER_ID, TMP_USER, TMP_USER)
                                .withDate("2013-11-01").withTime("08:40:05").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(TMP_WS, TMP_WS_ID, TMP_USER)
                                .withDate("2013-11-01").withTime("08:40:06").build());

        events.add(
                Event.Builder
                        .createSessionUsageEvent(USER, WS, SESSION_ID, "2013-11-01 19:00:00", "2013-11-01 20:04:00", false)
                        .withDate("2013-11-01").withTime("19:00:00").build());
        events.add(
                Event.Builder
                        .createSessionUsageEvent(TMP_USER, TMP_WS, "id3", "2013-11-01 21:00:00", "2013-11-01 21:05:00", false)
                        .withDate("2013-11-01").withTime("21:00:00").build());

        // event of target user in the target workspace and in time of first session
        events.add(Event.Builder.createRunStartedEvent(USER, WS, "project", "type", "id1").withDate("2013-11-01")
                                .withTime(
                                        "19:08:00,600").build());
        events.add(Event.Builder.createRunFinishedEvent(USER, WS, "project", "type", "id1", 120000).withDate("2013-11-01").withTime(
                "19:10:00,900").build());

        // event of target user in another workspace and in time of main session
        events.add(Event.Builder.createBuildStartedEvent(USER, WS2, "project", "type", "id2")
                                .withDate("2013-11-01").withTime("19:12:00,000").build());
        events.add(Event.Builder.createBuildFinishedEvent(USER, WS2, "project", "type", "id2", 120000)
                                .withDate("2013-11-01").withTime("19:14:00,000").build());

        // event of another user in the target workspace and in time of main session
        events.add(Event.Builder.createRunStartedEvent(USER2, WS2, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("19:08:00,000").build());
        events.add(Event.Builder.createRunFinishedEvent(USER2, WS2, "project", "type", "id1", 120000)
                                .withDate("2013-11-01").withTime("19:10:00,000").build());

        // event of target user in the target workspace and in time of second sessi
        events.add(Event.Builder.createDebugStartedEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("20:02:00,600").build());

        // event of target user in the target workspace and after the second session is finished
        events.add(Event.Builder.createProjectBuiltEvent(USER, WS, "project", "type", "id1")
                                .withDate("2013-11-01").withTime("20:04:00,320").build());

//
//        // factory session, won't be taken in account
//        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", TMP_WS, TMP_USER, "", "")
//                                .withDate("2013-11-01").withTime("21:00:00,155").build());
//        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", TMP_WS, TMP_USER)
//                                .withDate("2013-11-01").withTime("21:00:05,555").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST)
                                     .getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void testActivity() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);
        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 14);

        LOG.info(value.getAll().toString());

        assertItem(value,
                   0,
                   "user-created",
                   "",
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 08:40:01,000").getTime(),
                   0, 0);

        assertItem(value,
                   1,
                   "workspace-created",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 08:40:02,000").getTime(),
                   0, 0);

        assertItem(value,
                   2,
                   "user-created",
                   "",
                   USER2_ID,
                   fullDateFormatMils.parse("2013-11-01 08:40:03,000").getTime(),
                   0, 0);

        assertItem(value,
                   3,
                   "workspace-created",
                   WS2_ID,
                   USER2_ID,
                   fullDateFormatMils.parse("2013-11-01 08:40:04,000").getTime(),
                   0, 0);

        assertItem(value,
                   4,
                   "user-created",
                   "",
                   TMP_USER_ID,
                   fullDateFormatMils.parse("2013-11-01 08:40:05,000").getTime(),
                   0, 0);

        assertItem(value,
                   5,
                   "workspace-created",
                   TMP_WS_ID,
                   TMP_USER_ID,
                   fullDateFormatMils.parse("2013-11-01 08:40:06,000").getTime(),
                   0, 0);

        assertItem(value,
                   6,
                   "run-started",
                   WS2_ID,
                   USER2_ID,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   7,
                   "run-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   0, 0);

        assertItem(value,
                   8,
                   "run-finished",
                   WS2_ID,
                   USER2_ID,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   9,
                   "run-finished",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   0, 0);

        assertItem(value,
                   10,
                   "build-started",
                   WS2_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:12:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   11,
                   "build-finished",
                   WS2_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:14:00,000").getTime(),
                   0, 0);

        assertItem(value,
                   12,
                   "debug-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   0, 0);

        assertItem(value,
                   13,
                   "project-built",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,320").getTime(),
                   0, 0);

        metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY);
        Assert.assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(14));
    }

    @Test
    public void testOneSessionActivityWithHidedSessionEvents() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.SESSION_ID, SESSION_ID);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);
        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 6);

        long startTime = fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime();

        assertItem(value,
                   0,
                   "ide-opened",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime(),
                   0,
                   startTime - fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime());

        assertItem(value,
                   1,
                   "run-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() - startTime);

        assertItem(value,
                   2,
                   "run-finished",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() - startTime);

        assertItem(value,
                   3,
                   "debug-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() - startTime);

        assertItem(value,
                   4,
                   "ide-closed",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime() - startTime);

        assertItem(value,
                   5,
                   "idle",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime() - startTime);

        metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY);
        Assert.assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(3));
    }


    @Test
    public void testOneSessionActivity() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.SESSION_ID, SESSION_ID);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);
        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 6);

        long startTime = fullDateFormat.parse("2013-11-01 19:00:00").getTime();

        assertItem(value,
                   0,
                   "ide-opened",
                   null,
                   null,
                   fullDateFormat.parse("2013-11-01 19:00:00").getTime(),
                   0,
                   0);

        assertItem(value,
                   1,
                   "run-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() - startTime);

        assertItem(value,
                   2,
                   "run-finished",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() - startTime);

        assertItem(value,
                   3,
                   "debug-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() - startTime);

        assertItem(value,
                   4,
                   "ide-closed",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime() - startTime);

        assertItem(value,
                   5,
                   "idle",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime() - startTime);


        metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY);
        Assert.assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(3));
    }

    @Test
    public void testOneSessionActivityPagination() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.SESSION_ID, SESSION_ID);
        builder.put(Parameters.PAGE, 1);
        builder.put(Parameters.PER_PAGE, 2);

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 3);

        long startTime = fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime();

        assertItem(value,
                   0,
                   "ide-opened",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime() - startTime);

        assertItem(value,
                   1,
                   "run-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime() - startTime);

        assertItem(value,
                   2,
                   "run-finished",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:08:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime() - startTime);

        builder.put(Parameters.PAGE, 2);

        value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 3);

        assertItem(value,
                   0,
                   "debug-started",
                   WS_ID,
                   USER_ID,
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() -
                   fullDateFormatMils.parse("2013-11-01 19:10:00,900").getTime(),
                   fullDateFormatMils.parse("2013-11-01 20:02:00,600").getTime() - startTime);

        assertItem(value,
                   1,
                   "ide-closed",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime() - startTime);

        assertItem(value,
                   2,
                   "idle",
                   null,
                   null,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime(),
                   0,
                   fullDateFormatMils.parse("2013-11-01 20:04:00,000").getTime() - startTime);

        builder.put(Parameters.PAGE, 3);

        value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 0);
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
}
