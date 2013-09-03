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

package com.codenvy.analytics.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptUsersCreatedFromFactory extends BaseTest {

    private Map<String, String> context;
    private File                log;

    @BeforeClass
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("user-id1", "user3@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id2", "user4@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id3", "Anonymoususer_1").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id4", "Anonymoususer_2").withDate("2013-01-01").build());

        events.add(Event.Builder.createUserChangedNameEvent("user1@gmail.com", "user3@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_2", "user5@gmail.com").withDate("2013-01-01")
                        .build());

        // anon -> registered
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_1", "user4@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-1", "Anonymoususer_1", "website")
                        .withDate("2013-01-01").build());

        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_2", "user5@gmail.com").withDate("2013-01-01")
                        .build());

        log = LogGenerator.generateLog(events);

        context = Utils.newContext();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
    }

    @Test
    public void testScript() throws Exception {
        LongValueData valueData =
                (LongValueData)executeAndReturnResult(ScriptType.USER_CREATED_FROM_FACTORY, log, context);

        assertEquals(valueData.getAsLong(), 1);
    }

    @Test
    public void testScriptByDomains() throws Exception {
        MapStringLongValueData valueData =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.USER_CREATED_FROM_FACTORY_BY_DOMAINS,
                                                               log,
                                                               context);

        assertEquals(valueData.size(), 1);
        assertEquals(valueData.getAll().get("gmail.com").longValue(), 1L);
    }

    @Test
    public void testScriptByUsers() throws Exception {
        MapStringLongValueData valueData =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.USER_CREATED_FROM_FACTORY_BY_USERS,
                                                               log,
                                                               context);

        assertEquals(valueData.size(), 1);
        assertEquals(valueData.getAll().get("user4@gmail.com").longValue(), 1L);
    }

    @Test
    public void testScriptByWs() throws Exception {
        MapStringLongValueData valueData =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.USER_CREATED_FROM_FACTORY_BY_WS,
                                                               log,
                                                               context);

        assertEquals(valueData.size(), 1);
        assertEquals(valueData.getAll().get("tmp-1").longValue(), 1L);
    }
}
