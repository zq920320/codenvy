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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersSessions extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
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

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_UPDATE_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void shouldReturnSessionsListForAnonUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.SORT, "+date");
        builder.put(MetricFilter.USER, "anonymoususer_user11");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData valueData = (ListValueData)metric.getValue(builder.build());
        assertEquals(2, valueData.size());

        MapValueData items = (MapValueData)valueData.getAll().get(0);
        assertEquals(items.getAll().get("date"),
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
        assertEquals(items.getAll().get("date"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:30:00,000").getTime()));
        assertEquals(items.getAll().get("end_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:32:00,000").getTime()));
        assertEquals(items.getAll().get("session_id"), StringValueData.valueOf("3"));
        assertEquals(items.getAll().get("time"), LongValueData.valueOf(2 * 60 * 1000));
        assertEquals(items.getAll().get("domain"), StringValueData.valueOf(""));
        assertEquals(items.getAll().get("ws"), StringValueData.valueOf("ws2"));
        assertEquals(items.getAll().get("user_company"), StringValueData.valueOf(""));
        assertEquals(items.getAll().get("logout_interval"), LongValueData.valueOf(0));

        metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(2));
    }

    @Test
    public void shouldReturnSessionsListForRegisteredUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(MetricFilter.USER, "user@gmail.com");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData valueData = (ListValueData)metric.getValue(builder.build());
        assertEquals(1, valueData.size());

        MapValueData items = (MapValueData)valueData.getAll().get(0);
        assertEquals(items.getAll().get("date"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 20:00:00,000").getTime()));
        assertEquals(items.getAll().get("end_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 20:06:00,000").getTime()));
        assertEquals(items.getAll().get("session_id"), StringValueData.valueOf("2"));
        assertEquals(items.getAll().get("ws"), StringValueData.valueOf("ws1"));
        assertEquals(items.getAll().get("time"), LongValueData.valueOf(360000));
        assertEquals(items.getAll().get("domain"), StringValueData.valueOf("gmail.com"));
        assertEquals(items.getAll().get("user_company"), StringValueData.valueOf("company"));
        assertEquals(items.getAll().get("logout_interval"), LongValueData.valueOf(60 * 1000));

        metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));
    }

    @Test
    public void shouldReturnEmptyList() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(MetricFilter.USER, "user@gmail");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData valueData = (ListValueData)metric.getValue(builder.build());
        assertEquals(0, valueData.size());

        metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        assertEquals(metric.getValue(builder.build()).getAsString(), "0");
    }
}
