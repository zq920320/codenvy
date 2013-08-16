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
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptProductUsageTime extends BaseTest {

    @Test
    public void testExecuteScript() throws Exception {
        List<Event> events = new ArrayList<>();

        // user1 session #1 [7m]
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:07:00").build());

        // user2 session #1 [4m]
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:29:00").build());

        // user1 session #2 [7m]
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("21:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("21:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("21:07:00").build());


        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20101001");
        params.put(MetricParameter.TO_DATE.name(), "20101001");

        MapStringListValueData value =
                (MapStringListValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_USERS, log, params);
        Map<String, ListStringValueData> all = value.getAll();
        assertEquals(all.size(), 2);
        assertEquals(all.get("user1"), new ListStringValueData(Arrays.asList("840", "2")));
        assertEquals(all.get("user2"), new ListStringValueData(Arrays.asList("240", "1")));
    }


}

