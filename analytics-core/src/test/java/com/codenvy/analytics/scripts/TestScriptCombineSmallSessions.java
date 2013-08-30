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
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptCombineSmallSessions extends BaseTest {

    @Test
    public void testExecuteScript() throws Exception {
        List<Event> events = new ArrayList<>();


        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:06:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("19:07:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("19:10:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("19:20:01").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("19:22:01").build());

        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "4").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "4").withDate("2013-01-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "4").withDate("2013-01-01")
                        .withTime("20:10:00").build());


        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.ANY.name());


//        executeAndReturnResult(ScriptType.TEST_COMBINE_SMALL_SESSIONS, log, params);
    }
}

