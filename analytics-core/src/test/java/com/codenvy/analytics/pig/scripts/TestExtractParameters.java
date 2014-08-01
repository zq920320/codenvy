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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Reshetnyak
 */
public class TestExtractParameters extends BaseTest {

    @BeforeClass
    public void prepare() throws IOException, ParseException {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2014-01-01").withTime("18:00:00").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1@gmail.com")
                                .withDate("2014-01-01").withTime("18:00:00").build());

        events.add(Event.Builder.createSessionStartedEventParamenters("user1@gmail.com", "ws1", "ide", "sid1")
                                .withDate("2014-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEventParameters("user1@gmail.com", "ws1", "ide", "sid1")
                                .withDate("2014-01-01").withTime("19:30:00").build());

        events.add(Event.Builder.createSessionStartedEventParamenters("user2@gmail.com", "ws1", "ide", "sid2")
                                .withDate("2014-01-01").withTime("19:10:00").build());
        events.add(Event.Builder.createSessionFinishedEventParameters("user2@gmail.com", "ws1", "ide", "sid2")
                                .withDate("2014-01-01").withTime("19:45:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
    }

    @Test
    public void testExtractFromParameters() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");

        // test expanded metric value
        List<ValueData> all = treatAsList(MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST).getValue(
                builder.build()));
        assertEquals(all.size(), 2);

        MapValueData value = (MapValueData)all.get(0);
        assertTrue(value.getAll().containsKey("session_id"));
        assertEquals(value.getAll().get("session_id").toString(), "sid1");

        value = (MapValueData)all.get(1);
        assertTrue(value.getAll().containsKey("session_id"));
        assertEquals(value.getAll().get("session_id").toString(), "sid2");
    }
}
