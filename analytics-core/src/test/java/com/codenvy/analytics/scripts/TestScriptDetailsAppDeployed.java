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
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptDetailsAppDeployed extends BaseTest {

    @Test
    public void testScriptDetailsProjectCreatedTypes() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws2", "session", "project2", "type1", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws3", "session", "project3", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws3", "session", "project4", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws4", "session", "project4", "type2", "local")
                                .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20101001");
        context.put(MetricParameter.TO_DATE.name(), "20101001");

        ListListStringValueData value =
                                        (ListListStringValueData)executeAndReturnResult(ScriptType.PROJECTS_DEPLOYED, log, context);

        List<ListStringValueData> all = value.getAll();
        ListStringValueData item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "project1", "type1", "paas1"));
        ListStringValueData item2 = new ListStringValueData(Arrays.asList("ws2", "user1", "project2", "type1", "paas3"));
        ListStringValueData item3 = new ListStringValueData(Arrays.asList("ws3", "user2", "project3", "type2", "paas3"));
        ListStringValueData item4 = new ListStringValueData(Arrays.asList("ws3", "user3", "project4", "type2", "paas3"));
        ListStringValueData item5 = new ListStringValueData(Arrays.asList("ws4", "user3", "project4", "type2", "local"));

        assertEquals(all.size(), 5);
        assertTrue(all.contains(item1));
        assertTrue(all.contains(item2));
        assertTrue(all.contains(item3));
        assertTrue(all.contains(item4));
        assertTrue(all.contains(item5));
    }
}
