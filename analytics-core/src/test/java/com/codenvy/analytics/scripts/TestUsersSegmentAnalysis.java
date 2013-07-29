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
import com.codenvy.analytics.metrics.MetricParameter.ENTITY_TYPE;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersSegmentAnalysis extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();

        // 5 min in day
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:30:00").build());
        // 10 min, in week
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-09-30")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-09-30")
                                .withTime("20:30:00").build());


        // 15 min, in month
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-09-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-09-15")
                                .withTime("20:30:00").build());

        // 20 min, in 2 months
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-08-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-08-15")
                                .withTime("20:30:00").build());

        // 25 min, in 3 monthsr
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-07-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-07-15")
                                .withTime("20:30:00").build());


        File log = LogGenerator.generateLog(events);

        FileUtils.deleteDirectory(new File(BASE_DIR, "LOG"));

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.RESULT_DIR.name(), BASE_DIR);
        context.put(MetricParameter.TO_DATE.name(), "20101001");
        execute(ScriptType.PRODUCT_USAGE_TIME_LOG_PREPARATION, log, context);

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_SEGMENT_ANALYSIS_1, log,
                                                                                        context);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 1);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("1", "1", "1", "1", "0", "0"))));
    }
}
