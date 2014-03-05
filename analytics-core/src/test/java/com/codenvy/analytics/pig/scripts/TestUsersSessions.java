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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.sessions.ProductUsageSessions;
import com.codenvy.analytics.metrics.sessions.ProductUsageSessionsList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersSessions extends BaseTest {

    private static final String COLLECTION          = TestUsersSessions.class.getSimpleName().toLowerCase();
    private static final String COLLECTION_PROFILES = TestUsersSessions.class.getSimpleName().toLowerCase() + "prof";
    private static final String COLLECTION_STATS    = TestUsersSessions.class.getSimpleName().toLowerCase() + "stat";

    @BeforeClass
    public void prepare() throws Exception {
        Map<String, String> params = Utils.newContext();

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
        events.add(Event.Builder.createUserSSOLoggedOutEvent("user@gmail.com").withDate("2013-11-01")
                                .withTime("20:06:00").build());


        // sessions #3 - 120s
        events.add(Event.Builder.createSessionStartedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                                .withDate("2013-11-01").withTime("19:30:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("ANONYMOUSUSER_user11", "ws2", "ide", "3")
                                .withDate("2013-11-01").withTime("19:32:00").build());

        // by mistake
        events.add(Event.Builder.createSessionFinishedEvent("user@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                                .withTime("20:25:00").build());

        events.add(Event.Builder.createUserUpdateProfile("user@gmail.com", "", "", "company", "", "")
                                .withDate("2013-11-01").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, COLLECTION_PROFILES);
        Parameters.LOG.put(params, log.getAbsolutePath());

        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, params);

        Parameters.STORAGE_TABLE.put(params, COLLECTION);
        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(params, COLLECTION_STATS);
        Parameters.STORAGE_TABLE_USERS_PROFILES.put(params, COLLECTION_PROFILES);
        Parameters.LOG.put(params, log.getAbsolutePath());

        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);
    }

    @Test
    public void shouldReturnSessionsListForAnonUser() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");
        Parameters.SORT.put(context, "+date");
        MetricFilter.USER.put(context, "ANONYMOUSUSER_user11");

        Metric metric = new TestProductUsageSessionsList();

        ListValueData valueData = (ListValueData)metric.getValue(context);
        assertEquals(2, valueData.size());

        MapValueData items = (MapValueData)valueData.getAll().get(0);
        assertEquals(items.getAll().get("start_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime()));
        assertEquals(items.getAll().get("end_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:04:00,000").getTime()));
        assertEquals(items.getAll().get("session_id"), StringValueData.valueOf("1"));
        assertEquals(items.getAll().get("time"), LongValueData.valueOf(4 * 60 * 1000));
        assertEquals(items.getAll().get("domain"), StringValueData.valueOf(""));
        assertEquals(items.getAll().get("ws"), StringValueData.valueOf("ws1"));
        assertEquals(items.getAll().get("user_company"), StringValueData.valueOf(""));
        assertEquals(items.getAll().get("logout_interval"), LongValueData.valueOf(0));

        items = (MapValueData)valueData.getAll().get(1);
        assertEquals(items.getAll().get("start_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:30:00,000").getTime()));
        assertEquals(items.getAll().get("end_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:32:00,000").getTime()));
        assertEquals(items.getAll().get("session_id"), StringValueData.valueOf("3"));
        assertEquals(items.getAll().get("time"), LongValueData.valueOf(2 * 60 * 1000));
        assertEquals(items.getAll().get("domain"), StringValueData.valueOf(""));
        assertEquals(items.getAll().get("ws"), StringValueData.valueOf("ws2"));
        assertEquals(items.getAll().get("user_company"), StringValueData.valueOf(""));
        assertEquals(items.getAll().get("logout_interval"), LongValueData.valueOf(0));

        metric = new TestProductUsageSessions();
        assertEquals(metric.getValue(context), LongValueData.valueOf(2));
    }

    @Test
    public void shouldReturnSessionsListForRegisteredUser() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");
        MetricFilter.USER.put(context, "user@gmail.com");

        Metric metric = new TestProductUsageSessionsList();

        ListValueData valueData = (ListValueData)metric.getValue(context);
        assertEquals(1, valueData.size());

        MapValueData items = (MapValueData)valueData.getAll().get(0);
        assertEquals(items.getAll().get("start_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 20:00:00,000").getTime()));
        assertEquals(items.getAll().get("end_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 20:06:00,000").getTime()));
        assertEquals(items.getAll().get("session_id"), StringValueData.valueOf("2"));
        assertEquals(items.getAll().get("ws"), StringValueData.valueOf("ws1"));
        assertEquals(items.getAll().get("time"), LongValueData.valueOf(360000));
        assertEquals(items.getAll().get("domain"), StringValueData.valueOf("gmail.com"));
        assertEquals(items.getAll().get("user_company"), StringValueData.valueOf("company"));
        assertEquals(items.getAll().get("logout_interval"), LongValueData.valueOf(60 * 1000));

        metric = new TestProductUsageSessions();
        assertEquals(metric.getValue(context), LongValueData.valueOf(1));
    }

    @Test
    public void shouldReturnEmptyList() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");
        MetricFilter.USER.put(context, "user@gmail");

        Metric metric = new TestProductUsageSessionsList();

        ListValueData valueData = (ListValueData)metric.getValue(context);
        assertEquals(0, valueData.size());

        metric = new TestProductUsageSessions();
        assertEquals(metric.getValue(context).getAsString(), "0");
    }

    //----------------- Tested Metrics

    private class TestProductUsageSessions extends ProductUsageSessions {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }

    private class TestProductUsageSessionsList extends ProductUsageSessionsList {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }
}
