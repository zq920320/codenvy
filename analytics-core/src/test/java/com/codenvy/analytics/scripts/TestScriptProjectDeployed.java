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
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptProjectDeployed extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws1", "session", "project1", "type1", "paas1")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws2", "session", "project2", "type1", "paas3")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws3", "session", "project3", "type2", "paas3")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws3", "session", "project4", "type2", "paas3")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3@mail.ru", "ws4", "session", "project4", "type2", "local")
                                .withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20130101");
        context.put(MetricParameter.TO_DATE.name(), "20130101");

        MapValueData value =
                (MapValueData)executeAndReturnResult(ScriptType.PROJECT_DEPLOYED, log, context);

        assertEquals(value.size(), 3);
        assertEquals(value.getAll().get("paas1"), Long.valueOf(1));
        assertEquals(value.getAll().get("paas3"), Long.valueOf(3));
        assertEquals(value.getAll().get("local"), Long.valueOf(1));

        value =
                (MapValueData)executeAndReturnResult(ScriptType.PROJECT_DEPLOYED_BY_USERS, log, context);

        assertEquals(value.size(), 5);
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("paas1", "user1@gmail.com"))), Long.valueOf(1));
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("paas3", "user1@gmail.com"))), Long.valueOf(1));
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("paas3", "user2@gmail.com"))), Long.valueOf(1));
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("paas3", "user3@gmail.com"))), Long.valueOf(1));
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("local", "user3@mail.ru"))), Long.valueOf(1));

        value =
                (MapValueData)executeAndReturnResult(ScriptType.PROJECT_DEPLOYED_BY_DOMAINS, log, context);

        assertEquals(value.size(), 3);
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("paas1", "gmail.com"))), Long.valueOf(1));
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("paas3", "gmail.com"))), Long.valueOf(3));
        assertEquals(value.getAll().get(new ListStringValueData(Arrays.asList("local", "mail.ru"))), Long.valueOf(1));
    }
}
