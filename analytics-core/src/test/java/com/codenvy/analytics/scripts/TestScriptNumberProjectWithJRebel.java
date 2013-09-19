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

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestScriptNumberProjectWithJRebel extends BaseTest {

    @Test
    public void testScriptNumberProjectWithJRebel() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "sessio1", "project1", "type")
                                .withDate("2013-01-01").withTime("10:00:00").build());
        events.add(Event.Builder.createJRebelProjectEvent("user1@gmail.com", "ws1", "session1", "project1", "type", true)
                                .withDate("2013-01-01").withTime("10:05:00").build());

        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "sessio2", "project2", "type")
                                .withDate("2013-01-01").withTime("10:10:00").build());
        events.add(Event.Builder.createJRebelProjectEvent("user2@gmail.com", "ws2", "session2", "project2", "type", true)
                                .withDate("2013-01-01").withTime("10:15:00").build());

        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "sessio4", "project3", "type")
                                .withDate("2013-01-01").withTime("10:20:00").build());
        events.add(Event.Builder.createJRebelProjectEvent("user2@gmail.com", "ws2", "sessio4", "project3", "type", true)
                                .withDate("2013-01-01").withTime("10:25:00").build());

        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "sessio5", "project4", "type")
                                .withDate("2013-01-01").withTime("10:30:00").build());
        events.add(Event.Builder.createJRebelProjectEvent("user2@gmail.com", "ws2", "sessio5", "project4", "type", true)
                                .withDate("2013-01-01").withTime("10:35:00").build());
        events.add(Event.Builder.createJRebelProjectEvent("user2@gmail.com", "ws2", "sessio6", "project4", "type", false)
                                .withDate("2013-01-01").withTime("10:40:00").build());

        events.add(Event.Builder.createJRebelProjectEvent("user1@gmail.com", "ws1", "session7", "project1", "type", true)
                                .withDate("2013-01-01").withTime("10:45:00").build());

        events.add(Event.Builder.createJRebelProjectEvent("user2@gmail.com", "ws2", "sessio8", "project3", "type", true)
                                .withDate("2013-01-01").withTime("10:50:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        LongValueData valueData = (LongValueData)executeAndReturnResult(ScriptType.NUMBER_PROJECT_WITH_JREBEL, log, context);

        assertEquals(valueData.getAsLong(), 3);
    }
}
