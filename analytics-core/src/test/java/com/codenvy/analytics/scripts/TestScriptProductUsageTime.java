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
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptProductUsageTime extends BaseTest {

    @Test
    public void testEventFound() throws Exception {
        List<Event> events = new ArrayList<Event>();

        // 7 min, user1 session #1
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:07:00").build());

        // 4 min, user2 session #1
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2010-10-01")
                        .withTime("20:29:00").build());

        // 7 min, user1 session #2
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("21:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("21:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("21:07:00").build());

        // 0 min, user3 session #1
        events.add(Event.Builder.createProjectBuiltEvent("user3", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:25:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.name(), "20101001");
        params.put(MetricParameter.TO_DATE.name(), "20101003");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME, log, params);
        List<ListStringValueData> all = value.getAll();

        ListStringValueData item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T20:00:00.000Z", "420"));
        ListStringValueData item2 = new ListStringValueData(Arrays.asList("ws1", "user2", "2010-10-01T20:25:00.000Z", "240"));
        ListStringValueData item3 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T21:00:00.000Z", "420"));
        ListStringValueData item4 = new ListStringValueData(Arrays.asList("ws1", "user3", "2010-10-01T20:25:00.000Z", "0"));

        assertEquals(all.size(), 4);
//        assertTrue(all.contains(item1));
//        assertTrue(all.contains(item2));
//        assertTrue(all.contains(item3));
//        assertTrue(all.contains(item4));
    }
}
