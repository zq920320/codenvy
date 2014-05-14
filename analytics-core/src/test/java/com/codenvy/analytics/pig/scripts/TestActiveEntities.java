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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActiveEntities extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "anonymoususer_1").withDate("2013-01-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user2@gmail.com").withDate("2013-01-01").build());
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.toString());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.toString());
        builder.put(Parameters.PARAM, "user");
        builder.put(Parameters.STORAGE_TABLE, MetricType.ACTIVE_USERS_SET.toString().toLowerCase());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.toString());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.toString());
        builder.put(Parameters.PARAM, "ws");
        builder.put(Parameters.STORAGE_TABLE, MetricType.ACTIVE_WORKSPACES_SET.toString().toLowerCase());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());
    }

    @Test
    public void testActiveUsersSet() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_USERS_SET);
        SetValueData result = ValueDataUtil.getAsSet(metric, builder.build());

        assertEquals(result.size(), 2);
        assertTrue(result.getAll().contains(StringValueData.valueOf("user1@gmail.com")));
        assertTrue(result.getAll().contains(StringValueData.valueOf("user2@gmail.com")));
    }

    @Test
    public void testActiveUsers() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_USERS);
        LongValueData result = ValueDataUtil.getAsLong(metric, builder.build());

        assertEquals(result, LongValueData.valueOf(2));
    }

    @Test
    public void testActiveWorkspacesSet() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_WORKSPACES_SET);
        SetValueData result = ValueDataUtil.getAsSet(metric, builder.build());

        assertEquals(result.size(), 2);
        assertTrue(result.getAll().contains(StringValueData.valueOf("ws1")));
        assertTrue(result.getAll().contains(StringValueData.valueOf("ws2")));
    }

    @Test
    public void testActiveWorkspaces() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_WORKSPACES);
        LongValueData result = ValueDataUtil.getAsLong(metric, builder.build());

        assertEquals(result, LongValueData.valueOf(2));
    }
}
