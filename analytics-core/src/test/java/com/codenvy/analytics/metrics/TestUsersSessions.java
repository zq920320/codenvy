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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestUsersSessions extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        addRegisteredUser(UID1, "user1@gmail.com");

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_user11", "ws1", "1", "2013-11-01 19:00:00", "2013-11-01 19:06:00", false).
                withDate("2013-11-01").withTime("19:00:00").build());

        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "2", "2013-11-01 20:00:00", "2013-11-01 20:05:00", false).
                withDate("2013-11-01").withTime("20:00:00").build());
        events.add(Event.Builder.createUserSSOLoggedOutEvent("user1@gmail.com").withDate("2013-11-01")
                                .withTime("20:06:00").build());

        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_user11", "ws2", "3", "2013-11-01 19:30:00", "2013-11-01 19:32:00", false).
                withDate("2013-11-01").withTime("19:30:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void shouldReturnSessionsListForAnonUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.SORT, "+date");
        builder.put(MetricFilter.USER_ID, "anonymoususer_user11");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData valueData = (ListValueData)metric.getValue(builder.build());
        assertEquals(2, valueData.size());

        MapValueData items = (MapValueData)valueData.getAll().get(0);
        assertEquals(items.getAll().get("date"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:00:00,000").getTime()));
        assertEquals(items.getAll().get("end_time"),
                     LongValueData.valueOf(fullDateFormatMils.parse("2013-11-01 19:06:00,000").getTime()));
        assertEquals(items.getAll().get("session_id"), StringValueData.valueOf("1"));
        assertEquals(items.getAll().get("time"), LongValueData.valueOf(6 * 60 * 1000));
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

        ListValueData summary = (ListValueData)((Summaraziable)metric).getSummaryValue(builder.build());
        assertEquals(summary.size(), 1);
        items = (MapValueData)summary.getAll().get(0);

        assertEquals(items.getAll().get("time"), LongValueData.valueOf(8 * 60 * 1000));

        metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(2));
    }

    @Test
    public void shouldReturnSessionsListForRegisteredUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(MetricFilter.USER, "user1@gmail.com");

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
        assertEquals(items.getAll().get("user_company"), StringValueData.valueOf(""));
        assertEquals(items.getAll().get("logout_interval"), LongValueData.valueOf(60 * 1000));

        ListValueData summary = (ListValueData)((Summaraziable)metric).getSummaryValue(builder.build());
        assertEquals(summary.size(), 1);
        items = (MapValueData)summary.getAll().get(0);

        assertEquals(items.getAll().get("time"), LongValueData.valueOf(360000));

        metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        assertEquals(metric.getValue(builder.build()), LongValueData.valueOf(1));
    }

    @Test
    public void shouldReturnEmptyList() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(MetricFilter.USER_ID, "user@gmail");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData valueData = (ListValueData)metric.getValue(builder.build());
        assertEquals(0, valueData.size());

        ListValueData summary = (ListValueData)((Summaraziable)metric).getSummaryValue(builder.build());
        assertEquals(summary.size(), 0);

        metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        assertEquals(metric.getValue(builder.build()).getAsString(), "0");
    }
}
